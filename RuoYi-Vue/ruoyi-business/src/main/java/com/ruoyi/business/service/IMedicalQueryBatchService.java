package com.ruoyi.business.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.medical.MedicalQueryBatchPreview;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;

public interface IMedicalQueryBatchService
{
    MedicalQueryBatchPreview preview(MultipartFile file);

    MedicalQueryBatchPreview previewRealtime(MultipartFile file);

    MedicalQueryBatchPreview validate(List<MedicalQueryBatchRow> rows);

    MedicalQueryBatchPreview validateRealtime(List<MedicalQueryBatchRow> rows);
}
