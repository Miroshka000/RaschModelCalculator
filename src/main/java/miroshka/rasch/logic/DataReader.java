package miroshka.rasch.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataReader {
    
    public enum FileType {
        XLSX, XLS, CSV, UNSUPPORTED
    }
    
    public double[][] readData(File file) throws IOException {
        FileType fileType = determineFileType(file);
        
        switch (fileType) {
            case XLSX:
                return readExcel(file, false);
            case XLS:
                return readExcel(file, true);
            case CSV:
                return readCsv(file);
            default:
                throw new IOException("Неподдерживаемый формат файла. Поддерживаются только .xlsx, .xls и .csv");
        }
    }
    
    private FileType determineFileType(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".xlsx")) {
            return FileType.XLSX;
        } else if (fileName.endsWith(".xls")) {
            return FileType.XLS;
        } else if (fileName.endsWith(".csv")) {
            return FileType.CSV;
        } else {
            return FileType.UNSUPPORTED;
        }
    }
    
    private double[][] readExcel(File file, boolean isOldFormat) throws IOException {
        List<double[]> dataList = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = isOldFormat ? new HSSFWorkbook(fis) : new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();
            
            if (lastRowNum <= firstRowNum) {
                System.out.println("Пустой файл Excel");
                return new double[0][0];
            }
            
            Row headerRow = sheet.getRow(firstRowNum);
            if (headerRow == null) {
                System.out.println("Нет заголовка в Excel");
                return new double[0][0];
            }
            
            int numCols = countNonEmptyColumns(headerRow);
            
            if (numCols <= 0) {
                System.out.println("Нет столбцов с данными");
                return new double[0][0];
            }
            
            System.out.println("Найдено " + numCols + " столбцов данных");
            
            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                double[] rowData = new double[numCols];
                boolean hasData = false;
                
                for (int j = 0; j < numCols; j++) {
                    Cell cell = row.getCell(j + 1);
                    double value = extractNumericValue(cell);
                    if (value != -1) {
                        rowData[j] = value > 0 ? 1.0 : 0.0;
                        hasData = true;
                    } else {
                        rowData[j] = 0.0;
                    }
                }
                
                if (hasData) {
                    dataList.add(rowData);
                }
            }
        }
        
        if (dataList.isEmpty()) {
            System.out.println("Не удалось прочитать данные из Excel");
            return new double[0][0];
        }
        
        System.out.println("Прочитано строк данных: " + dataList.size());
        return dataList.toArray(new double[0][]);
    }
    
    private int countNonEmptyColumns(Row row) {
        int count = 0;
        for (int i = 1; i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
    
    private double extractNumericValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return -1;
        }
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return 1.0;
                    }
                    return cell.getNumericCellValue();
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty()) return -1;
                    
                    try {
                        return Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        String lowercaseValue = value.toLowerCase();
                        if (lowercaseValue.equals("1") || lowercaseValue.equals("true") || 
                            lowercaseValue.equals("да") || lowercaseValue.equals("yes") || 
                            lowercaseValue.equals("+")) {
                            return 1.0;
                        }
                        return 0.0;
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1.0 : 0.0;
                case FORMULA:
                    try {
                        double numericResult = cell.getNumericCellValue();
                        if (Double.isNaN(numericResult) || Double.isInfinite(numericResult)) {
                            return 0.0;
                        }
                        return numericResult;
                    } catch (Exception e) {
                        try {
                            return cell.getStringCellValue().equalsIgnoreCase("true") ? 1.0 : 0.0;
                        } catch (Exception ex) {
                            return 0.0;
                        }
                    }
                default:
                    return -1;
            }
        } catch (Exception e) {
            System.out.println("Ошибка при чтении ячейки: " + e.getMessage());
            return -1;
        }
    }
    
    private double[][] readCsv(File file) throws IOException {
        List<double[]> dataList = new ArrayList<>();
        String line;
        String cvsSplitBy = ";";
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                return new double[0][0];
            }
            
            String[] headerCols = header.split(cvsSplitBy);
            int numCols = headerCols.length - 1;
            
            if (numCols <= 0) {
                return new double[0][0];
            }
            
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy);
                
                if (data.length <= 1) {
                    continue;
                }
                
                double[] rowData = new double[numCols];
                boolean hasData = false;
                
                for (int i = 0; i < numCols && i+1 < data.length; i++) {
                    String value = data[i + 1].replace("\"", "").trim();
                    if (value.isEmpty()) {
                        rowData[i] = 0.0;
                        continue;
                    }
                    
                    try {
                        double numValue = Double.parseDouble(value);
                        rowData[i] = numValue > 0 ? 1.0 : 0.0;
                        hasData = true;
                    } catch (NumberFormatException e) {
                        if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true") || 
                            value.equalsIgnoreCase("да") || value.equalsIgnoreCase("yes") ||
                            value.equals("+")) {
                            rowData[i] = 1.0;
                        } else {
                            rowData[i] = 0.0;
                        }
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    dataList.add(rowData);
                }
            }
        }
        
        return dataList.isEmpty() ? new double[0][0] : dataList.toArray(new double[0][]);
    }
} 