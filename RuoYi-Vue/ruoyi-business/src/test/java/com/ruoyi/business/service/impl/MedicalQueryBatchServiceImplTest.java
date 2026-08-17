package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.business.domain.medical.MedicalQueryBatchPreview;
import com.ruoyi.business.domain.medical.MedicalQueryBatchRow;
import com.ruoyi.business.service.MedicalQueryException;

public class MedicalQueryBatchServiceImplTest
{
    private final MedicalQueryBatchServiceImpl service = new MedicalQueryBatchServiceImpl();

    @Test
    public void xlsxPreviewValidatesFieldsAndMarksEveryDuplicateRow() throws Exception
    {
        MockMultipartFile file = workbookFile(new XSSFWorkbook(), "名单.xlsx", List.of(
                new String[] { "姓名", "身份证号" },
                new String[] { "张三", "43010219900101123x" },
                new String[] { "李四", "123" },
                new String[] { "张三", "43010219900101123X" },
                new String[] { "王五", "430102199001011235" }));

        MedicalQueryBatchPreview preview = service.preview(file);

        assertEquals(4, preview.getTotalCount());
        assertEquals(1, preview.getValidCount());
        assertEquals(3, preview.getInvalidCount());
        assertEquals(2, preview.getDuplicateCount());
        assertEquals("43010219900101123X", preview.getRows().get(0).getIdCard());
        assertEquals("43010219900101123x", preview.getRows().get(0).getOriginalIdCard());
        assertTrue(preview.getRows().get(0).getErrors().contains("名单内姓名和身份证号重复"));
        assertTrue(preview.getRows().get(1).getErrors().contains("身份证号必须为18位，末位可以是X"));
        assertTrue(preview.getRows().get(3).isValid());
    }

    @Test
    public void editedRowsCanBeRevalidatedWithoutLosingOriginalValues()
    {
        MedicalQueryBatchRow row = row(2, "原姓名", "错误证件", " 新姓名 ", "43010219900101123x");

        MedicalQueryBatchPreview preview = service.validate(List.of(row));

        assertEquals(1, preview.getValidCount());
        assertTrue(preview.getRows().get(0).isValid());
        assertEquals("原姓名", preview.getRows().get(0).getOriginalName());
        assertEquals("错误证件", preview.getRows().get(0).getOriginalIdCard());
        assertEquals("新姓名", preview.getRows().get(0).getName());
        assertEquals("43010219900101123X", preview.getRows().get(0).getIdCard());
    }

    @Test
    public void xlsFilesAreSupported() throws Exception
    {
        MockMultipartFile file = workbookFile(new HSSFWorkbook(), "名单.xls", List.of(
                new String[] { "patient_name", "id_card" },
                new String[] { "张三", "430102199001011234" }));

        MedicalQueryBatchPreview preview = service.preview(file);

        assertEquals(1, preview.getValidCount());
        assertEquals(2, preview.getRows().get(0).getRowNo());
    }

    @Test
    public void nonExcelExtensionReturnsStableFileTypeCode()
    {
        MockMultipartFile file = new MockMultipartFile("file", "名单.csv", "text/csv", "姓名,身份证号".getBytes());

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.preview(file));

        assertEquals("4004", exception.getCode());
    }

    @Test
    public void moreThanFiveHundredRowsIsRejected()
    {
        List<MedicalQueryBatchRow> rows = new ArrayList<>();
        for (int index = 0; index < 501; index++)
        {
            rows.add(row(index + 2, null, null, "姓名" + index, "430102199001011234"));
        }

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.validate(rows));

        assertEquals("4005", exception.getCode());
    }

    @Test
    public void missingRequiredHeaderIsRejected() throws Exception
    {
        MockMultipartFile file = workbookFile(new XSSFWorkbook(), "名单.xlsx", List.of(
                new String[] { "姓名", "手机号" },
                new String[] { "张三", "13800000000" }));

        MedicalQueryException exception = assertThrows(MedicalQueryException.class, () -> service.preview(file));

        assertEquals("4000", exception.getCode());
        assertFalse(exception.getMessage().isEmpty());
    }

    private MedicalQueryBatchRow row(Integer rowNo, String originalName, String originalIdCard, String name,
            String idCard)
    {
        MedicalQueryBatchRow row = new MedicalQueryBatchRow();
        row.setRowNo(rowNo);
        row.setOriginalName(originalName);
        row.setOriginalIdCard(originalIdCard);
        row.setName(name);
        row.setIdCard(idCard);
        return row;
    }

    private MockMultipartFile workbookFile(Workbook workbook, String fileName, List<String[]> values)
            throws Exception
    {
        try (workbook; ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            Sheet sheet = workbook.createSheet("名单");
            for (int rowIndex = 0; rowIndex < values.size(); rowIndex++)
            {
                Row row = sheet.createRow(rowIndex);
                String[] cells = values.get(rowIndex);
                for (int columnIndex = 0; columnIndex < cells.length; columnIndex++)
                {
                    row.createCell(columnIndex).setCellValue(cells[columnIndex]);
                }
            }
            workbook.write(output);
            return new MockMultipartFile("file", fileName, "application/octet-stream", output.toByteArray());
        }
    }
}
