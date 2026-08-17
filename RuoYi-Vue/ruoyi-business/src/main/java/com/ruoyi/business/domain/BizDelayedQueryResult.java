package com.ruoyi.business.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BizDelayedQueryResult extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long requestId;
    private Integer rowNo;
    private String rawJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Integer getRowNo() { return rowNo; }
    public void setRowNo(Integer rowNo) { this.rowNo = rowNo; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
}
