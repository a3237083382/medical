package com.ruoyi.business.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.business.domain.BizHisBigdata;
import com.ruoyi.business.domain.BizHisClinicVisit;
import com.ruoyi.business.domain.BizHisHospitalization;
import com.ruoyi.business.mapper.BizDataImportLogMapper;
import com.ruoyi.business.mapper.BizHisBigdataMapper;
import com.ruoyi.business.mapper.BizHisClinicVisitMapper;
import com.ruoyi.business.mapper.BizHisHospitalizationMapper;
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
    @Autowired
    private BizHisClinicVisitMapper clinicVisitMapper;
    @Autowired
    private BizHisHospitalizationMapper hospitalizationMapper;
    @Autowired
    private BizHisBigdataMapper bigdataMapper;

    @Override
    public BizDataImportLog selectById(Long id) { return importLogMapper.selectBizDataImportLogById(id); }

    @Override
    public List<BizDataImportLog> selectList(BizDataImportLog param) { return importLogMapper.selectBizDataImportLogList(param); }

    @Override
    public int deleteByBatchNo(String batchNo)
    {
        medicalDataMapper.deleteBizHistoryMedicalDataByBatchNo(batchNo);
        clinicVisitMapper.deleteBizHisClinicVisitByBatchNo(batchNo);
        hospitalizationMapper.deleteBizHisHospitalizationByBatchNo(batchNo);
        bigdataMapper.deleteBizHisBigdataByBatchNo(batchNo);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcel(MultipartFile file, String createBy) throws Exception
    {
        String originalName = file.getOriginalFilename();
        if (originalName == null || (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls")))
            throw new IllegalArgumentException("only .xlsx or .xls supported");

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

        int success = 0, failed = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is))
        {
            Sheet sheet = wb.getSheetAt(0);
            String[] headers = new String[0];
            String dataType = null;

            for (int i = 0; i <= sheet.getLastRowNum(); i++)
            {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                int colCount = Math.max(row.getLastCellNum(), 1);
                String[] values = new String[colCount];
                for (int j = 0; j < colCount; j++) values[j] = getCellValue(row.getCell(j));

                if (i == 0) { headers = values.clone(); dataType = detectDataType(headers); continue; }

                try
                {
                    if ("HOSPITALIZATION".equals(dataType))
                    {
                        BizHisHospitalization d = rowToHospitalization(values, headers, batchNo, createBy);
                        if (d != null) hospitalizationMapper.insertBizHisHospitalization(d);
                    }
                    else if ("CLINIC_VISIT".equals(dataType))
                    {
                        BizHisClinicVisit d = rowToClinicVisit(values, headers, batchNo, createBy);
                        if (d != null) clinicVisitMapper.insertBizHisClinicVisit(d);
                    }
                    else
                    {
                        BizHisBigdata d = rowToBigdata(values, headers, batchNo, createBy);
                        if (d != null) bigdataMapper.insertBatch(java.util.List.of(d));
                    }
                    success++;
                }
                catch (Exception e) { failed++; log.warn("Row {} failed: {}", i + 1, e.getMessage()); }
            }
        }

        importLog.setTotalRows(success + failed);
        importLog.setSuccessRows(success);
        importLog.setFailedRows(failed);
        importLog.setStatus(failed == 0 ? "1" : "2");
        importLogMapper.updateBizDataImportLog(importLog);
        return batchNo;
    }

    private String detectDataType(String[] headers)
    {
        String joined = String.join(" ", headers).toLowerCase();
        int cScore = 0, hScore = 0;
        for (String kw : new String[]{"就诊","病种","诊断","医院","科室","visit","disease","doctor"})
            if (joined.contains(kw.toLowerCase())) cScore++;
        for (String kw : new String[]{"医疗费","统筹基金","个人支付","住院","total_fee","pool_fund","hospitalization"})
            if (joined.contains(kw.toLowerCase())) hScore++;
        if (hScore >= 3) return "HOSPITALIZATION";
        if (cScore >= 3) return "CLINIC_VISIT";
        return "BIGDATA";
    }

    private BizHisClinicVisit rowToClinicVisit(String[] v, String[] h, String batchNo, String createBy)
    {
        BizHisClinicVisit d = new BizHisClinicVisit();
        d.setImportBatchNo(batchNo); d.setCreateBy(createBy);
        for (int i = 0; i < h.length && i < v.length; i++)
        {
            String hl = (h[i] == null ? "" : h[i].trim().toLowerCase());
            String val = (v[i] == null ? "" : v[i].trim());
            if (hl.isEmpty()) continue;
            try {
                if (hl.contains("灏辫瘖鏃堕棿")||hl.contains("visit_time")) d.setVisitTime(parseDate(val));
                else if (hl.contains("姓名")||hl.contains("patient_name")) d.setPatientName(val);
                else if (hl.contains("身份证号")||hl.contains("id_card")||hl.contains("证件号码")) d.setIdCard(val);
                else if (hl.contains("浜哄憳缂栧彿")||hl.contains("patient_no")) d.setPatientNo(val);
                else if (hl.contains("险种类型")&&!hl.contains("名称")) d.setInsuranceType(val);
                else if (hl.contains("鏈夋晥鏍囧織")||hl.contains("valid_flag")) d.setValidFlag(val);
                else if (hl.contains("鏈烘瀯浠ｇ爜")&&!hl.contains("名称")) d.setOrgCode(val);
                else if (hl.contains("鏈烘瀯名称")||hl.contains("org_name")) d.setOrgName(val);
                else if (hl.contains("病历号")||hl.contains("medical_record")) d.setMedicalRecordNo(val);
                else if (hl.contains("鐥呯名称")||hl.contains("disease_name")) d.setDiseaseName(val);
                else if (hl.contains("门诊诊断")||hl.contains("outpatient")) d.setOutpatientDiagnosis(val);
                else if (hl.contains("涓昏瘖鍖诲笀")||hl.contains("doctor_name")) d.setDoctorName(val);
                else if (hl.contains("住院主诊断")||hl.contains("inpatient")) d.setInpatientDiagnosis(val);
                else if (hl.contains("缁撴潫鏃堕棿")||hl.contains("end_time")) d.setEndTime(parseDate(val));
                else if (hl.contains("鐥呮儏鎻忚堪")||hl.contains("condition")) d.setConditionDesc(val);
                else if (hl.contains("澶囨敞")||hl.contains("remark")) d.setRemark(val);
            } catch(Exception e) { log.debug("Skip {}: {}", hl, e.getMessage()); }
        }
        return (d.getPatientName() == null || d.getPatientName().isEmpty()) ? null : d;
    }

    private BizHisHospitalization rowToHospitalization(String[] v, String[] h, String batchNo, String createBy)
    {
        BizHisHospitalization d = new BizHisHospitalization();
        d.setImportBatchNo(batchNo); d.setCreateBy(createBy);
        for (int i = 0; i < h.length && i < v.length; i++)
        {
            String hl = (h[i] == null ? "" : h[i].trim().toLowerCase());
            String val = (v[i] == null ? "" : v[i].trim());
            if (hl.isEmpty()) continue;
            try {
                if (hl.contains("姓名")||hl.contains("patient_name")) d.setPatientName(val);
                else if (hl.contains("身份证号")||hl.contains("id_card")) d.setIdCard(val);
                else if (hl.contains("险种类型")&&!hl.contains("名称")) d.setInsuranceType(val);
                else if (hl.contains("闄╃名称")||hl.contains("insurance_name")) d.setInsuranceName(val);
                else if (hl.contains("鍖荤枟绫诲埆")&&!hl.contains("名称")) d.setMedicalCategory(val);
                else if (hl.contains("鍖荤枟绫诲埆名称")) d.setMedicalCategoryName(val);
                else if (hl.contains("鏀粯鍦扮偣")||hl.contains("pay_location")) d.setPayLocation(val);
                else if (hl.contains("鏈烘瀯浠ｇ爜")) d.setOrgCode(val);
                else if (hl.contains("鏈烘瀯名称")) d.setOrgName(val);
                else if (hl.contains("鏈烘瀯绛夌骇")||hl.contains("org_level")) d.setOrgLevel(val);
                else if (hl.contains("就诊开始")||hl.contains("visit_start")) d.setVisitStartTime(parseDate(val));
                else if (hl.contains("灏卞尰缁撴潫")||hl.contains("visit_end")) d.setVisitEndTime(parseDate(val));
                else if (hl.contains("缁撶畻鏃堕棿")||hl.contains("settlement")) d.setSettlementTime(parseDate(val));
                else if (hl.contains("鍖荤枟璐规€婚")||hl.contains("total_fee")) d.setTotalFee(parseDecimal(val));
                else if (hl.contains("全自费")) d.setSelfPayFull(parseDecimal(val));
                else if (hl.contains("超限价自付")) d.setExcessLimitSelfPay(parseDecimal(val));
                else if (hl.contains("鍏堣鑷粯")) d.setAdvanceSelfPay(parseDecimal(val));
                else if (hl.contains("绗﹀悎鑼冨洿")) d.setWithinRangeFee(parseDecimal(val));
                else if (hl.contains("璧蜂粯鏍囧噯")) d.setDeductibleStandard(parseDecimal(val));
                else if (hl.contains("鏈璧蜂粯")) d.setCurrentDeductible(parseDecimal(val));
                else if (hl.contains("实际鏀粯璧蜂粯")) d.setActualDeductible(parseDecimal(val));
                else if (hl.contains("统筹基金鏀嚭")) d.setPoolFundPayment(parseDecimal(val));
                else if (hl.contains("统筹鏀粯姣斾緥")||hl.contains("pay_ratio")) d.setBasicMedicalPayRatio(parseDecimal(val));
                else if (hl.contains("公务员")) d.setCivilServantSubsidy(parseDecimal(val));
                else if (hl.contains("补充鍖荤枟淇濋櫓")) d.setSupplementInsurancePayment(parseDecimal(val));
                else if (hl.contains("大病补充")) d.setSeriousIllnessInsurance(parseDecimal(val));
                else if (hl.contains("大额鍖荤枟琛ュ姪")) d.setLargeMedicalSubsidy(parseDecimal(val));
                else if (hl.contains("残疾浜哄憳")) d.setDisabledPersonnelFund(parseDecimal(val));
                else if (hl.contains("鍖荤枟救助")) d.setMedicalAssistanceFund(parseDecimal(val));
                else if (hl.contains("鍏跺畠基金")||hl.contains("other_fund")) d.setOtherFundPayment(parseDecimal(val));
                else if (hl.contains("基金鏀粯总额")||hl.contains("fund_total")) d.setFundTotalPayment(parseDecimal(val));
                else if (hl.contains("涓汉鏀粯")&&!hl.contains("账户")) d.setPersonalPayment(parseDecimal(val));
                else if (hl.contains("涓汉账户鏀嚭")) d.setPersonalAccountPayment(parseDecimal(val));
                else if (hl.contains("鐜伴噾鏀粯")) d.setCashPayment(parseDecimal(val));
            } catch(Exception e) { log.debug("Skip {}: {}", hl, e.getMessage()); }
        }
        return (d.getPatientName() == null || d.getPatientName().isEmpty()) ? null : d;
    }

    private BizHisBigdata rowToBigdata(String[] v, String[] h, String batchNo, String createBy)
    {
        String name = null, idCard = null;
        Map<String, String> extra = new LinkedHashMap<>();
        for (int i = 0; i < h.length && i < v.length; i++)
        {
            String hl = (h[i] == null ? "" : h[i].trim().toLowerCase());
            String val = (v[i] == null ? "" : v[i].trim());
            if (hl.isEmpty()) continue;
            if (hl.contains("姓名")||hl.contains("patient_name")) name = val;
            else if (hl.contains("身份证号")||hl.contains("id_card")||hl.contains("证件号码")) idCard = val;
            else extra.put(h[i].trim(), val);
        }
        if (name == null || name.isEmpty()) return null;
        BizHisBigdata d = new BizHisBigdata();
        d.setImportBatchNo(batchNo); d.setCreateBy(createBy);
        d.setPatientName(name); d.setIdCard(idCard);
        d.setDataCategory("通用大数据");
        extra.put("_patientName", name);
        if (idCard != null) extra.put("_idCard", idCard);
        d.setDataJson(JSON.toJSONString(extra));
        return d;
    }

    private Date parseDate(String value)
    {
        if (value == null || value.isEmpty()) return null;
        try { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value); }
        catch (Exception e) { try { return new SimpleDateFormat("yyyy-MM-dd").parse(value); } catch (Exception e2) { return null; } }
    }

    private java.math.BigDecimal parseDecimal(String value)
    {
        if (value == null || value.isEmpty()) return null;
        try { return new java.math.BigDecimal(value.replace(",", "")); } catch (Exception e) { return null; }
    }

    private String getCellValue(Cell cell)
    {
        if (cell == null) return "";
        CellType type = cell.getCellType();
        if (type == CellType.STRING) return cell.getStringCellValue();
        else if (type == CellType.NUMERIC)
        {
            if (DateUtil.isCellDateFormatted(cell))
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val) && !Double.isInfinite(val)) return String.valueOf((long) val);
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

