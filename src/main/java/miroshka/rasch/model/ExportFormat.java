package miroshka.rasch.model;

import java.util.Arrays;
import java.util.List;

public enum ExportFormat {
    WORD("Word документ", "*.docx", "docx", "Документ Microsoft Word"),
    EXCEL("Excel таблица", "*.xlsx", "xlsx", "Электронная таблица Microsoft Excel"),
    CSV("CSV файл", "*.csv", "csv", "Файл значений, разделенных запятыми");
    
    private final String displayName;
    private final String filePattern;
    private final String fileExtension;
    private final String description;
    
    ExportFormat(String displayName, String filePattern, String fileExtension, String description) {
        this.displayName = displayName;
        this.filePattern = filePattern;
        this.fileExtension = fileExtension;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getFilePattern() {
        return filePattern;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static List<ExportFormat> getAllFormats() {
        return Arrays.asList(values());
    }
    
    public static ExportFormat fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return WORD;
        }
        
        String cleanExtension = extension.toLowerCase().replace(".", "");
        
        for (ExportFormat format : values()) {
            if (format.fileExtension.equals(cleanExtension)) {
                return format;
            }
        }
        
        return WORD;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
