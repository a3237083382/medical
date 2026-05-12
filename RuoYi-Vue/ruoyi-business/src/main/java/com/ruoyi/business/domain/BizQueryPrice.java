package com.ruoyi.business.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 查询价目对象 biz_query_price
 */
public class BizQueryPrice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 查询类型（唯一） */
    @NotBlank(message = "查询类型不能为空")
    @Excel(name = "查询类型")
    private String queryType;

    /** 查询名称 */
    @NotBlank(message = "查询名称不能为空")
    @Excel(name = "查询名称")
    private String queryName;

    /** 单次查询费用 */
    @NotNull(message = "查询费用不能为空")
    @Excel(name = "单次费用", cellType = ColumnType.NUMERIC)
    private BigDecimal fee;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getQueryName() { return queryName; }
    public void setQueryName(String queryName) { this.queryName = queryName; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("queryType", getQueryType())
            .append("queryName", getQueryName())
            .append("fee", getFee())
            .append("status", getStatus())
            .append("remark", getRemark())
            .toString();
    }
}
