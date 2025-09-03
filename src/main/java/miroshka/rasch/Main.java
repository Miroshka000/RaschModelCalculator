package miroshka.rasch;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int WINDOW_WIDTH = 1224;
    private static final int WINDOW_HEIGHT = 968;
    private static final int MIN_WINDOW_WIDTH = 1200;
    private static final int MIN_WINDOW_HEIGHT = 900;
    private static final String APP_TITLE = "Rasch Model Calculator";
    private static final String FXML_PATH = "/views/main.fxml";
    private static final String CSS_PATH = "/styles/modern-style.css";
    private static final String ICON_PATH = "/icon.png";

    @Override
    public void start(Stage primaryStage) {
        try {
            Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
            
            URL fxmlUrl = Main.class.getResource(FXML_PATH);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: " + FXML_PATH);
            }
            Parent root = FXMLLoader.load(fxmlUrl);

            configureStage(primaryStage, root);
            loadIcon(primaryStage);

            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private void configureStage(Stage stage, Parent root) {
        stage.setTitle(APP_TITLE);
        
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        URL cssUrl = Main.class.getResource(CSS_PATH);
        if (cssUrl != null) {
            String cssPath = cssUrl.toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("CSS loaded successfully: " + cssPath);
        } else {
            System.out.println("CSS file not found: " + CSS_PATH);
        }
        
        stage.setScene(scene);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
    }
    
    private void loadIcon(Stage stage) {
        try (InputStream iconStream = Main.class.getResourceAsStream(ICON_PATH)) {
            if (iconStream == null) {
                System.out.println("Icon resource not found: " + ICON_PATH);
                return;
            }
            Image icon = new Image(iconStream);
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Error loading icon: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 