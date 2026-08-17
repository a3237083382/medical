package com.ruoyi.business.service.impl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;
import com.ruoyi.business.domain.BizMedicalQueryRequest;
import com.ruoyi.business.domain.BizMedicalQueryResult;
import com.ruoyi.business.domain.medical.MedicalQueryExportFile;
import com.ruoyi.business.mapper.BizMedicalQueryBatchMapper;
import com.ruoyi.business.mapper.BizMedicalQueryRequestMapper;
import com.ruoyi.business.mapper.BizMedicalQueryResultMapper;
import com.ruoyi.business.service.IDelayedMedicalQueryExportService;
import com.ruoyi.business.service.MedicalQueryException;

@Service
public class DelayedMedicalQueryExportServiceImpl implements IDelayedMedicalQueryExportService
{
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final BizMedicalQueryRequestMapper requestMapper;
    private final BizMedicalQueryBatchMapper batchMapper;
    private final BizMedicalQueryResultMapper resultMapper;

    public DelayedMedicalQueryExportServiceImpl(BizMedicalQueryRequestMapper requestMapper,
            BizMedicalQueryBatchMapper batchMapper, BizMedicalQueryResultMapper resultMapper)
    {
        this.requestMapper = requestMapper;
        this.batchMapper = batchMapper;
        this.resultMapper = resultMapper;
    }

    @Override
    public MedicalQueryExportFile exportRequest(Long companyId, String requestNo)
    {
        BizMedicalQueryRequest request = loadExportableRequest(companyId, requestNo);
        BizMedicalQueryResult result = loadUploadedResult(request);
        if ("COMPLETED".equals(request.getProcessStatus()) && result == null)
        {
            throw new MedicalQueryException("4093", "result is not ready");
        }
        try (Workbook workbook = new XSSFWorkbook())
        {
            Styles styles = new Styles(workbook);
            writeRequestSheet(workbook.createSheet("查询结果"), request, result, styles);
            return new MedicalQueryExportFile("精准延时查询结果_" + request.getRequestNo() + ".xlsx",
                    toBytes(workbook));
        }
        catch (MedicalQueryException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MedicalQueryException("5004", "Excel export failed");
        }
    }

    @Override
    public MedicalQueryExportFile exportBatch(Long companyId, String batchNo)
    {
        if (companyId == null || isEmpty(batchNo))
        {
            throw new MedicalQueryException("4000", "companyId and batchNo are required");
        }
        BizMedicalQueryBatch batch = batchMapper.selectCompanyBatchByNo(companyId, batchNo.trim());
        if (batch == null)
        {
            throw new MedicalQueryException("4042", "batch not found");
        }
        List<BizMedicalQueryBatchItem> items = batchMapper.selectDelayedBatchItems(batch.getId());
        if (items.stream().anyMatch(item -> "PENDING".equals(item.getProcessStatus())
                || "PROCESSING".equals(item.getProcessStatus())))
        {
            throw new MedicalQueryException("4094", "batch still has pending or processing items");
        }

        try (Workbook workbook = new XSSFWorkbook())
        {
            Styles styles = new Styles(workbook);
            writeBatchSummary(workbook.createSheet("批次汇总"), batch, items, styles);
            Set<String> sheetNames = new HashSet<>();
            sheetNames.add("批次汇总");
            for (BizMedicalQueryBatchItem item : items)
            {
                if (!"COMPLETED".equals(item.getProcessStatus()) || !"UPLOADED".equals(item.getUploadStatus()))
                {
                    continue;
                }
                BizMedicalQueryRequest request = requestMapper.selectDelayedRequestById(item.getRequestId());
                if (request == null || !companyId.equals(request.getCompanyId()))
                {
                    continue;
                }
                BizMedicalQueryResult result = resultMapper.selectByRequestId(item.getRequestId());
                if (result == null || result.getUploadedTime() == null)
                {
                    continue;
                }
                String preferredName = String.format("%03d_%s", item.getRowNo(), safeText(item.getPatientName()));
                Sheet sheet = workbook.createSheet(uniqueSheetName(preferredName, sheetNames));
                writeRequestSheet(sheet, request, result, styles);
            }
            return new MedicalQueryExportFile("精准延时批次结果_" + batch.getBatchNo() + ".xlsx",
                    toBytes(workbook));
        }
        catch (MedicalQueryException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new MedicalQueryException("5004", "Excel export failed");
        }
    }

    public String getContentType()
    {
        return EXCEL_CONTENT_TYPE;
    }

    private BizMedicalQueryRequest loadExportableRequest(Long companyId, String requestNo)
    {
        if (companyId == null || isEmpty(requestNo))
        {
            throw new MedicalQueryException("4000", "companyId and requestNo are required");
        }
        BizMedicalQueryRequest request = requestMapper.selectCompanyRequestByNo(companyId, requestNo.trim());
        if (request == null || !"DELAYED".equals(request.getServiceMode()))
        {
            throw new MedicalQueryException("4041", "request not found");
        }
        if ("PENDING".equals(request.getProcessStatus()) || "PROCESSING".equals(request.getProcessStatus()))
        {
            throw new MedicalQueryException("4093", "result is not ready");
        }
        if ("COMPLETED".equals(request.getProcessStatus()) && !"UPLOADED".equals(request.getUploadStatus()))
        {
            throw new MedicalQueryException("4093", "result is not ready");
        }
        return request;
    }

    private BizMedicalQueryResult loadUploadedResult(BizMedicalQueryRequest request)
    {
        BizMedicalQueryResult result = resultMapper.selectByRequestId(request.getId());
        return result != null && result.getUploadedTime() != null ? result : null;
    }

    private void writeBatchSummary(Sheet sheet, BizMedicalQueryBatch batch, List<BizMedicalQueryBatchItem> items,
            Styles styles)
    {
        String[] headers = { "序号", "请求编号", "姓名", "身份证号", "处理状态", "结果状态", "上传状态", "费用(元)" };
        Row title = sheet.createRow(0);
        title.createCell(0).setCellValue("精准延时批次结果 - " + batch.getBatchNo());
        title.getCell(0).setCellStyle(styles.title);
        Row header = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++)
        {
            createTextCell(header, i, headers[i], styles.header);
        }
        int rowIndex = 3;
        for (BizMedicalQueryBatchItem item : items)
        {
            Row row = sheet.createRow(rowIndex++);
            createTextCell(row, 0, String.valueOf(item.getRowNo()), styles.text);
            createTextCell(row, 1, item.getRequestNo(), styles.text);
            createTextCell(row, 2, item.getPatientName(), styles.text);
            createTextCell(row, 3, item.getIdCard(), styles.text);
            createTextCell(row, 4, item.getProcessStatus(), styles.text);
            createTextCell(row, 5, item.getResultStatus(), styles.text);
            createTextCell(row, 6, item.getUploadStatus(), styles.text);
            Cell feeCell = row.createCell(7);
            feeCell.setCellValue(item.getFeeSnapshot() == null ? 0D : item.getFeeSnapshot().doubleValue());
            feeCell.setCellStyle(styles.money);
        }
        sheet.createFreezePane(0, 3);
        setWidths(sheet, new int[] { 10, 26, 14, 24, 16, 16, 16, 14 });
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(2, Math.max(2, rowIndex - 1), 0,
                headers.length - 1));
    }

    private void writeRequestSheet(Sheet sheet, BizMedicalQueryRequest request, BizMedicalQueryResult result,
            Styles styles)
    {
        int rowIndex = 0;
        Row title = sheet.createRow(rowIndex++);
        title.createCell(0).setCellValue("精准延时查询结果");
        title.getCell(0).setCellStyle(styles.title);
        rowIndex++;
        rowIndex = writeMetadata(sheet, rowIndex, "请求编号", request.getRequestNo(), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "姓名", request.getPatientName(), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "身份证号", request.getIdCard(), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "结果状态", request.getResultStatus(), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "结果摘要", result == null ? request.getResultStatus() : result.getResultSummary(), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "完成时间", formatDate(request.getCompleteTime()), styles);
        rowIndex = writeMetadata(sheet, rowIndex, "导出时间", formatDate(new Date()), styles);
        rowIndex++;

        List<Column> columns = result == null ? new ArrayList<>() : parseColumns(result.getColumnSchema());
        List<Map<String, Object>> records = result == null ? new ArrayList<>() : parseRecords(result.getResultData());
        if (columns.isEmpty() && !records.isEmpty())
        {
            for (String field : records.get(0).keySet())
            {
                columns.add(new Column(field, field, columns.size()));
            }
        }
        if (columns.isEmpty())
        {
            columns.add(new Column("result", "查询结果", 0));
        }
        Row header = sheet.createRow(rowIndex++);
        for (int i = 0; i < columns.size(); i++)
        {
            createTextCell(header, i, columns.get(i).label, styles.header);
        }
        int freezeRow = rowIndex;
        for (Map<String, Object> record : records)
        {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++)
            {
                createTextCell(row, i, displayValue(record.get(columns.get(i).field)), styles.text);
            }
        }
        if (records.isEmpty())
        {
            Row row = sheet.createRow(rowIndex);
            createTextCell(row, 0, safeText(result == null ? request.getResultStatus() : result.getResultSummary()), styles.text);
        }
        sheet.createFreezePane(0, freezeRow);
        for (int i = 0; i < columns.size(); i++)
        {
            sheet.setColumnWidth(i, Math.min(50, Math.max(14, columns.get(i).label.length() * 2 + 4)) * 256);
        }
        sheet.setColumnWidth(0, Math.max(sheet.getColumnWidth(0), 22 * 256));
        if (columns.size() > 1)
        {
            sheet.setColumnWidth(1, Math.max(sheet.getColumnWidth(1), 22 * 256));
        }
    }

    private int writeMetadata(Sheet sheet, int rowIndex, String label, String value, Styles styles)
    {
        Row row = sheet.createRow(rowIndex);
        createTextCell(row, 0, label, styles.label);
        createTextCell(row, 1, safeText(value), styles.text);
        return rowIndex + 1;
    }

    @SuppressWarnings("unchecked")
    private List<Column> parseColumns(String json)
    {
        List<Column> columns = new ArrayList<>();
        if (isEmpty(json))
        {
            return columns;
        }
        Object parsed = JSON.parse(json);
        if (!(parsed instanceof List<?>))
        {
            return columns;
        }
        for (Object item : (List<?>) parsed)
        {
            if (!(item instanceof Map<?, ?>))
            {
                continue;
            }
            Map<String, Object> map = (Map<String, Object>) item;
            String field = safeText(map.get("field"));
            if (field.isEmpty())
            {
                continue;
            }
            String label = safeText(map.get("label"));
            int order = map.get("order") instanceof Number ? ((Number) map.get("order")).intValue() : columns.size();
            columns.add(new Column(field, label.isEmpty() ? field : label, order));
        }
        columns.sort(Comparator.comparingInt(column -> column.order));
        return columns;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseRecords(String json)
    {
        List<Map<String, Object>> records = new ArrayList<>();
        if (isEmpty(json))
        {
            return records;
        }
        Object parsed = JSON.parse(json);
        Object recordValue = parsed instanceof Map<?, ?> ? ((Map<?, ?>) parsed).get("records") : parsed;
        if (recordValue instanceof List<?>)
        {
            for (Object item : (List<?>) recordValue)
            {
                if (item instanceof Map<?, ?>)
                {
                    records.add(new LinkedHashMap<>((Map<String, Object>) item));
                }
            }
        }
        else if (parsed instanceof Map<?, ?>)
        {
            records.add(new LinkedHashMap<>((Map<String, Object>) parsed));
        }
        return records;
    }

    private byte[] toBytes(Workbook workbook) throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        workbook.write(output);
        return output.toByteArray();
    }

    private void createTextCell(Row row, int index, String value, CellStyle style)
    {
        Cell cell = row.createCell(index);
        cell.setCellValue(safeText(value));
        cell.setCellStyle(style);
    }

    private String displayValue(Object value)
    {
        if (value == null)
        {
            return "";
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>)
        {
            return JSON.toJSONString(value);
        }
        return String.valueOf(value);
    }

    private String uniqueSheetName(String preferred, Set<String> used)
    {
        String base = safeText(preferred).replaceAll("[\\\\/?*\\[\\]:]", "_");
        if (base.isEmpty())
        {
            base = "查询结果";
        }
        base = base.substring(0, Math.min(31, base.length()));
        String candidate = base;
        int suffix = 2;
        while (used.contains(candidate))
        {
            String suffixText = "_" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 31 - suffixText.length())) + suffixText;
        }
        used.add(candidate);
        return candidate;
    }

    private void setWidths(Sheet sheet, int[] widths)
    {
        for (int i = 0; i < widths.length; i++)
        {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private String formatDate(Date value)
    {
        return value == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }

    private boolean isEmpty(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String safeText(Object value)
    {
        return value == null ? "" : String.valueOf(value);
    }

    private static class Column
    {
        private final String field;
        private final String label;
        private final int order;

        private Column(String field, String label, int order)
        {
            this.field = field;
            this.label = label;
            this.order = order;
        }
    }

    private static class Styles
    {
        private final CellStyle title;
        private final CellStyle header;
        private final CellStyle label;
        private final CellStyle text;
        private final CellStyle money;

        private Styles(Workbook workbook)
        {
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            title = workbook.createCellStyle();
            title.setFont(titleFont);

            Font whiteBold = workbook.createFont();
            whiteBold.setBold(true);
            whiteBold.setColor(IndexedColors.WHITE.getIndex());
            header = bordered(workbook);
            header.setFont(whiteBold);
            header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setAlignment(HorizontalAlignment.CENTER);

            Font bold = workbook.createFont();
            bold.setBold(true);
            label = bordered(workbook);
            label.setFont(bold);
            label.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            label.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            text = bordered(workbook);
            text.setWrapText(true);
            text.setDataFormat(workbook.createDataFormat().getFormat("@"));
            text.setQuotePrefixed(true);
            money = bordered(workbook);
            money.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        }

        private static CellStyle bordered(Workbook workbook)
        {
            CellStyle style = workbook.createCellStyle();
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
            return style;
        }
    }
}
