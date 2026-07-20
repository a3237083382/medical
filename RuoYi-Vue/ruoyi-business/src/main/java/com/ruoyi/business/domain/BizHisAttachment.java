package com.ruoyi.business.domain;
import com.ruoyi.common.core.domain.BaseEntity;
public class BizHisAttachment extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id; private String importBatchNo; private String patientName; private String idCard;
    private String fileType; private String fileName; private String filePath; private Long fileSize;
    public Long getId() { return id; } public void setId(Long v) { this.id = v; }
    public String getImportBatchNo() { return importBatchNo; } public void setImportBatchNo(String v) { this.importBatchNo = v; }
    public String getPatientName() { return patientName; } public void setPatientName(String v) { this.patientName = v; }
    public String getIdCard() { return idCard; } public void setIdCard(String v) { this.idCard = v; }
    public String getFileType() { return fileType; } public void setFileType(String v) { this.fileType = v; }
    public String getFileName() { return fileName; } public void setFileName(String v) { this.fileName = v; }
    public String getFilePath() { return filePath; } public void setFilePath(String v) { this.filePath = v; }
    public Long getFileSize() { return fileSize; } public void setFileSize(Long v) { this.fileSize = v; }
}