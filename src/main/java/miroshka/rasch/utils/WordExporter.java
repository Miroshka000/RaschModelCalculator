package miroshka.rasch.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import miroshka.rasch.logic.RaschModel;

public final class WordExporter {
    
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0000", SYMBOLS);
    
    private WordExporter() {
    }

    public static void exportAbilitiesToWord(double[] abilities, File outputFile) throws IOException {
        Optional.ofNullable(abilities)
            .filter(arr -> arr.length > 0)
            .orElseThrow(() -> new IllegalArgumentException("Массив способностей не может быть пустым"));
            
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("Способности (логиты)");
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            
            for (double ability : abilities) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(DECIMAL_FORMAT.format(ability));
                run.setFontSize(12);
            }
            
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }
    }
    
    public static void exportCompleteResultsToWord(RaschModel.RaschResult result, File outputFile) throws IOException {
        Optional.ofNullable(result)
            .filter(r -> !r.isEmpty())
            .orElseThrow(() -> new IllegalArgumentException("Результаты анализа не могут быть пустыми"));
            
        try (XWPFDocument document = new XWPFDocument()) {
            
            createDocumentTitle(document, "Полный отчет анализа по модели Раша");
            
            exportPersonSection(document, result);
            
            document.createParagraph();
            
            exportItemSection(document, result);
            
            saveDocument(document, outputFile);
        }
    }
    
    private static void createDocumentTitle(XWPFDocument document, String titleText) {
        XWPFParagraph title = document.createParagraph();
        XWPFRun titleRun = title.createRun();
        titleRun.setText(titleText);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        
        document.createParagraph();
    }
    
    private static void exportPersonSection(XWPFDocument document, RaschModel.RaschResult result) {
        XWPFParagraph subtitle = document.createParagraph();
        XWPFRun subtitleRun = subtitle.createRun();
        subtitleRun.setText("Показатели студентов");
        subtitleRun.setBold(true);
        subtitleRun.setFontSize(14);
        
        XWPFTable table = document.createTable();
        
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("ID студента");
        headerRow.addNewTableCell().setText("Способности (логиты)");
        headerRow.addNewTableCell().setText("Infit MNSQ");
        headerRow.addNewTableCell().setText("Outfit MNSQ");
        headerRow.addNewTableCell().setText("Infit ZSTD");
        headerRow.addNewTableCell().setText("Outfit ZSTD");
        
        double[] abilities = result.getPersonAbilities();
        double[] infitMNSQ = result.getPersonInfitMNSQ();
        double[] outfitMNSQ = result.getPersonOutfitMNSQ();
        double[] infitZSTD = result.getPersonInfitZSTD();
        double[] outfitZSTD = result.getPersonOutfitZSTD();
        
        for (int i = 0; i < abilities.length; i++) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(String.valueOf(i + 1));
            row.getCell(1).setText(DECIMAL_FORMAT.format(abilities[i]));
            row.getCell(2).setText(DECIMAL_FORMAT.format(infitMNSQ[i]));
            row.getCell(3).setText(DECIMAL_FORMAT.format(outfitMNSQ[i]));
            row.getCell(4).setText(DECIMAL_FORMAT.format(infitZSTD[i]));
            row.getCell(5).setText(DECIMAL_FORMAT.format(outfitZSTD[i]));
        }
    }
    
    private static void exportItemSection(XWPFDocument document, RaschModel.RaschResult result) {
        XWPFParagraph subtitle = document.createParagraph();
        XWPFRun subtitleRun = subtitle.createRun();
        subtitleRun.setText("Показатели заданий");
        subtitleRun.setBold(true);
        subtitleRun.setFontSize(14);
        
        XWPFTable table = document.createTable();
        
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("ID задания");
        headerRow.addNewTableCell().setText("Трудность (логиты)");
        headerRow.addNewTableCell().setText("Infit MNSQ");
        headerRow.addNewTableCell().setText("Outfit MNSQ");
        headerRow.addNewTableCell().setText("Infit ZSTD");
        headerRow.addNewTableCell().setText("Outfit ZSTD");
        
        double[] difficulties = result.getItemDifficulties();
        double[] infitMNSQ = result.getItemInfitMNSQ();
        double[] outfitMNSQ = result.getItemOutfitMNSQ();
        double[] infitZSTD = result.getItemInfitZSTD();
        double[] outfitZSTD = result.getItemOutfitZSTD();
        
        for (int i = 0; i < difficulties.length; i++) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(String.valueOf(i + 1));
            row.getCell(1).setText(DECIMAL_FORMAT.format(difficulties[i]));
            row.getCell(2).setText(DECIMAL_FORMAT.format(infitMNSQ[i]));
            row.getCell(3).setText(DECIMAL_FORMAT.format(outfitMNSQ[i]));
            row.getCell(4).setText(DECIMAL_FORMAT.format(infitZSTD[i]));
            row.getCell(5).setText(DECIMAL_FORMAT.format(outfitZSTD[i]));
        }
    }
    
    private static void saveDocument(XWPFDocument document, File outputFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            document.write(out);
        }
    }
} 