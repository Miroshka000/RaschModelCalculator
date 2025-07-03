package miroshka.rasch.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RaschModelProcessor {

    private final DataReader dataReader;
    private final RaschModel raschModel;
    
    public RaschModelProcessor() {
        this.dataReader = new DataReader();
        this.raschModel = new RaschModel();
    }
    
    public double[][] readDataFromFile(File file) throws IOException {
        return dataReader.readData(file);
    }
    
    public RaschModel.RaschResult calculateRaschModel(double[][] data) {
        return raschModel.calculate(data);
    }

    public static class RaschResult {
        public final double[] personAbilities;
        public final double[] itemDifficulties;

        public RaschResult(double[] personAbilities, double[] itemDifficulties) {
            this.personAbilities = personAbilities;
            this.itemDifficulties = itemDifficulties;
        }
    }

    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_CRITERION = 0.001;

    private double[][] readDataFromExcel(File file, boolean isOldFormat) throws IOException {
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
            
            int numCols = 0;
            for (int i = 1; i <= headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    numCols++;
                } else {
                    break;
                }
            }
            
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
                    double value = extractNumericValueFromCell(cell);
                    if (value != -1) { // -1 означает, что ячейка пуста или не содержит число
                        rowData[j] = value > 0 ? 1.0 : 0.0; // Преобразование в бинарные данные (0/1)
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
        
        double[][] result = dataList.toArray(new double[0][]);
        System.out.println("Прочитано строк данных: " + result.length);
        
        if (result.length > 0) {
            System.out.println("Первая строка данных: " + 
                DoubleStream.of(result[0]).mapToObj(String::valueOf).collect(Collectors.joining(", ")));
        }
        
        return result;
    }

    private double extractNumericValueFromCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return -1; // Пустая ячейка
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

    private double[][] readDataFromCsv(File file) throws IOException {
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
                        rowData[i] = numValue > 0 ? 1.0 : 0.0; // Бинаризация данных
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