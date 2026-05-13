package com.ruoyi.business.service;

public class MedicalQueryException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final String code;

    public MedicalQueryException(String code, String message)
    {
        super(message);
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}
