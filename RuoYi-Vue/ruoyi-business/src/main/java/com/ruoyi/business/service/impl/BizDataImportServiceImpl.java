package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizDataImportLog;
import com.ruoyi.business.domain.BizHistoryMedicalData;
import com.ruoyi.business.mapper.BizDataImportLogMapper;
import com.ruoyi.business.mapper.BizHistoryMedicalDataMapper;
import com.ruoyi.business.service.IBizDataImportService;

@Service
public class BizDataImportServiceImpl implements IBizDataImportService
{
    private static final Logger log = LoggerFactory.getLogger(BizDataImportServiceImpl.class);
    private static final int BATCH_SIZE = 500;

    @Autowired
    private BizHistoryMedicalDataMapper medicalDataMapper;

    @Autowired
    private BizDataImportLogMapper importLogMapper;

    @Override
    public BizDataImportLog selectById(Long id)
    {
        return importLogMapper.selectBizDataImportLogById(id);
    }

    @Override
    public List<BizDataImportLog> selectList(BizDataImportLog param)
    {
        return importLogMapper.selectBizDataImportLogList(param);
    }

    @Override
    public int deleteByBatchNo(String batchNo)
    {
        return medicalDataMapper.deleteBizHistoryMedicalDataByBatchNo(batchNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcel(MultipartFile file, String createBy) throws Exception
    {
        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls")))
        {
            throw new IllegalArgumentException("only .xlsx or .xls supported");
        }

        String batchNo = UUID.randomUUID().toString().replace("-", "").substring(0, 16) + System.currentTimeMillis();

        BizDataImportLog importLog = new BizDataImportLog();
        importLog.setBatchNo(batchNo);
        importLog.setFileName(originalName);
        importLog.setFileSize(file.getSize());
        importLog.setTotalRows(0);
        importLog.setSuccessRows(0);
        importLog.setFailedRows(0);
        importLog.setStatus("0");
        importLog.setCreateBy(createBy);
        importLogMapper.insertBizDataImportLog(importLog);

        int success = 0;
        int failed = 0;
        try
        {
            List<BizHistoryMedicalData> batch = new ArrayList<>(BATCH_SIZE);
            try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is))
            {
                Sheet sheet = wb.getSheetAt(0);
                String[] headers = new String[0];

                for (int i = 0; i <= sheet.getLastRowNum(); i++)
                {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    int colCount = Math.max(row.getLastCellNum(), 1);
                    String[] values = new String[colCount];
                    for (int j = 0; j < colCount; j++)
                    {
                        values[j] = getCellValue(row.getCell(j));
                    }

                    if (i == 0)
                    {
                        headers = values.clone();
                        continue;
                    }

                    try
                    {
                        BizHistoryMedicalData data = rowToData(values, headers, batchNo, createBy);
                        if (data != null)
                        {
                            batch.add(data);
                        }
                        success++;
                    }
                    catch (Exception e)
                    {
                        failed++;
                        log.warn("Row {} failed: {}", i + 1, e.getMessage());
                    }

                    if (batch.size() >= BATCH_SIZE)
                    {
                        medicalDataMapper.insertBatch(batch);
                        batch.clear();
                    }
                }

                if (!batch.isEmpty())
                {
                    medicalDataMapper.insertBatch(batch);
                }
            }

            importLog.setTotalRows(success + failed);
            importLog.setSuccessRows(success);
            importLog.setFailedRows(failed);
            importLog.setStatus(failed == 0 ? "1" : "2");
            importLogMapper.updateBizDataImportLog(importLog);
        }
        catch (Exception e)
        {
            log.error("Import failed: {}", e.getMessage());
            importLog.setTotalRows(success + failed);
            importLog.setSuccessRows(success);
            importLog.setFailedRows(failed);
            importLog.setStatus("3");
            importLog.setErrorMsg(e.getMessage());
            importLogMapper.updateBizDataImportLog(importLog);
            throw e;
        }

        return batchNo;
    }

    private BizHistoryMedicalData rowToData(String[] values, String[] headers, String batchNo, String createBy)
    {
        BizHistoryMedicalData data = new BizHistoryMedicalData();
        data.setImportBatchNo(batchNo);
        data.setCreateBy(createBy);
        data.setStatus("0");

        java.util.Map<String, String> extraFields = new java.util.LinkedHashMap<>();

        for (int i = 0; i < headers.length && i < values.length; i++)
        {
            String h = (headers[i] == null ? "" : headers[i].trim().toLowerCase());
            String v = (values[i] == null ? "" : values[i].trim());
            if (h.isEmpty()) continue;

            boolean mapped = true;
            if (h.contains("query_type") || h.contains("querytype") || h.contains("\u67E5\u8BE2\u7C7B\u578B"))
                data.setQueryType(v);
            else if (h.contains("patient_name") || h.contains("patientname") || h.contains("\u59D3\u540D") || h.contains("\u60A3\u8005"))
                data.setPatientName(v);
            else if (h.contains("id_card") || h.contains("idcard") || h.contains("\u8EAB\u4EFD\u8BC1") || h.contains("\u8BC1\u4EF6\u53F7"))
                data.setIdCard(v);
            else if (h.contains("gender") || h.contains("\u6027\u522B"))
                data.setGender(v);
            else if (h.contains("birth") || h.contains("\u51FA\u751F"))
                data.setBirthDate(v);
            else if (h.contains("phone") || h.contains("\u7535\u8BDD") || h.contains("\u624B\u673A"))
                data.setPhone(v);
            else if (h.contains("diagnosis") || h.contains("\u8BCA\u65AD") || h.contains("\u75BE\u75C5"))
                data.setDiagnosis(v);
            else if (h.contains("hospital") || h.contains("\u533B\u9662") || h.contains("\u673A\u6784"))
                data.setHospital(v);
            else if (h.contains("department") || h.contains("\u79D1\u5BA4"))
                data.setDepartment(v);
            else if (h.contains("visit_date") || h.contains("visitdate") || h.contains("\u5C31\u8BCA\u65E5\u671F") || h.contains("\u65E5\u671F"))
                data.setVisitDate(v);
            else if (h.contains("visit_type") || h.contains("visittype") || h.contains("\u5C31\u8BCA\u7C7B\u578B"))
                data.setVisitType(v);
            else if (h.contains("disease_code") || h.contains("diseasecode") || h.contains("\u75BE\u75C5\u7F16\u7801"))
                data.setDiseaseCode(v);
            else if (h.contains("medical_record") || h.contains("medicalrecord") || h.contains("\u75C5\u5386\u53F7"))
                data.setMedicalRecordNo(v);
            else if (h.contains("doctor") || h.contains("\u533B\u751F"))
                data.setDoctor(v);
            else
                mapped = false;

            if (!mapped)
            {
                extraFields.put(headers[i].trim(), v);
            }
        }

        if (data.getPatientName() == null || data.getPatientName().isEmpty())
        {
            return null;
        }

        if (!extraFields.isEmpty())
        {
            data.setDataJson(JSON.toJSONString(extraFields));
        }

        return data;
    }

    private String getCellValue(Cell cell)
    {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.STRING) return cell.getStringCellValue();
        else if (type == CellType.NUMERIC)
        {
            if (DateUtil.isCellDateFormatted(cell))
            {
                return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
            }
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val) && !Double.isInfinite(val))
                return String.valueOf((long) val);
            return String.valueOf(val);
        }
        else if (type == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        else if (type == CellType.FORMULA)
        {
            try { return String.valueOf(cell.getNumericCellValue()); }
            catch (Exception e) { return cell.getStringCellValue(); }
        }
        return "";
    }
}