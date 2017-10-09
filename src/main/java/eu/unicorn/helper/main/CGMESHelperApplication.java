package eu.unicorn.helper.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CGMESHelperApplication extends Application {

    Logger logger = LoggerFactory.getLogger(CGMESHelperApplication.class);

    public CGMESHelperApplication() throws IOException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting application with UTC time settings.");
        System.setProperty("user.timezone", "UTC");
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        primaryStage.setTitle("CGMES Model Helper");
        primaryStage.setScene(new Scene(root, 1024, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
