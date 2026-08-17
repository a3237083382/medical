package com.ruoyi.business.domain.medical;

public class MedicalQueryBatchCancellationResult
{
    private int cancelledCount;
    private int notCancellableCount;
    private MedicalQueryBatchProgress progress;

    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }
    public int getNotCancellableCount() { return notCancellableCount; }
    public void setNotCancellableCount(int notCancellableCount) { this.notCancellableCount = notCancellableCount; }
    public MedicalQueryBatchProgress getProgress() { return progress; }
    public void setProgress(MedicalQueryBatchProgress progress) { this.progress = progress; }
}
