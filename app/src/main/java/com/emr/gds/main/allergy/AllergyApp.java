package com.emr.gds.main.allergy;

import com.emr.gds.main.allergy.controller.AllergyController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AllergyApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Allergy History Recorder Pro - v2.0");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(900);

        AllergyController controller = new AllergyController();
        Scene scene = new Scene(controller.getView());

        // A basic default stylesheet
        String defaultCSS = ".root { -fx-font-family: 'Segoe UI', sans-serif; } .button { -fx-background-radius: 6; } .table-view { -fx-table-cell-border-color: transparent; }";
        scene.getStylesheets().add("data:text/css," + defaultCSS);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
