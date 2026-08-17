package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BizDelayedQueryImport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long requestId;
    private String fileName;
    private Long fileSize;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private String status;
    private String errorMsg;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getSuccessRows() { return successRows; }
    public void setSuccessRows(Integer successRows) { this.successRows = successRows; }

    public Integer getFailedRows() { return failedRows; }
    public void setFailedRows(Integer failedRows) { this.failedRows = failedRows; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
