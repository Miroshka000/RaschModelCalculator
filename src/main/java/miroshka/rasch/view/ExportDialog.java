package miroshka.rasch.view;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import miroshka.rasch.model.ExportFormat;

public final class ExportDialog extends Dialog<ExportFormat> {
    
    private final ComboBox<ExportFormat> formatComboBox;
    
    public ExportDialog() {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setTitle("Выбор формата экспорта");
        setHeaderText("Выберите формат для экспорта данных анализа");
        
        formatComboBox = new ComboBox<>(FXCollections.observableList(ExportFormat.getAllFormats()));
        formatComboBox.setValue(ExportFormat.WORD);
        formatComboBox.setMaxWidth(Double.MAX_VALUE);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        grid.add(new Label("Формат экспорта:"), 0, 0);
        grid.add(formatComboBox, 1, 0);
        
        Label descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(300);
        descriptionLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        
        formatComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                descriptionLabel.setText(newVal.getDescription());
            }
        });
        
        if (formatComboBox.getValue() != null) {
            descriptionLabel.setText(formatComboBox.getValue().getDescription());
        }
        
        grid.add(descriptionLabel, 0, 1, 2, 1);
        
        getDialogPane().setContent(grid);
        
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return formatComboBox.getValue();
            }
            return null;
        });
    }
    
    public static Optional<ExportFormat> showFormatSelectionDialog() {
        ExportDialog dialog = new ExportDialog();
        return dialog.showAndWait();
    }
}
