package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.BizDelayedQueryRequest;
import com.ruoyi.business.domain.BizDelayedQueryResult;
import com.ruoyi.business.service.impl.BizDelayedQueryServiceImpl.CompanyLogs;

public interface IBizDelayedQueryService
{
    public BizDelayedQueryRequest submit(Long companyId, String companyName, String patientName, String idCard, String requestIp);

    public default BizDelayedQueryRequest submit(Long companyId, String companyName, String patientName, String idCard,
            String queryType, String requestIp)
    {
        return submit(companyId, companyName, patientName, idCard, requestIp);
    }

    public List<BizDelayedQueryRequest> submitBatch(Long companyId, String companyName,
            List<BizDelayedQueryRequest> requests, String requestIp);

    public default List<BizDelayedQueryRequest> submitBatch(Long companyId, String companyName,
            List<BizDelayedQueryRequest> requests, String queryType, String requestIp)
    {
        return submitBatch(companyId, companyName, requests, requestIp);
    }

    public List<BizDelayedQueryRequest> selectList(BizDelayedQueryRequest request);

    public int countPendingRequests();

    public Map<String, Object> cancelBatch(Long companyId, String batchNo);

    public Map<String, Object> cancelItem(Long companyId, Long id);

    public BizDelayedQueryRequest selectAdminDetail(Long id);

    public BizDelayedQueryRequest selectCompanyDetail(Long id, Long companyId);

    public BizDelayedQueryRequest saveDraft(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String handlerName);

    public BizDelayedQueryRequest complete(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String handlerName);

    public BizDelayedQueryRequest updateUploadedResult(Long id, List<BizDelayedQueryResult> results,
            String resultStatus, String resultMessage, String modifyBy, String modifyReason);

    public BizDelayedQueryRequest importExcel(Long id, MultipartFile file, String operator) throws Exception;

    public CompanyLogs selectCompanyLogs(Long companyId);
}
