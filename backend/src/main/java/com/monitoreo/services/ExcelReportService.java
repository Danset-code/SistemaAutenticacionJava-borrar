
package com.monitoreo.services;

import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;

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

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public byte[] generar(List<Lectura> lecturas) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte Sensores");

            // Estilo de la cabecera
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_BLUE.getIndex()
            );
            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            String[] columns = {
                    "ID",
                    "Fecha",
                    "Dispositivo",
                    "ID hardware",
                    "Sensor",
                    "Canal",
                    "Tipo",
                    "Valor",
                    "Unidad"
            };

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Se escriben todos los registros recibidos.
            int rowIndex = 1;

            for (Lectura lectura : lecturas) {

                Row row = sheet.createRow(rowIndex++);

                Sensor sensor = lectura.getSensor();

                row.createCell(0).setCellValue(
                        lectura.getId() == null ? 0 : lectura.getId()
                );

                row.createCell(1).setCellValue(
                        formatDate(lectura.getFecha())
                );

                row.createCell(2).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getDeviceAlias())
                );

                row.createCell(3).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getDeviceId())
                );

                row.createCell(4).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getNombre())
                );

                row.createCell(5).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getCanal())
                );

                row.createCell(6).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getTipo())
                );

                Cell valorCell = row.createCell(7);

                if (lectura.getValor() != null) {
                    valorCell.setCellValue(lectura.getValor());
                } else {
                    valorCell.setBlank();
                }

                row.createCell(8).setCellValue(
                        texto(sensor == null
                                ? null
                                : sensor.getUnidad())
                );
            }

            // Ajusta el ancho de las columnas al contenido.
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            return out.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo generar el archivo Excel",
                    e
            );
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private String formatDate(LocalDateTime fecha) {
        return fecha == null
                ? ""
                : DATE_FORMAT.format(fecha);
    }
}