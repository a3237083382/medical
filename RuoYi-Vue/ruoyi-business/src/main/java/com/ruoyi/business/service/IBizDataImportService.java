package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizDataImportLog;
import org.springframework.web.multipart.MultipartFile;

public interface IBizDataImportService
{
    BizDataImportLog selectById(Long id);

    List<BizDataImportLog> selectList(BizDataImportLog log);

    String importExcel(MultipartFile file, String createBy) throws Exception;

    int deleteByBatchNo(String batchNo);
}
