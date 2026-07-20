package com.ruoyi.business.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.business.domain.BizHisAttachment;
import com.ruoyi.business.domain.BizHisBigdata;
import com.ruoyi.business.domain.BizHisClinicVisit;
import com.ruoyi.business.domain.BizHisHospitalization;
import com.ruoyi.business.mapper.BizHisAttachmentMapper;
import com.ruoyi.business.mapper.BizHisBigdataMapper;
import com.ruoyi.business.mapper.BizHisClinicVisitMapper;
import com.ruoyi.business.mapper.BizHisHospitalizationMapper;
import com.ruoyi.business.service.IBizHistoryQueryService;

@Service
public class BizHistoryQueryServiceImpl implements IBizHistoryQueryService
{
    @Autowired
    private BizHisClinicVisitMapper clinicVisitMapper;

    @Autowired
    private BizHisHospitalizationMapper hospitalizationMapper;

    @Autowired
    private BizHisBigdataMapper bigdataMapper;

    @Autowired
    private BizHisAttachmentMapper attachmentMapper;

    @Override
    public Map<String, Object> queryByPerson(String patientName, String idCard)
    {
        Map<String, Object> result = new LinkedHashMap<>();

        List<BizHisClinicVisit> clinicVisits = queryClinicVisits(patientName, idCard);
        if (!clinicVisits.isEmpty())
        {
            result.put("clinicVisits", clinicVisits);
        }

        List<BizHisHospitalization> hospitalizations = queryHospitalizations(patientName, idCard);
        if (!hospitalizations.isEmpty())
        {
            result.put("hospitalizations", hospitalizations);
        }

        List<BizHisBigdata> bigdataRecords = queryBigdata(patientName, idCard);
        if (!bigdataRecords.isEmpty())
        {
            result.put("bigdataRecords", bigdataRecords);
        }

        List<BizHisAttachment> attachments = queryAttachments(patientName, idCard);
        if (!attachments.isEmpty())
        {
            result.put("attachments", attachments);
        }

        return result;
    }

    @Override
    public List<BizHisClinicVisit> queryClinicVisits(String patientName, String idCard)
    {
        return clinicVisitMapper.selectByPerson(patientName, idCard);
    }

    @Override
    public List<BizHisHospitalization> queryHospitalizations(String patientName, String idCard)
    {
        return hospitalizationMapper.selectByPerson(patientName, idCard);
    }

    @Override
    public List<BizHisBigdata> queryBigdata(String patientName, String idCard)
    {
        return bigdataMapper.selectByPerson(patientName, idCard);
    }

    @Override
    public List<BizHisAttachment> queryAttachments(String patientName, String idCard)
    {
        return attachmentMapper.selectByPerson(patientName, idCard);
    }

    @Override
    public void cacheQueryResult(String queryType, Map<String, Object> rawData)
    {
        if (rawData == null || rawData.isEmpty()) return;
        String patientName = valueOf(rawData.get("patientName"));
        String idCard = valueOf(rawData.get("idCard"));
        if (patientName == null || patientName.isEmpty() || idCard == null || idCard.isEmpty()) return;

        String currentJson = JSON.toJSONString(rawData);
        
        // 查重：如果已有完全相同的dataJson缓存则跳过
        List<BizHisBigdata> existing = bigdataMapper.selectByPerson(patientName, idCard);
        if (existing != null)
        {
            for (BizHisBigdata item : existing)
            {
                if (item != null && currentJson.equals(item.getDataJson()))
                {
                    return;
                }
            }
        }

        String batchNo = "c" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 15);

        BizHisClinicVisit visit = new BizHisClinicVisit();
        visit.setImportBatchNo(batchNo);
        visit.setPatientName(patientName);
        visit.setIdCard(idCard);
        visit.setDiseaseName(valueOf(rawData.get("diagnosis")));
        visit.setOrgName(valueOf(rawData.get("hospital")));
        clinicVisitMapper.insertBizHisClinicVisit(visit);

        BizHisBigdata bigdata = new BizHisBigdata();
        bigdata.setImportBatchNo(batchNo);
        bigdata.setPatientName(patientName);
        bigdata.setIdCard(idCard);
        bigdata.setDataCategory(queryType == null ? "实时缓存" : queryType);
        bigdata.setDataJson(currentJson);
        bigdata.setCreateBy("system");
        bigdataMapper.insertBatch(java.util.List.of(bigdata));
    }

    private String valueOf(Object v)
    {
        return v == null ? null : String.valueOf(v).trim();
    }
}
