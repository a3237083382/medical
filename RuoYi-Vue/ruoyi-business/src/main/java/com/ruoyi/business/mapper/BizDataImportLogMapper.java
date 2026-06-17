package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizDataImportLog;

public interface BizDataImportLogMapper
{
    BizDataImportLog selectBizDataImportLogById(Long id);

    List<BizDataImportLog> selectBizDataImportLogList(BizDataImportLog log);

    int insertBizDataImportLog(BizDataImportLog log);

    int updateBizDataImportLog(BizDataImportLog log);
}