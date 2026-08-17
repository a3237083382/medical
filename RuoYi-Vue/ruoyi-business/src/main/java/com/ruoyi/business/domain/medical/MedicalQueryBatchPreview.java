package com.ruoyi.business.domain.medical;

import java.util.ArrayList;
import java.util.List;

public class MedicalQueryBatchPreview
{
    private int totalCount;
    private int validCount;
    private int invalidCount;
    private int duplicateCount;
    private List<MedicalQueryBatchRow> rows = new ArrayList<>();

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int totalCount)
    {
        this.totalCount = totalCount;
    }

    public int getValidCount()
    {
        return validCount;
    }

    public void setValidCount(int validCount)
    {
        this.validCount = validCount;
    }

    public int getInvalidCount()
    {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount)
    {
        this.invalidCount = invalidCount;
    }

    public int getDuplicateCount()
    {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount)
    {
        this.duplicateCount = duplicateCount;
    }

    public List<MedicalQueryBatchRow> getRows()
    {
        return rows;
    }

    public void setRows(List<MedicalQueryBatchRow> rows)
    {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }
}
