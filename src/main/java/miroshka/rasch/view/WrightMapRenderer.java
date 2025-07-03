package miroshka.rasch.view;

import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import miroshka.rasch.logic.RaschModel;

public class WrightMapRenderer {
    private static final double MIN_VAL = -3.0;
    private static final double MAX_VAL = 3.0;
    private static final int NUM_BINS = 12;
    
    public void drawWrightMap(Pane wrightMapPane, RaschModel.RaschResult result) {
        wrightMapPane.getChildren().clear();

        if (result == null || result.isEmpty()) {
            Text noDataText = createText("Нет данных для отображения", 
                    wrightMapPane.getWidth() / 2 - 100, 
                    wrightMapPane.getHeight() / 2, true);
            wrightMapPane.getChildren().add(noDataText);
            return;
        }

        final double paneWidth = wrightMapPane.getWidth();
        final double paneHeight = wrightMapPane.getHeight();
        final double range = 6.0;
        final double center = paneHeight / 2;
        final double scale = paneHeight / range;
        final double binSize = (MAX_VAL - MIN_VAL) / NUM_BINS;

        drawCoordinateSystem(wrightMapPane, paneWidth, paneHeight, scale, center);
        
        int[] personBins = calculateHistogramBins(result.getPersonAbilities(), binSize);
        int[] itemBins = calculateHistogramBins(result.getItemDifficulties(), binSize);

        int maxCount = findMaxCount(personBins, itemBins);
        double barWidthScale = (paneWidth / 2 - 80) / maxCount;

        drawHistograms(wrightMapPane, personBins, itemBins, paneWidth, center, scale, binSize, barWidthScale);
        
        addLabels(wrightMapPane, paneWidth, paneHeight);
    }
    
    private void drawCoordinateSystem(Pane wrightMapPane, double paneWidth, double paneHeight, double scale, double center) {
        Rectangle vAxis = new Rectangle(paneWidth / 2, 30, 2, paneHeight - 60);
        vAxis.setFill(Color.GRAY);
        wrightMapPane.getChildren().add(vAxis);

        for (double i = MIN_VAL; i <= MAX_VAL; i += 1.0) {
            double y = center - i * scale;
            
            Rectangle line = new Rectangle(40, y, paneWidth - 80, 1);
            line.setFill(Color.web("#3a3a5a"));
            
            Text label = createText(String.format("%.1f", i), 20, y + 5, false);
            
            wrightMapPane.getChildren().addAll(line, label);
        }
    }
    
    private void addLabels(Pane wrightMapPane, double paneWidth, double paneHeight) {
        Text personsLabel = createText("Студенты", paneWidth / 2 - 100, 30, true);
        Text itemsLabel = createText("Задания", paneWidth / 2 + 80, 30, true);
        
        Text title = createText("Карта Райта (Item-Person Map)", paneWidth / 2 - 130, 15, true);
        title.setFill(Color.web("#e94560"));
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        Text info = createText("Распределение уровней подготовки и трудностей заданий", 
                paneWidth / 2 - 200, paneHeight - 15, false);
        info.setFill(Color.web("#a9b7c6"));
        
        wrightMapPane.getChildren().addAll(personsLabel, itemsLabel, title, info);
    }
    
    private int[] calculateHistogramBins(double[] values, double binSize) {
        int[] bins = new int[NUM_BINS];
        
        for (double value : values) {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                continue;
            }
            
            int bin = (int) ((value - MIN_VAL) / binSize);
            if (bin >= 0 && bin < NUM_BINS) {
                bins[bin]++;
            }
        }
        
        return bins;
    }
    
    private int findMaxCount(int[] personBins, int[] itemBins) {
        int maxCount = 0;
        for (int count : personBins) if (count > maxCount) maxCount = count;
        for (int count : itemBins) if (count > maxCount) maxCount = count;
        return maxCount > 0 ? maxCount : 1;
    }
    
    private void drawHistograms(Pane wrightMapPane, int[] personBins, int[] itemBins, 
                               double paneWidth, double center, double scale, 
                               double binSize, double barWidthScale) {
        for (int i = 0; i < NUM_BINS; i++) {
            double y = center - ((MIN_VAL + i * binSize + binSize / 2) * scale);
            double binHeight = scale * binSize * 0.8;

            if (personBins[i] > 0) {
                double barWidth = personBins[i] * barWidthScale;
                Rectangle rect = createHistogramBar(
                        paneWidth / 2 - 10 - barWidth, 
                        y - binHeight/2, 
                        barWidth, 
                        binHeight, 
                        "#4895EF");
                
                Text countText = createText(String.valueOf(personBins[i]), 
                        paneWidth / 2 - 15 - barWidth / 2, y + 5, false);
                wrightMapPane.getChildren().addAll(rect, countText);
            }

            if (itemBins[i] > 0) {
                double barWidth = itemBins[i] * barWidthScale;
                Rectangle rect = createHistogramBar(
                        paneWidth / 2 + 10, 
                        y - binHeight/2, 
                        barWidth, 
                        binHeight, 
                        "#F72585");
                
                Text countText = createText(String.valueOf(itemBins[i]), 
                        paneWidth / 2 + 10 + barWidth / 2, y + 5, false);
                wrightMapPane.getChildren().addAll(rect, countText);
            }
        }
    }
    
    private Rectangle createHistogramBar(double x, double y, double width, double height, String color) {
        Rectangle rect = new Rectangle(x, y, width, height);
        rect.setFill(Color.web(color));
        rect.setArcHeight(5);
        rect.setArcWidth(5);
        rect.setOpacity(0.8);
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#000000", 0.5));
        dropShadow.setRadius(3);
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        rect.setEffect(dropShadow);
        
        return rect;
    }
    
    private Text createText(String content, double x, double y, boolean isBold) {
        Text text = new Text(x, y, content);
        if (isBold) {
            text.setFont(Font.font("System", FontWeight.BOLD, 14));
        } else {
            text.setFont(Font.font("System", 13));
        }
        text.setFill(Color.WHITE);
        return text;
    }
} 