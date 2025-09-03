package miroshka.rasch.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miroshka.rasch.logic.RaschModel;

public final class ExcelExporter {
    
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0000", SYMBOLS);
    
    private ExcelExporter() {
    }
    
    public static void exportCompleteResultsToExcel(RaschModel.RaschResult result, File outputFile) throws IOException {
        Optional.ofNullable(result)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("Результаты анализа не могут быть пустыми"));
            
        try (Workbook workbook = new XSSFWorkbook()) {
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            exportPersonSheet(workbook, result, headerStyle, dataStyle);
            
            exportItemSheet(workbook, result, headerStyle, dataStyle);
            
            saveWorkbook(workbook, outputFile);
        }
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }
    
    private static void exportPersonSheet(Workbook workbook, RaschModel.RaschResult result, 
                                        CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Показатели студентов");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID студента", "Способности (логиты)", "Infit MNSQ", 
                           "Outfit MNSQ", "Infit ZSTD", "Outfit ZSTD"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        double[] abilities = result.getPersonAbilities();
        double[] infitMNSQ = result.getPersonInfitMNSQ();
        double[] outfitMNSQ = result.getPersonOutfitMNSQ();
        double[] infitZSTD = result.getPersonInfitZSTD();
        double[] outfitZSTD = result.getPersonOutfitZSTD();
        
        for (int i = 0; i < abilities.length; i++) {
            Row row = sheet.createRow(i + 1);
            
            createCell(row, 0, String.valueOf(i + 1), dataStyle);
            createCell(row, 1, DECIMAL_FORMAT.format(abilities[i]), dataStyle);
            createCell(row, 2, DECIMAL_FORMAT.format(infitMNSQ[i]), dataStyle);
            createCell(row, 3, DECIMAL_FORMAT.format(outfitMNSQ[i]), dataStyle);
            createCell(row, 4, DECIMAL_FORMAT.format(infitZSTD[i]), dataStyle);
            createCell(row, 5, DECIMAL_FORMAT.format(outfitZSTD[i]), dataStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static void exportItemSheet(Workbook workbook, RaschModel.RaschResult result,
                                      CellStyle headerStyle, CellStyle dataStyle) {
        Sheet sheet = workbook.createSheet("Показатели заданий");
        
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID задания", "Трудность (логиты)", "Infit MNSQ", 
                           "Outfit MNSQ", "Infit ZSTD", "Outfit ZSTD"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        double[] difficulties = result.getItemDifficulties();
        double[] infitMNSQ = result.getItemInfitMNSQ();
        double[] outfitMNSQ = result.getItemOutfitMNSQ();
        double[] infitZSTD = result.getItemInfitZSTD();
        double[] outfitZSTD = result.getItemOutfitZSTD();
        
        for (int i = 0; i < difficulties.length; i++) {
            Row row = sheet.createRow(i + 1);
            
            createCell(row, 0, String.valueOf(i + 1), dataStyle);
            createCell(row, 1, DECIMAL_FORMAT.format(difficulties[i]), dataStyle);
            createCell(row, 2, DECIMAL_FORMAT.format(infitMNSQ[i]), dataStyle);
            createCell(row, 3, DECIMAL_FORMAT.format(outfitMNSQ[i]), dataStyle);
            createCell(row, 4, DECIMAL_FORMAT.format(infitZSTD[i]), dataStyle);
            createCell(row, 5, DECIMAL_FORMAT.format(outfitZSTD[i]), dataStyle);
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private static void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    private static void saveWorkbook(Workbook workbook, File outputFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            workbook.write(out);
        }
    }
}
