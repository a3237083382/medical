package com.ruoyi.business.domain.medical;

import java.util.List;
import com.ruoyi.business.domain.BizMedicalQueryBatch;
import com.ruoyi.business.domain.BizMedicalQueryBatchItem;

public class DelayedMedicalQueryBatchDetail
{
    private BizMedicalQueryBatch batch;
    private List<BizMedicalQueryBatchItem> items;

    public BizMedicalQueryBatch getBatch() { return batch; }
    public void setBatch(BizMedicalQueryBatch batch) { this.batch = batch; }
    public List<BizMedicalQueryBatchItem> getItems() { return items; }
    public void setItems(List<BizMedicalQueryBatchItem> items) { this.items = items; }
}
