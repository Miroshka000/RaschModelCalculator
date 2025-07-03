package miroshka.rasch.controller;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import miroshka.rasch.logic.RaschModel;
import miroshka.rasch.logic.RaschModelProcessor;
import miroshka.rasch.model.Item;
import miroshka.rasch.model.Person;
import miroshka.rasch.view.WrightMapRenderer;

import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;

public class MainController {
    @FXML
    private Label filePathLabel;
    
    @FXML
    private Button loadButton;

    @FXML
    private Hyperlink githubLink;

    @FXML
    private TableView<Person> personAbilityTable;
    
    @FXML
    private TableView<Item> itemDifficultyTable;
    
    @FXML
    private Pane wrightMapPane;

    private final RaschModelProcessor processor;
    private final WrightMapRenderer mapRenderer;
    private final DecimalFormat df;
    
    public MainController() {
        this.processor = new RaschModelProcessor();
        this.mapRenderer = new WrightMapRenderer();
        this.df = new DecimalFormat("0.00");
    }

    @FXML
    public void initialize() {
        initializePersonTable();
        initializeItemTable();
        setupWrightMapResizeListeners();
    }

    private void initializePersonTable() {
        TableColumn<Person, Integer> personIdCol = new TableColumn<>("ID Студента");
        personIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        personIdCol.setPrefWidth(100);
        personIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Person, Double> personAbilityCol = createFormattedDoubleColumn("Уровень подготовки", "ability", 200);
        TableColumn<Person, Double> personInfitMNSQCol = createFormattedDoubleColumn("Infit MNSQ", "infitMNSQ", 120);
        TableColumn<Person, Double> personOutfitMNSQCol = createFormattedDoubleColumn("Outfit MNSQ", "outfitMNSQ", 120);
        TableColumn<Person, Double> personInfitZSTDCol = createFormattedDoubleColumn("Infit ZSTD", "infitZSTD", 120);
        TableColumn<Person, Double> personOutfitZSTDCol = createFormattedDoubleColumn("Outfit ZSTD", "outfitZSTD", 120);

        personAbilityTable.getColumns().addAll(personIdCol, personAbilityCol, personInfitMNSQCol, personOutfitMNSQCol, personInfitZSTDCol, personOutfitZSTDCol);
    }

    private void initializeItemTable() {
        TableColumn<Item, Integer> itemIdCol = new TableColumn<>("ID Задания");
        itemIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        itemIdCol.setPrefWidth(100);
        itemIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Item, Double> itemDifficultyCol = createFormattedDoubleColumn("Трудность", "difficulty", 200);
        TableColumn<Item, Double> itemInfitMNSQCol = createFormattedDoubleColumn("Infit MNSQ", "infitMNSQ", 120);
        TableColumn<Item, Double> itemOutfitMNSQCol = createFormattedDoubleColumn("Outfit MNSQ", "outfitMNSQ", 120);
        TableColumn<Item, Double> itemInfitZSTDCol = createFormattedDoubleColumn("Infit ZSTD", "infitZSTD", 120);
        TableColumn<Item, Double> itemOutfitZSTDCol = createFormattedDoubleColumn("Outfit ZSTD", "outfitZSTD", 120);

        itemDifficultyTable.getColumns().addAll(itemIdCol, itemDifficultyCol, itemInfitMNSQCol, itemOutfitMNSQCol, itemInfitZSTDCol, itemOutfitZSTDCol);
    }

    private <T> TableColumn<T, Double> createFormattedDoubleColumn(String title, String property, double width) {
        TableColumn<T, Double> column = new TableColumn<>();
        Label headerLabel = new Label(title);
        column.setGraphic(headerLabel);

        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);

        String tooltipText = getTooltipTextFor(title);
        if (!tooltipText.isEmpty()) {
            Tooltip headerTooltip = new Tooltip(tooltipText);
            headerTooltip.setWrapText(true);
            headerTooltip.setStyle("-fx-font-size: 14px; -fx-background-color: #2A3B4D; -fx-text-fill: white; -fx-border-color: #4895EF; -fx-border-width: 1;");
            headerLabel.setTooltip(headerTooltip);
        }

        column.setCellFactory(col -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();

            {
                String cellTooltipText = getTooltipTextFor(title);
                if (!cellTooltipText.isEmpty()) {
                    tooltip.setText(cellTooltipText);
                    tooltip.setWrapText(true);
                    tooltip.setStyle("-fx-font-size: 14px; -fx-background-color: #2A3B4D; -fx-text-fill: white; -fx-border-color: #4895EF; -fx-border-width: 1;");
                }
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || Double.isNaN(item) || Double.isInfinite(item)) {
                    setText(null);
                    setTooltip(null);
                    setStyle("");
                } else {
                    setText(df.format(item));
                    setAlignment(Pos.CENTER);
                    if (!tooltip.getText().isEmpty()) {
                        setTooltip(tooltip);
                    }

                    if (title.contains("MNSQ")) {
                        if (item < 0.5 || item > 1.5) {
                            setStyle("-fx-background-color: #F72585; -fx-text-fill: white; -fx-alignment: CENTER;");
                        } else {
                            setStyle("-fx-alignment: CENTER;");
                        }
                    } else if (title.contains("ZSTD")) {
                        if (item < -2.0 || item > 2.0) {
                            setStyle("-fx-background-color: #F72585; -fx-text-fill: white; -fx-alignment: CENTER;");
                        } else {
                            setStyle("-fx-alignment: CENTER;");
                        }
                    } else {
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });
        return column;
    }

    private String getTooltipTextFor(String title) {
        if (title.contains("Infit MNSQ")) {
            return "Infit (Information-Weighted Fit) Mean-Square.\nПоказывает соответствие данных модели с фокусом на ответах, близких к уровню испытуемого/сложности задания.\nИдеал: 1.0. Норма: 0.5-1.5.";
        }
        if (title.contains("Outfit MNSQ")) {
            return "Outfit (Outlier-Sensitive Fit) Mean-Square.\nПоказывает соответствие данных, чувствителен к неожиданным 'выбросам' (например, случайным угадываниям).\nИдеал: 1.0. Норма: 0.5-1.5.";
        }
        if (title.contains("Infit ZSTD")) {
            return "Infit ZSTD (Standardized).\nСтатистическая значимость Infit MNSQ.\nПоказывает, насколько вероятно данное значение Infit, если данные идеально подходят модели.\nИдеал: 0.0. Норма: от -2.0 до +2.0.";
        }
        if (title.contains("Outfit ZSTD")) {
            return "Outfit ZSTD (Standardized).\nСтатистическая значимость Outfit MNSQ.\nПоказывает, насколько вероятно данное значение Outfit, если данные идеально подходят модели.\nИдеал: 0.0. Норма: от -2.0 до +2.0.";
        }
        if (title.contains("Уровень подготовки")) {
            return "Логит-мера уровня подготовки испытуемого.\nЧем выше значение, тем выше уровень подготовки.";
        }
        if (title.contains("Трудность")) {
            return "Логит-мера трудности задания.\nЧем выше значение, тем сложнее задание.";
        }
        return "";
    }

    private void setupWrightMapResizeListeners() {
        wrightMapPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (personAbilityTable.getItems() != null && !personAbilityTable.getItems().isEmpty()) {
                RaschModel.RaschResult result = extractResultFromTables();
                mapRenderer.drawWrightMap(wrightMapPane, result);
            }
        });
        wrightMapPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (personAbilityTable.getItems() != null && !personAbilityTable.getItems().isEmpty()) {
                RaschModel.RaschResult result = extractResultFromTables();
                mapRenderer.drawWrightMap(wrightMapPane, result);
            }
        });
    }

    private RaschModel.RaschResult extractResultFromTables() {
        if (personAbilityTable.getItems() == null || personAbilityTable.getItems().isEmpty() ||
            itemDifficultyTable.getItems() == null || itemDifficultyTable.getItems().isEmpty()) {
            return new RaschModel.RaschResult(new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0], new double[0]);
        }

        double[] personAbilities = personAbilityTable.getItems().stream()
                .mapToDouble(Person::getAbility).toArray();
        double[] itemDifficulties = itemDifficultyTable.getItems().stream()
                .mapToDouble(Item::getDifficulty).toArray();
        
        double[] pInfitMNSQ = personAbilityTable.getItems().stream().mapToDouble(Person::getInfitMNSQ).toArray();
        double[] pOutfitMNSQ = personAbilityTable.getItems().stream().mapToDouble(Person::getOutfitMNSQ).toArray();
        double[] pInfitZSTD = personAbilityTable.getItems().stream().mapToDouble(Person::getInfitZSTD).toArray();
        double[] pOutfitZSTD = personAbilityTable.getItems().stream().mapToDouble(Person::getOutfitZSTD).toArray();

        double[] iInfitMNSQ = itemDifficultyTable.getItems().stream().mapToDouble(Item::getInfitMNSQ).toArray();
        double[] iOutfitMNSQ = itemDifficultyTable.getItems().stream().mapToDouble(Item::getOutfitMNSQ).toArray();
        double[] iInfitZSTD = itemDifficultyTable.getItems().stream().mapToDouble(Item::getInfitZSTD).toArray();
        double[] iOutfitZSTD = itemDifficultyTable.getItems().stream().mapToDouble(Item::getOutfitZSTD).toArray();

        return new RaschModel.RaschResult(personAbilities, itemDifficulties, pInfitMNSQ, pOutfitMNSQ, pInfitZSTD, pOutfitZSTD, iInfitMNSQ, iOutfitMNSQ, iInfitZSTD, iOutfitZSTD);
    }

    @FXML
    private void handleLoadExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с данными");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Все поддерживаемые форматы", "*.xlsx", "*.xls", "*.csv"),
                new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx", "*.xls"),
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );
        Stage stage = (Stage) filePathLabel.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            updateFilePath("Загрузка файла: " + selectedFile.getName() + "...");
            
            new Thread(() -> {
                try {
                    double[][] data = processor.readDataFromFile(selectedFile);
                    if(data.length > 0 && data[0].length > 0) {
                        RaschModel.RaschResult result = processor.calculateRaschModel(data);
                        if (result.isEmpty()) {
                            Platform.runLater(() -> {
                                showError("Ошибка вычислений", "Не удалось рассчитать параметры модели Раша. Проверьте формат входных данных.");
                                updateFilePath("Ошибка обработки: " + selectedFile.getName());
                            });
                            return;
                        }
                        
                        Platform.runLater(() -> {
                            updateUIWithResults(result);
                            updateFilePath("Файл: " + selectedFile.getName());
                            showSuccessAnimation();
                        });
                        
                        System.out.println("Расчет завершен. Интерфейс обновлен.");
                    } else {
                        Platform.runLater(() -> {
                            showError("Ошибка данных", "Не удалось прочитать данные из файла или файл пуст.");
                            updateFilePath("Ошибка: файл пуст или не содержит данных");
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showError("Ошибка чтения файла", e.getMessage());
                        updateFilePath("Ошибка чтения: " + e.getMessage());
                        e.printStackTrace();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Непредвиденная ошибка", "Произошла ошибка при обработке файла: " + e.getMessage());
                        updateFilePath("Ошибка: " + e.getMessage());
                        e.printStackTrace();
                    });
                }
            }).start();
        }
    }
    
    private void showSuccessAnimation() {
        loadButton.setDisable(true);
        
        Rectangle successIndicator = new Rectangle(loadButton.getWidth() - 20, 4);
        successIndicator.setFill(Color.web("#4CAF50"));
        successIndicator.setArcWidth(4);
        successIndicator.setArcHeight(4);
        successIndicator.setTranslateY(loadButton.getHeight() - 4);
        successIndicator.setTranslateX(10);
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#4CAF50"));
        glow.setWidth(10);
        glow.setHeight(10);
        successIndicator.setEffect(glow);
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), successIndicator);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            loadButton.setDisable(false);
        });
        
        fadeOut.play();
    }
    
    private void updateFilePath(String text) {
        filePathLabel.setText(text);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getScene().getStylesheets().add(getClass().getResource("/styles/modern-style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("modern-alert");
        
        alert.showAndWait();
    }

    private void updateUIWithResults(RaschModel.RaschResult result) {
        if (result == null || result.isEmpty()) {
            showError("Ошибка результатов", "Результаты расчета пустые или некорректные.");
            return;
        }

        ObservableList<Person> personData = createPersonsList(result);
        personAbilityTable.setItems(personData);

        ObservableList<Item> itemData = createItemsList(result);
        itemDifficultyTable.setItems(itemData);

        mapRenderer.drawWrightMap(wrightMapPane, result);
    }
    
    private ObservableList<Person> createPersonsList(RaschModel.RaschResult result) {
        double[] abilities = result.getPersonAbilities();
        double[] infitMNSQs = result.getPersonInfitMNSQ();
        double[] outfitMNSQs = result.getPersonOutfitMNSQ();
        double[] infitZSTDs = result.getPersonInfitZSTD();
        double[] outfitZSTDs = result.getPersonOutfitZSTD();

        ObservableList<Person> persons = FXCollections.observableArrayList();
        for (int i = 0; i < abilities.length; i++) {
            double ability = abilities[i];
            if (Double.isNaN(ability) || Double.isInfinite(ability)) {
                ability = 0.0;
            }
            Person p = new Person(i + 1, ability);
            p.setInfitMNSQ(infitMNSQs[i]);
            p.setOutfitMNSQ(outfitMNSQs[i]);
            p.setInfitZSTD(infitZSTDs[i]);
            p.setOutfitZSTD(outfitZSTDs[i]);
            persons.add(p);
        }
        return persons;
    }
    
    private ObservableList<Item> createItemsList(RaschModel.RaschResult result) {
        double[] difficulties = result.getItemDifficulties();
        double[] infitMNSQs = result.getItemInfitMNSQ();
        double[] outfitMNSQs = result.getItemOutfitMNSQ();
        double[] infitZSTDs = result.getItemInfitZSTD();
        double[] outfitZSTDs = result.getItemOutfitZSTD();

        ObservableList<Item> items = FXCollections.observableArrayList();
        for (int i = 0; i < difficulties.length; i++) {
            double difficulty = difficulties[i];
            if (Double.isNaN(difficulty) || Double.isInfinite(difficulty)) {
                difficulty = 0.0;
            }
            Item item = new Item(i + 1, difficulty);
            item.setInfitMNSQ(infitMNSQs[i]);
            item.setOutfitMNSQ(outfitMNSQs[i]);
            item.setInfitZSTD(infitZSTDs[i]);
            item.setOutfitZSTD(outfitZSTDs[i]);
            items.add(item);
        }
        return items;
    }

    @FXML
    private void handleGitHubLink(ActionEvent event) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://github.com/Miroshka000"));
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showError("Ошибка браузера", "Не удалось открыть ссылку в браузере.");
        }
        event.consume();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        Platform.exit();
    }
} 