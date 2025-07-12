package com.example.carrental;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {
    public static void switchScene(Stage stage, String fxmlPath) throws Exception {
        Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
        stage.setScene(new Scene(root));
        stage.show();
    }
}

