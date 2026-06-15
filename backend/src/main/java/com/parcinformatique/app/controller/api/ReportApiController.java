package com.parcinformatique.app.controller.api;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.parcinformatique.app.entity.Equipment;
import com.parcinformatique.app.repository.EquipmentRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportApiController {

    private final EquipmentRepository equipmentRepository;

    @GetMapping("/inventory.pdf")
    @PreAuthorize("hasAuthority('reports.read')")
    public ResponseEntity<byte[]> exportInventoryPdf() throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        document.add(new Paragraph("Rapport d'inventaire - Parc Informatique"));
        document.add(new Paragraph("Total equipements: " + equipmentRepository.count()));
        document.add(new Paragraph(" "));
        for (Equipment equipment : equipmentRepository.findAllByOrderByNameAsc()) {
            document.add(new Paragraph(
                equipment.getInventoryCode() + " | " + equipment.getName() + " | " + equipment.getStatus()));
        }
        document.close();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("inventory-report.pdf").build().toString())
            .contentType(MediaType.APPLICATION_PDF)
            .body(outputStream.toByteArray());
    }

    @GetMapping("/inventory.xlsx")
    @PreAuthorize("hasAuthority('reports.read')")
    public ResponseEntity<byte[]> exportInventoryExcel() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Inventory");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Code");
            header.createCell(1).setCellValue("Nom");
            header.createCell(2).setCellValue("Statut");
            header.createCell(3).setCellValue("Categorie");
            header.createCell(4).setCellValue("Localisation");

            int rowIndex = 1;
            for (Equipment equipment : equipmentRepository.findAllByOrderByNameAsc()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(equipment.getInventoryCode());
                row.createCell(1).setCellValue(equipment.getName());
                row.createCell(2).setCellValue(equipment.getStatus().name());
                row.createCell(3).setCellValue(equipment.getCategory() != null ? equipment.getCategory().getName() : "");
                row.createCell(4).setCellValue(equipment.getLocation() != null ? equipment.getLocation().getName() : "");
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("inventory-report.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(outputStream.toByteArray());
        }
    }
}
