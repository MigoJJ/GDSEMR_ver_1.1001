package com.emr.gds.main.medication;

import com.emr.gds.util.StageSizing;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MedicationCategory extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/main/medication/launcher.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("EMR Medication Helper");
        primaryStage.setScene(new Scene(root));
        StageSizing.fitToScreen(primaryStage, 0.35, 0.85, 400, 600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
