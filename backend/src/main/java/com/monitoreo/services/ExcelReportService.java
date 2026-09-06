package com.monitoreo.services;

import com.monitoreo.models.Lectura;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] generar(List<Lectura> lecturas) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Reporte Sensores");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Fecha", "Sensor", "Tipo", "Valor", "Unidad", "Estado"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Lectura lectura : lecturas) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(lectura.getId() == null ? 0 : lectura.getId());
                row.createCell(1).setCellValue(formatDate(lectura.getFecha()));
                row.createCell(2).setCellValue(lectura.getSensor().getNombre());
                row.createCell(3).setCellValue(lectura.getSensor().getTipo());
                row.createCell(4).setCellValue(lectura.getValor() == null ? 0.0 : lectura.getValor());
                row.createCell(5).setCellValue(lectura.getSensor().getUnidad());
                row.createCell(6).setCellValue(lectura.getSensor().getEstado());
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el archivo Excel", e);
        }
    }

    private String formatDate(LocalDateTime fecha) {
        return fecha == null ? "" : DATE_FORMAT.format(fecha);
    }
}
