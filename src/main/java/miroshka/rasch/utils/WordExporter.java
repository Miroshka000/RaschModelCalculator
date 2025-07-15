package miroshka.rasch.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class WordExporter {

    public static void exportAbilitiesToWord(double[] abilities, File outputFile) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat df = new DecimalFormat("0.0000", symbols);
            
            XWPFParagraph titleParagraph = document.createParagraph();
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("Способности (логиты)");
            titleRun.setBold(true);
            titleRun.setFontSize(14);
            
            for (double ability : abilities) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(df.format(ability));
                run.setFontSize(12);
            }
            
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                document.write(out);
            }
        }
    }
} 