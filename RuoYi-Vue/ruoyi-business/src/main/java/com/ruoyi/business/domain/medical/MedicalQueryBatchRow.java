package com.ruoyi.business.domain.medical;

import java.util.ArrayList;
import java.util.List;

public class MedicalQueryBatchRow
{
    private Integer rowNo;
    private String originalName;
    private String originalIdCard;
    private String name;
    private String idCard;
    private String startDate;
    private String endDate;
    private boolean valid;
    private List<String> errors = new ArrayList<>();

    public Integer getRowNo()
    {
        return rowNo;
    }

    public void setRowNo(Integer rowNo)
    {
        this.rowNo = rowNo;
    }

    public String getOriginalName()
    {
        return originalName;
    }

    public void setOriginalName(String originalName)
    {
        this.originalName = originalName;
    }

    public String getOriginalIdCard()
    {
        return originalIdCard;
    }

    public void setOriginalIdCard(String originalIdCard)
    {
        this.originalIdCard = originalIdCard;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public void setErrors(List<String> errors)
    {
        this.errors = errors == null ? new ArrayList<>() : errors;
    }
}
