package miroshka.rasch.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import miroshka.rasch.logic.RaschModel;
import miroshka.rasch.logic.RaschModelProcessor;
import miroshka.rasch.model.Item;
import miroshka.rasch.model.Person;
import miroshka.rasch.utils.AnimationManager;
import miroshka.rasch.utils.ExportManager;
import miroshka.rasch.utils.Logger;
import miroshka.rasch.utils.UpdateManager;
import miroshka.rasch.utils.VersionManager;
import miroshka.rasch.view.ExportDialog;
import miroshka.rasch.view.WrightMapRenderer;

public class MainController {
    @FXML
    private Label filePathLabel;
    
    @FXML
    private Button loadButton;

    @FXML
    private Button exportButton;
    
    @FXML
    private Button exportAllButton;
    
    @FXML
    private Label versionLabel;

    @FXML
    private Hyperlink githubLink;

    @FXML
    private TableView<Person> personAbilityTable;
    
    @FXML
    private TableView<Item> itemDifficultyTable;
    
    @FXML
    private Pane wrightMapPane;
    
    @FXML
    private Circle statusIndicator;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Label studentsCount;
    
    @FXML
    private Label itemsCount;
    
    @FXML
    private Label reliabilityValue;
    
    @FXML
    private Label fitValue;
    
    @FXML
    private StackPane mapLoadingOverlay;
    
    @FXML
    private StackPane studentsCard;
    
    @FXML
    private StackPane itemsCard;
    
    @FXML
    private StackPane reliabilityCard;
    
    @FXML
    private StackPane fitCard;

    private final RaschModelProcessor processor;
    private final WrightMapRenderer mapRenderer;
    private final DecimalFormat df;
    private final VersionManager versionManager;
    private final UpdateManager updateManager;
    
    public MainController() {
        this.processor = new RaschModelProcessor();
        this.mapRenderer = new WrightMapRenderer();
        this.df = new DecimalFormat("0.00");
        
        String currentVersion = getCurrentVersion();
        this.versionManager = new VersionManager(currentVersion);
        this.updateManager = new UpdateManager();
    }
    
    private String getCurrentVersion() {
        try {
            java.util.Properties properties = new java.util.Properties();
            try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    properties.load(input);
                    return properties.getProperty("version", "1.0.5");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load version from application.properties: " + e.getMessage());
        }
        return "1.0.5";
    }

    @FXML
    public void initialize() {
        initializePersonTable();
        initializeItemTable();
        setupWrightMapResizeListeners();
        initializeDashboardElements();
        
        try {
            ResourceBundle appBundle = ResourceBundle.getBundle("application");
            String version = appBundle.getString("version");
            if (versionLabel != null) {
                versionLabel.setText("v" + version);
            }
        } catch (Exception e) {
            Logger.error("Failed to load application version: " + e.getMessage());
        }
        
        checkForUpdatesOnStartup();
    }
    
    private void initializeDashboardElements() {
        if (studentsCount != null) studentsCount.setText("0");
        if (itemsCount != null) itemsCount.setText("0");
        if (reliabilityValue != null) reliabilityValue.setText("--");
        if (fitValue != null) fitValue.setText("--");
        
        if (versionLabel != null) versionLabel.setText("v" + getCurrentVersion());
        
        updateStatus("Готов к работе", StatusType.READY);
        
        if (progressBar != null) progressBar.setVisible(false);
        if (mapLoadingOverlay != null) mapLoadingOverlay.setVisible(false);
        
        animateInitialCards();
    }
    
    private void animateInitialCards() {
        Platform.runLater(() -> {
            if (studentsCard != null) {
                AnimationManager.resetTransforms(studentsCard);
                AnimationManager.fadeInAndSlideUp(studentsCard, Duration.millis(300));
            }
            
            Platform.runLater(() -> {
                if (itemsCard != null) {
                    AnimationManager.resetTransforms(itemsCard);
                    AnimationManager.fadeInAndSlideUp(itemsCard, Duration.millis(400));
                }
            });
            
            Platform.runLater(() -> {
                if (reliabilityCard != null) {
                    AnimationManager.resetTransforms(reliabilityCard);
                    AnimationManager.fadeInAndSlideUp(reliabilityCard, Duration.millis(500));
                }
            });
            
            Platform.runLater(() -> {
                if (fitCard != null) {
                    AnimationManager.resetTransforms(fitCard);
                    AnimationManager.fadeInAndSlideUp(fitCard, Duration.millis(600));
                }
            });
        });
    }
    
    private enum StatusType {
        READY("#4caf50"),
        LOADING("#ff9800"),
        ERROR("#f44336"),
        SUCCESS("#4facfe");
        
        private final String color;
        
        StatusType(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    private void updateStatus(String message, StatusType type) {
        Platform.runLater(() -> {
            if (filePathLabel != null) {
                filePathLabel.setText(message);
            }
            if (statusIndicator != null) {
                statusIndicator.setFill(Color.web(type.getColor()));
                
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web(type.getColor()));
                glow.setWidth(16);
                glow.setHeight(16);
                statusIndicator.setEffect(glow);
            }
        });
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
        animateButtonClick(loadButton, this::performLoadExcel);
    }
    
    private void performLoadExcel() {
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
            updateStatus("Загрузка файла: " + selectedFile.getName() + "...", StatusType.LOADING);
            showProgress(true);
            
            new Thread(() -> {
                try {
                    double[][] data = processor.readDataFromFile(selectedFile);
                    if(data.length > 0 && data[0].length > 0) {
                        Platform.runLater(() -> updateStatus("Выполняется анализ данных...", StatusType.LOADING));
                        
                        RaschModel.RaschResult result = processor.calculateRaschModel(data);
                        if (result.isEmpty()) {
                            Platform.runLater(() -> {
                                showError("Ошибка вычислений", "Не удалось рассчитать параметры модели Раша. Проверьте формат входных данных.");
                                updateStatus("Ошибка обработки: " + selectedFile.getName(), StatusType.ERROR);
                                showProgress(false);
                            });
                            return;
                        }
                        
                        Platform.runLater(() -> {
                            updateUIWithResults(result);
                            updateStatus("Файл: " + selectedFile.getName(), StatusType.SUCCESS);
                            showProgress(false);
                            showSuccessAnimation();
                        });
                        
                        Logger.log("Расчет завершен. Интерфейс обновлен.");
                    } else {
                        Platform.runLater(() -> {
                            showError("Ошибка данных", "Не удалось прочитать данные из файла или файл пуст.");
                            updateStatus("Ошибка: файл пуст или не содержит данных", StatusType.ERROR);
                            showProgress(false);
                        });
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showError("Ошибка чтения файла", e.getMessage());
                        updateStatus("Ошибка чтения: " + e.getMessage(), StatusType.ERROR);
                        showProgress(false);
                        e.printStackTrace();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Непредвиденная ошибка", "Произошла ошибка при обработке файла: " + e.getMessage());
                        updateStatus("Ошибка: " + e.getMessage(), StatusType.ERROR);
                        showProgress(false);
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
    
    private void showProgress(boolean show) {
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setVisible(show);
                if (show) {
                    progressBar.setProgress(-1);
                }
            }
        });
    }
    
    private void showMapLoading(boolean show) {
        Platform.runLater(() -> {
            if (mapLoadingOverlay != null) {
                mapLoadingOverlay.setVisible(show);
            }
        });
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
        animateTableUpdate(personAbilityTable, () -> personAbilityTable.setItems(personData));

        ObservableList<Item> itemData = createItemsList(result);
        animateTableUpdate(itemDifficultyTable, () -> itemDifficultyTable.setItems(itemData));

        updateDashboardStats(result, personData.size(), itemData.size());
        
        showMapLoading(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    mapRenderer.drawWrightMap(wrightMapPane, result);
                    showMapLoading(false);
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> showMapLoading(false));
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void updateDashboardStats(RaschModel.RaschResult result, int personCount, int itemCount) {
        animateStatsUpdate(personCount, itemCount, result);
    }
    
    private void animateStatsUpdate(int personCount, int itemCount, RaschModel.RaschResult result) {
        Platform.runLater(() -> {
            if (studentsCard != null) {
                AnimationManager.pulse(studentsCard)
                    .thenRun(() -> Platform.runLater(() -> {
                        if (studentsCount != null) {
                            studentsCount.setText(String.valueOf(personCount));
                        }
                    }));
            }
            
            if (itemsCard != null) {
                AnimationManager.pulse(itemsCard)
                    .thenRun(() -> Platform.runLater(() -> {
                        if (itemsCount != null) {
                            itemsCount.setText(String.valueOf(itemCount));
                        }
                    }));
            }
            
            if (reliabilityCard != null) {
                AnimationManager.pulse(reliabilityCard)
                    .thenRun(() -> Platform.runLater(() -> {
                        if (reliabilityValue != null) {
                            double reliability = calculateReliability(result);
                            reliabilityValue.setText(df.format(reliability));
                        }
                    }));
            }
            
            if (fitCard != null) {
                AnimationManager.pulse(fitCard)
                    .thenRun(() -> Platform.runLater(() -> {
                        if (fitValue != null) {
                            double avgFit = calculateAverageFit(result);
                            fitValue.setText(df.format(avgFit));
                        }
                    }));
            }
        });
    }
    
    private void animateTableUpdate(TableView<?> table, Runnable updateAction) {
        Platform.runLater(() -> {
            if (table != null) {
                AnimationManager.fadeOut(table, Duration.millis(200))
                    .thenRun(() -> Platform.runLater(() -> {
                        updateAction.run();
                        AnimationManager.fadeIn(table, Duration.millis(300));
                    }));
            }
        });
    }
    
    private void animateButtonClick(javafx.scene.Node button, Runnable action) {
        if (button != null) {
            AnimationManager.scaleOut(button, Duration.millis(100))
                .thenCompose(v -> AnimationManager.scaleIn(button, Duration.millis(100)))
                .thenRun(() -> Platform.runLater(action));
        } else {
            action.run();
        }
    }
    
    private double calculateReliability(RaschModel.RaschResult result) {
        double[] abilities = result.getPersonAbilities();
        if (abilities.length < 2) return 0.0;
        
        double mean = java.util.Arrays.stream(abilities).average().orElse(0.0);
        double variance = java.util.Arrays.stream(abilities)
                .map(x -> Math.pow(x - mean, 2))
                .average().orElse(0.0);
        
        double reliability = Math.min(0.99, Math.max(0.0, variance / (variance + 1.0)));
        return reliability;
    }
    
    private double calculateAverageFit(RaschModel.RaschResult result) {
        double[] personInfit = result.getPersonInfitMNSQ();
        double[] itemInfit = result.getItemInfitMNSQ();
        
        double totalFit = 0.0;
        int count = 0;
        
        for (double fit : personInfit) {
            if (!Double.isNaN(fit) && !Double.isInfinite(fit)) {
                totalFit += fit;
                count++;
            }
        }
        
        for (double fit : itemInfit) {
            if (!Double.isNaN(fit) && !Double.isInfinite(fit)) {
                totalFit += fit;
                count++;
            }
        }
        
        return count > 0 ? totalFit / count : 1.0;
    }
    
    private ObservableList<Person> createPersonsList(RaschModel.RaschResult result) {
        double[] abilities = result.getPersonAbilities();
        double[] infitMNSQs = result.getPersonInfitMNSQ();
        double[] outfitMNSQs = result.getPersonOutfitMNSQ();
        double[] infitZSTDs = result.getPersonInfitZSTD();
        double[] outfitZSTDs = result.getPersonOutfitZSTD();

        ObservableList<Person> persons = FXCollections.observableArrayList();
        for (int i = 0; i < abilities.length; i++) {
            double ability = Double.isNaN(abilities[i]) || Double.isInfinite(abilities[i]) ? 0.0 : abilities[i];
            
            Person person = Person.builder(i + 1, ability)
                .withInfitMNSQ(sanitizeValue(infitMNSQs[i]))
                .withOutfitMNSQ(sanitizeValue(outfitMNSQs[i]))
                .withInfitZSTD(sanitizeValue(infitZSTDs[i]))
                .withOutfitZSTD(sanitizeValue(outfitZSTDs[i]))
                .build();
            
            persons.add(person);
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
            double difficulty = Double.isNaN(difficulties[i]) || Double.isInfinite(difficulties[i]) ? 0.0 : difficulties[i];
            
            Item item = Item.builder(i + 1, difficulty)
                .withInfitMNSQ(sanitizeValue(infitMNSQs[i]))
                .withOutfitMNSQ(sanitizeValue(outfitMNSQs[i]))
                .withInfitZSTD(sanitizeValue(infitZSTDs[i]))
                .withOutfitZSTD(sanitizeValue(outfitZSTDs[i]))
                .build();
            
            items.add(item);
        }
        return items;
    }
    
    private double sanitizeValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 1.0;
        }
        return value;
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

    @FXML
    private void handleExportToWord(ActionEvent event) {
        animateButtonClick(exportButton, this::performExportToWord);
    }
    
    private void performExportToWord() {
        if (personAbilityTable.getItems() == null || personAbilityTable.getItems().isEmpty()) {
            showError("Нет данных для экспорта", "Сначала загрузите данные из файла.");
            return;
        }

        ExportDialog.showFormatSelectionDialog().ifPresent(format -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить данные студентов");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.getDescription(), "*." + format.getFileExtension())
            );
            fileChooser.setInitialFileName("persons_export." + format.getFileExtension());
            
            Stage stage = (Stage) exportButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            if (selectedFile != null) {
                try {
                    RaschModel.RaschResult result = extractResultFromTables();
                    ExportManager.exportCompleteResults(result, selectedFile, format);
                    
                    showSuccess("Экспорт завершен", "Данные студентов успешно экспортированы в " + format.getDescription() + ".");
                    openFileIfSupported(selectedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Ошибка экспорта", "Не удалось экспортировать данные: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleExportAllData(ActionEvent event) {
        animateButtonClick(exportAllButton, this::performExportAllData);
    }
    
    private void performExportAllData() {
        if (personAbilityTable.getItems() == null || personAbilityTable.getItems().isEmpty()) {
            showError("Нет данных для экспорта", "Сначала загрузите данные из файла.");
            return;
        }

        ExportDialog.showFormatSelectionDialog().ifPresent(format -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить файл анализа");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.getDisplayName(), format.getFilePattern())
            );
            fileChooser.setInitialFileName(ExportManager.getDefaultFileName(format));
            
            Stage stage = (Stage) exportAllButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            if (selectedFile != null) {
                try {
                    RaschModel.RaschResult result = extractResultFromTables();
                    ExportManager.exportCompleteResults(result, selectedFile, format);
                    
                    showSuccess("Экспорт завершен", 
                        "Все данные успешно экспортированы в " + format.getDisplayName().toLowerCase() + ".");
                    
                    openFileIfSupported(selectedFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Ошибка экспорта", "Не удалось экспортировать данные: " + e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Непредвиденная ошибка", "Произошла ошибка при экспорте: " + e.getMessage());
                }
            }
        });
    }
    
    private void openFileIfSupported(File file) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (Exception e) {
                    Logger.error("Не удалось открыть файл автоматически: " + e.getMessage());
                }
            }).start();
        }
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (stage != null && stage.getScene() != null) {
            stage.getScene().getStylesheets().add(getClass().getResource("/styles/modern-style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("modern-alert");
        }
        
        alert.showAndWait();
    }
    
    private void checkForUpdatesOnStartup() {
        if (!updateManager.isUpdateSupported()) {
            return;
        }
        
        updateManager.cleanupOldUpdates();
        
        versionManager.checkForUpdatesAsync()
            .thenAccept(updateInfo -> {
                Platform.runLater(() -> {
                    if (updateInfo.isUpdateAvailable()) {
                        showUpdateAvailableNotification(updateInfo);
                    }
                });
            })
            .exceptionally(throwable -> {
                Logger.error("Failed to check for updates on startup: " + throwable.getMessage());
                return null;
            });
    }
    
    private void showUpdateAvailableNotification(VersionManager.UpdateInfo updateInfo) {
        if (updateButton != null) {
            updateButton.setVisible(true);
            updateButton.setText("🔄 Обновление " + updateInfo.getLatestVersion());
            AnimationManager.fadeInAndSlideUp(updateButton, Duration.millis(500));
        }
    }
    
    @FXML
    private void handleCheckUpdate(ActionEvent event) {
        animateButtonClick(updateButton, this::performCheckUpdate);
    }
    
    private void performCheckUpdate() {
        if (!updateManager.isUpdateSupported()) {
            showError("Обновление не поддерживается", 
                "Автообновление недоступно. Скачайте последнюю версию с GitHub.");
            return;
        }
        
        updateStatus("Проверка обновлений...", StatusType.LOADING);
        
        versionManager.checkForUpdatesAsync()
            .thenAccept(updateInfo -> {
                Platform.runLater(() -> {
                    updateStatus("Готов к работе", StatusType.READY);
                    handleUpdateCheckResult(updateInfo);
                });
            })
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    updateStatus("Ошибка проверки обновлений", StatusType.ERROR);
                    showError("Ошибка проверки обновлений", 
                        "Не удалось проверить обновления: " + throwable.getMessage());
                });
                return null;
            });
    }
    
    private void handleUpdateCheckResult(VersionManager.UpdateInfo updateInfo) {
        if (!updateInfo.isUpdateAvailable()) {
            showSuccess("Обновления", "У вас установлена последняя версия " + updateInfo.getCurrentVersion());
            return;
        }
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Доступно обновление");
        alert.setHeaderText("Новая версия " + updateInfo.getLatestVersion());
        alert.setContentText(
            "Текущая версия: " + updateInfo.getCurrentVersion() + "\n" +
            "Новая версия: " + updateInfo.getLatestVersion() + "\n\n" +
            "Хотите скачать и установить обновление?"
        );
        
        javafx.scene.control.ButtonType updateBtn = new javafx.scene.control.ButtonType("Обновить");
        javafx.scene.control.ButtonType releaseNotesBtn = new javafx.scene.control.ButtonType("Что нового");
        javafx.scene.control.ButtonType cancelBtn = new javafx.scene.control.ButtonType("Отмена", 
            javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(updateBtn, releaseNotesBtn, cancelBtn);
        
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (stage != null && stage.getScene() != null) {
            stage.getScene().getStylesheets().add(getClass().getResource("/styles/modern-style.css").toExternalForm());
        }
        
        alert.showAndWait().ifPresent(response -> {
            if (response == updateBtn) {
                startUpdateProcess(updateInfo);
            } else if (response == releaseNotesBtn) {
                updateInfo.getReleaseNotesUrl().ifPresent(url -> {
                    try {
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
                        }
                    } catch (Exception e) {
                        showError("Ошибка", "Не удалось открыть страницу с описанием изменений.");
                    }
                });
            }
        });
    }
    
    private void startUpdateProcess(VersionManager.UpdateInfo updateInfo) {
        String downloadUrl = updateInfo.getDownloadUrl().orElse(null);
        if (downloadUrl == null) {
            showError("Ошибка обновления", "Не удалось найти ссылку для скачивания обновления.");
            return;
        }
        
        updateStatus("Скачивание обновления...", StatusType.LOADING);
        showProgress(true);
        
        updateManager.downloadUpdateAsync(downloadUrl, progress -> {
            Platform.runLater(() -> {
                if (progressBar != null) {
                    progressBar.setProgress(progress / 100.0);
                }
            });
        })
        .thenAccept(updateFile -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("Установка обновления...", StatusType.LOADING);
                
                try {
                    updateManager.installUpdateAndRestart(updateFile);
                } catch (UpdateManager.UpdateException e) {
                    updateStatus("Ошибка установки обновления", StatusType.ERROR);
                    showError("Ошибка установки", "Не удалось установить обновление: " + e.getMessage());
                }
            });
        })
        .exceptionally(throwable -> {
            Platform.runLater(() -> {
                showProgress(false);
                updateStatus("Ошибка скачивания обновления", StatusType.ERROR);
                showError("Ошибка скачивания", "Не удалось скачать обновление: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    @FXML
    private void handleExportPersons(ActionEvent event) {
        performExportToWord();
    }
    
    @FXML
    private void handleExportItems(ActionEvent event) {
        performExportItems();
    }
    
    private void performExportItems() {
        if (itemDifficultyTable.getItems() == null || itemDifficultyTable.getItems().isEmpty()) {
            showError("Нет данных для экспорта", "Сначала загрузите данные из файла.");
            return;
        }

        ExportDialog.showFormatSelectionDialog().ifPresent(format -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить данные заданий");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(format.getDescription(), "*." + format.getFileExtension())
            );
            fileChooser.setInitialFileName("items_export." + format.getFileExtension());
            
            Stage stage = (Stage) exportButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);
            
            if (selectedFile != null) {
                try {
                    RaschModel.RaschResult result = extractResultFromTables();
                    ExportManager.exportCompleteResults(result, selectedFile, format);
                    
                    showSuccess("Экспорт завершен", "Данные заданий успешно экспортированы в " + format.getDescription() + ".");
                    openFileIfSupported(selectedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Ошибка экспорта", "Не удалось экспортировать данные: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleExportMap(ActionEvent event) {
        performExportMap();
    }
    
    private void performExportMap() {
        showSuccess("Экспорт карты", "Функция экспорта карты Райта будет реализована в следующих версиях.");
    }
} 