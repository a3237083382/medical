package com.ruoyi.business.domain.medical;

public class MedicalQueryExportFile
{
    private final String fileName;
    private final byte[] content;

    public MedicalQueryExportFile(String fileName, byte[] content)
    {
        this.fileName = fileName;
        this.content = content;
    }

    public String getFileName()
    {
        return fileName;
    }

    public byte[] getContent()
    {
        return content;
    }
}
