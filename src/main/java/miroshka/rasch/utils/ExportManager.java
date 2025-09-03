package miroshka.rasch.utils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import miroshka.rasch.logic.RaschModel;
import miroshka.rasch.model.ExportFormat;

public final class ExportManager {
    
    private ExportManager() {
    }
    
    public static void exportCompleteResults(RaschModel.RaschResult result, File outputFile, ExportFormat format) 
            throws IOException {
        
        validateExportParameters(result, outputFile, format);
        
        switch (format) {
            case WORD -> WordExporter.exportCompleteResultsToWord(result, outputFile);
            case EXCEL -> ExcelExporter.exportCompleteResultsToExcel(result, outputFile);
            case CSV -> CsvExporter.exportCompleteResultsToCsv(result, outputFile);
        }
    }
    
    public static void exportAbilitiesToWord(double[] abilities, File outputFile) throws IOException {
        validateAbilitiesParameters(abilities, outputFile);
        WordExporter.exportAbilitiesToWord(abilities, outputFile);
    }
    
    public static ExportFormat determineFormatFromFile(File file) {
        return Optional.ofNullable(file)
            .map(File::getName)
            .map(ExportManager::getFileExtension)
            .map(ExportFormat::fromExtension)
            .orElse(ExportFormat.WORD);
    }
    
    public static String getDefaultFileName(ExportFormat format) {
        return Optional.ofNullable(format)
            .map(f -> "rasch_analysis_results." + f.getFileExtension())
            .orElse("rasch_analysis_results.docx");
    }
    
    private static void validateExportParameters(RaschModel.RaschResult result, File outputFile, ExportFormat format) {
        Optional.ofNullable(result)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("Результаты анализа не могут быть пустыми"));
        
        Optional.ofNullable(outputFile)
            .orElseThrow(() -> new IllegalArgumentException("Файл вывода не может быть null"));
        
        Optional.ofNullable(format)
            .orElseThrow(() -> new IllegalArgumentException("Формат экспорта не может быть null"));
    }
    
    private static void validateAbilitiesParameters(double[] abilities, File outputFile) {
        Optional.ofNullable(abilities)
            .filter(arr -> arr.length > 0)
            .orElseThrow(() -> new IllegalArgumentException("Массив способностей не может быть пустым"));
        
        Optional.ofNullable(outputFile)
            .orElseThrow(() -> new IllegalArgumentException("Файл вывода не может быть null"));
    }
    
    private static String getFileExtension(String fileName) {
        return Optional.ofNullable(fileName)
            .filter(name -> name.contains("."))
            .map(name -> name.substring(name.lastIndexOf(".") + 1))
            .orElse("");
    }
}
