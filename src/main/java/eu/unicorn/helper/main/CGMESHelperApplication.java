package eu.unicorn.helper.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CGMESHelperApplication extends Application {

    public CGMESHelperApplication() throws IOException {
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));
        primaryStage.setTitle("CGMES HelperModel Helper");
        primaryStage.setScene(new Scene(root, 1024, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
