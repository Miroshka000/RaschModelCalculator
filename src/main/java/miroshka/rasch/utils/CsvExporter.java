package miroshka.rasch.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

import miroshka.rasch.logic.RaschModel;

public final class CsvExporter {
    
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0000", SYMBOLS);
    private static final String SEPARATOR = ";";
    
    private CsvExporter() {
    }
    
    public static void exportCompleteResultsToCsv(RaschModel.RaschResult result, File outputFile) throws IOException {
        Optional.ofNullable(result)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("Результаты анализа не могут быть пустыми"));
            
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            exportPersonSection(writer, result);
            
            writer.println();
            
            exportItemSection(writer, result);
        }
    }
    
    private static void exportPersonSection(PrintWriter writer, RaschModel.RaschResult result) {
        writer.println("ПОКАЗАТЕЛИ СТУДЕНТОВ");
        
        String[] headers = {"ID студента", "Способности (логиты)", "Infit MNSQ", 
                           "Outfit MNSQ", "Infit ZSTD", "Outfit ZSTD"};
        writer.println(String.join(SEPARATOR, headers));
        
        double[] abilities = result.getPersonAbilities();
        double[] infitMNSQ = result.getPersonInfitMNSQ();
        double[] outfitMNSQ = result.getPersonOutfitMNSQ();
        double[] infitZSTD = result.getPersonInfitZSTD();
        double[] outfitZSTD = result.getPersonOutfitZSTD();
        
        for (int i = 0; i < abilities.length; i++) {
            String[] rowData = {
                String.valueOf(i + 1),
                DECIMAL_FORMAT.format(abilities[i]),
                DECIMAL_FORMAT.format(infitMNSQ[i]),
                DECIMAL_FORMAT.format(outfitMNSQ[i]),
                DECIMAL_FORMAT.format(infitZSTD[i]),
                DECIMAL_FORMAT.format(outfitZSTD[i])
            };
            writer.println(String.join(SEPARATOR, rowData));
        }
    }
    
    private static void exportItemSection(PrintWriter writer, RaschModel.RaschResult result) {
        writer.println("ПОКАЗАТЕЛИ ЗАДАНИЙ");
        
        String[] headers = {"ID задания", "Трудность (логиты)", "Infit MNSQ", 
                           "Outfit MNSQ", "Infit ZSTD", "Outfit ZSTD"};
        writer.println(String.join(SEPARATOR, headers));
        
        double[] difficulties = result.getItemDifficulties();
        double[] infitMNSQ = result.getItemInfitMNSQ();
        double[] outfitMNSQ = result.getItemOutfitMNSQ();
        double[] infitZSTD = result.getItemInfitZSTD();
        double[] outfitZSTD = result.getItemOutfitZSTD();
        
        for (int i = 0; i < difficulties.length; i++) {
            String[] rowData = {
                String.valueOf(i + 1),
                DECIMAL_FORMAT.format(difficulties[i]),
                DECIMAL_FORMAT.format(infitMNSQ[i]),
                DECIMAL_FORMAT.format(outfitMNSQ[i]),
                DECIMAL_FORMAT.format(infitZSTD[i]),
                DECIMAL_FORMAT.format(outfitZSTD[i])
            };
            writer.println(String.join(SEPARATOR, rowData));
        }
    }
}
