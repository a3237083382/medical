package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.medical.MedicalQueryBatchPreview;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.service.IMedicalQueryBatchService;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class MedicalQueryBatchServiceImpl implements IMedicalQueryBatchService
{
    private static final int MAX_ROWS = 500;
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String DUPLICATE_ERROR = "名单内姓名和身份证号重复";
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[0-9]{17}[0-9X]$");
    private static final Set<String> NAME_HEADERS = Set.of("姓名", "患者姓名", "被保险人姓名", "name", "patientname");
    private static final Set<String> ID_CARD_HEADERS = Set.of("身份证号", "身份证号码", "证件号码", "idcard", "identitycard");
    private static final Set<String> START_HEADERS = Set.of("开始时间", "起始时间", "startdate", "starttime");
    private static final Set<String> END_HEADERS = Set.of("结束时间", "终止时间", "enddate", "endtime");

    @Override
    public MedicalQueryBatchPreview preview(MultipartFile file)
    {
        validateFile(file);
        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input))
        {
            if (workbook.getNumberOfSheets() == 0)
            {
                throw invalidParam("Excel中没有工作表");
            }
            return readSheet(workbook.getSheetAt(0));
        }
        catch (MedicalQueryException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw invalidParam("Excel文件无法解析");
        }
    }

    @Override
    public MedicalQueryBatchPreview previewRealtime(MultipartFile file)
    {
        validateFile(file);
        try (InputStream input = file.getInputStream(); Workbook workbook = WorkbookFactory.create(input))
        {
            if (workbook.getNumberOfSheets() == 0) throw invalidParam("Excel中没有工作表");
            return readRealtimeSheet(workbook.getSheetAt(0));
        }
        catch (MedicalQueryException e) { throw e; }
        catch (Exception e) { throw invalidParam("Excel文件无法解析"); }
    }

    private MedicalQueryBatchPreview readRealtimeSheet(Sheet sheet)
    {
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        int headerIndex = findFirstNonEmptyRow(sheet, formatter);
        if (headerIndex < 0) throw invalidParam("Excel名单不能为空");
        Row header = sheet.getRow(headerIndex);
        int idColumn = findHeaderColumn(header, formatter, ID_CARD_HEADERS);
        int startColumn = findHeaderColumn(header, formatter, START_HEADERS);
        int endColumn = findHeaderColumn(header, formatter, END_HEADERS);
        if (idColumn < 0 || startColumn < 0 || endColumn < 0) throw invalidParam("实时查询Excel必须包含身份证号、开始时间和结束时间列");
        List<MedicalQueryBatchRow> rows = new ArrayList<>();
        for (int rowIndex = headerIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++)
        {
            Row excelRow = sheet.getRow(rowIndex);
            String id = cellValue(excelRow, idColumn, formatter);
            String start = cellValue(excelRow, startColumn, formatter);
            String end = cellValue(excelRow, endColumn, formatter);
            if (normalize(id).isEmpty() && normalize(start).isEmpty() && normalize(end).isEmpty()) continue;
            MedicalQueryBatchRow row = new MedicalQueryBatchRow();
            row.setRowNo(rows.size() + 1); row.setOriginalIdCard(id); row.setIdCard(normalize(id).toUpperCase(Locale.ROOT));
            row.setStartDate(normalize(start)); row.setEndDate(normalize(end));
            List<String> errors = new ArrayList<>();
            if (!ID_CARD_PATTERN.matcher(row.getIdCard()).matches()) errors.add("身份证号必须为18位，末位可以是X");
            if (row.getStartDate().isEmpty() || row.getEndDate().isEmpty()) errors.add("开始时间和结束时间不能为空");
            try { if (!row.getStartDate().isEmpty() && !row.getEndDate().isEmpty() && row.getStartDate().compareTo(row.getEndDate()) > 0) errors.add("开始时间不能晚于结束时间"); } catch (Exception ignored) { }
            row.setErrors(errors); row.setValid(errors.isEmpty()); rows.add(row);
        }
        if (rows.isEmpty()) throw invalidParam("Excel名单不能为空");
        MedicalQueryBatchPreview result = new MedicalQueryBatchPreview(); result.setRows(rows); result.setTotalCount(rows.size());
        result.setValidCount((int) rows.stream().filter(MedicalQueryBatchRow::isValid).count()); result.setInvalidCount(rows.size() - result.getValidCount());
        return result;
    }

    @Override
    public MedicalQueryBatchPreview validate(List<MedicalQueryBatchRow> rows)
    {
        if (rows == null || rows.isEmpty())
        {
            throw invalidParam("名单不能为空");
        }
        if (rows.size() > MAX_ROWS)
        {
            throw new MedicalQueryException("4005", "名单最多支持500人");
        }

        List<MedicalQueryBatchRow> normalizedRows = new ArrayList<>(rows.size());
        for (int index = 0; index < rows.size(); index++)
        {
            MedicalQueryBatchRow source = rows.get(index);
            MedicalQueryBatchRow row = new MedicalQueryBatchRow();
            row.setRowNo(source == null || source.getRowNo() == null ? index + 2 : source.getRowNo());
            row.setOriginalName(source == null ? null : source.getOriginalName());
            row.setOriginalIdCard(source == null ? null : source.getOriginalIdCard());
            row.setName(normalize(source == null ? null : source.getName()));
            row.setIdCard(normalize(source == null ? null : source.getIdCard()).toUpperCase(Locale.ROOT));
            row.setStartDate(normalize(source == null ? null : source.getStartDate()));
            row.setEndDate(normalize(source == null ? null : source.getEndDate()));
            row.setErrors(validateRow(row));
            normalizedRows.add(row);
        }

        markDuplicates(normalizedRows);
        return summarize(normalizedRows);
    }

    @Override
    public MedicalQueryBatchPreview validateRealtime(List<MedicalQueryBatchRow> rows)
    {
        if (rows == null || rows.isEmpty()) throw invalidParam("名单不能为空");
        if (rows.size() > MAX_ROWS) throw new MedicalQueryException("4005", "名单最多支持500人");
        List<MedicalQueryBatchRow> normalized = new ArrayList<>(rows.size());
        Set<String> seen = new java.util.HashSet<>();
        for (int index = 0; index < rows.size(); index++)
        {
            MedicalQueryBatchRow source = rows.get(index);
            MedicalQueryBatchRow row = new MedicalQueryBatchRow();
            row.setRowNo(source == null || source.getRowNo() == null ? index + 2 : source.getRowNo());
            row.setOriginalIdCard(source == null ? null : source.getOriginalIdCard());
            row.setIdCard(normalize(source == null ? null : source.getIdCard()).toUpperCase(Locale.ROOT));
            row.setStartDate(normalize(source == null ? null : source.getStartDate()));
            row.setEndDate(normalize(source == null ? null : source.getEndDate()));
            List<String> errors = new ArrayList<>();
            if (!ID_CARD_PATTERN.matcher(row.getIdCard()).matches()) errors.add("身份证号必须为18位，末位可以是X");
            if (row.getStartDate().isEmpty() || row.getEndDate().isEmpty()) errors.add("开始时间和结束时间不能为空");
            if (!row.getStartDate().isEmpty() && !row.getEndDate().isEmpty() && row.getStartDate().compareTo(row.getEndDate()) > 0) errors.add("开始时间不能晚于结束时间");
            String key = row.getIdCard() + "\u0000" + row.getStartDate() + "\u0000" + row.getEndDate();
            if (!row.getIdCard().isEmpty() && !row.getStartDate().isEmpty() && !row.getEndDate().isEmpty() && !seen.add(key)) errors.add("名单内身份证号和时间范围重复");
            row.setErrors(errors); row.setValid(errors.isEmpty()); normalized.add(row);
        }
        return summarize(normalized);
    }

    private MedicalQueryBatchPreview readSheet(Sheet sheet)
    {
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        int headerIndex = findFirstNonEmptyRow(sheet, formatter);
        if (headerIndex < 0)
        {
            throw invalidParam("Excel名单不能为空");
        }
        Row header = sheet.getRow(headerIndex);
        int nameColumn = findHeaderColumn(header, formatter, NAME_HEADERS);
        int idCardColumn = findHeaderColumn(header, formatter, ID_CARD_HEADERS);
        if (nameColumn < 0 || idCardColumn < 0)
        {
            throw invalidParam("Excel必须包含姓名和身份证号列");
        }

        List<MedicalQueryBatchRow> rows = new ArrayList<>();
        for (int rowIndex = headerIndex + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++)
        {
            Row excelRow = sheet.getRow(rowIndex);
            String originalName = cellValue(excelRow, nameColumn, formatter);
            String originalIdCard = cellValue(excelRow, idCardColumn, formatter);
            if (normalize(originalName).isEmpty() && normalize(originalIdCard).isEmpty())
            {
                continue;
            }
            if (rows.size() >= MAX_ROWS)
            {
                throw new MedicalQueryException("4005", "名单最多支持500人");
            }
            MedicalQueryBatchRow row = new MedicalQueryBatchRow();
            row.setRowNo(rowIndex + 1);
            row.setOriginalName(originalName);
            row.setOriginalIdCard(originalIdCard);
            row.setName(normalize(originalName));
            row.setIdCard(normalize(originalIdCard).toUpperCase(Locale.ROOT));
            rows.add(row);
        }
        if (rows.isEmpty())
        {
            throw invalidParam("Excel名单不能为空");
        }
        return validate(rows);
    }

    private List<String> validateRow(MedicalQueryBatchRow row)
    {
        List<String> errors = new ArrayList<>();
        if (row.getName().isEmpty())
        {
            errors.add("姓名不能为空");
        }
        else if (row.getName().length() > 50)
        {
            errors.add("姓名不能超过50个字符");
        }
        if (row.getIdCard().isEmpty())
        {
            errors.add("身份证号不能为空");
        }
        else if (!ID_CARD_PATTERN.matcher(row.getIdCard()).matches())
        {
            errors.add("身份证号必须为18位，末位可以是X");
        }
        return errors;
    }

    private void markDuplicates(List<MedicalQueryBatchRow> rows)
    {
        Map<String, List<MedicalQueryBatchRow>> groups = new HashMap<>();
        for (MedicalQueryBatchRow row : rows)
        {
            if (!row.getName().isEmpty() && !row.getIdCard().isEmpty())
            {
                groups.computeIfAbsent(row.getName() + "\u0000" + row.getIdCard(), key -> new ArrayList<>()).add(row);
            }
        }
        for (List<MedicalQueryBatchRow> group : groups.values())
        {
            if (group.size() > 1)
            {
                for (MedicalQueryBatchRow row : group)
                {
                    row.getErrors().add(DUPLICATE_ERROR);
                }
            }
        }
    }

    private MedicalQueryBatchPreview summarize(List<MedicalQueryBatchRow> rows)
    {
        int validCount = 0;
        int duplicateCount = 0;
        for (MedicalQueryBatchRow row : rows)
        {
            row.setValid(row.getErrors().isEmpty());
            if (row.isValid())
            {
                validCount++;
            }
            if (row.getErrors().contains(DUPLICATE_ERROR))
            {
                duplicateCount++;
            }
        }
        MedicalQueryBatchPreview preview = new MedicalQueryBatchPreview();
        preview.setTotalCount(rows.size());
        preview.setValidCount(validCount);
        preview.setInvalidCount(rows.size() - validCount);
        preview.setDuplicateCount(duplicateCount);
        preview.setRows(rows);
        return preview;
    }

    private int findFirstNonEmptyRow(Sheet sheet, DataFormatter formatter)
    {
        for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++)
        {
            Row row = sheet.getRow(rowIndex);
            if (row == null)
            {
                continue;
            }
            for (int columnIndex = 0; columnIndex < Math.max(0, row.getLastCellNum()); columnIndex++)
            {
                if (!normalize(formatter.formatCellValue(row.getCell(columnIndex))).isEmpty())
                {
                    return rowIndex;
                }
            }
        }
        return -1;
    }

    private int findHeaderColumn(Row header, DataFormatter formatter, Set<String> aliases)
    {
        for (int index = 0; index < Math.max(0, header.getLastCellNum()); index++)
        {
            if (aliases.contains(normalizeHeader(formatter.formatCellValue(header.getCell(index)))))
            {
                return index;
            }
        }
        return -1;
    }

    private String cellValue(Row row, int columnIndex, DataFormatter formatter)
    {
        return row == null ? "" : formatter.formatCellValue(row.getCell(columnIndex));
    }

    private void validateFile(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw invalidParam("请选择Excel文件");
        }
        String fileName = file.getOriginalFilename();
        String normalizedFileName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (!normalizedFileName.endsWith(".xlsx") && !normalizedFileName.endsWith(".xls"))
        {
            throw new MedicalQueryException("4004", "只支持.xlsx或.xls文件");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw invalidParam("Excel文件不能超过10MB");
        }
    }

    private String normalizeHeader(String value)
    {
        return normalize(value).toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace("-", "");
    }

    private String normalize(String value)
    {
        return value == null ? "" : value.trim();
    }

    private MedicalQueryException invalidParam(String message)
    {
        return new MedicalQueryException("4000", message);
    }
}
