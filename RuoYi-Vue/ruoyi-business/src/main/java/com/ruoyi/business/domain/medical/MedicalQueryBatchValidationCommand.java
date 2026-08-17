package com.ruoyi.business.domain.medical;

import java.util.ArrayList;
import java.util.List;

public class MedicalQueryBatchValidationCommand
{
    private List<MedicalQueryBatchRow> rows = new ArrayList<>();

    public List<MedicalQueryBatchRow> getRows()
    {
        return rows;
    }

    public void setRows(List<MedicalQueryBatchRow> rows)
    {
        this.rows = rows;
    }
}
