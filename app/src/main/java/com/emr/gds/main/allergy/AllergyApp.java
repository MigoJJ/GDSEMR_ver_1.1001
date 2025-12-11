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
        // Gogh-inspired palette: sunflower yellows with deep cobalt accents.
        String defaultCSS = """
                .root { -fx-font-family: 'Segoe UI', sans-serif; }
                .button {
                    -fx-background-radius: 6;
                    -fx-background-color: linear-gradient(to bottom, #ffcf56, #f4a300);
                    -fx-text-fill: #0f2a5f;
                    -fx-font-weight: bold;
                    -fx-border-color: #0f2a5f;
                    -fx-border-radius: 6;
                }
                .button:hover {
                    -fx-background-color: linear-gradient(to bottom, #ffd978, #f7b733);
                }
                .table-view {
                    -fx-table-cell-border-color: transparent;
                    -fx-background-color: linear-gradient(to bottom, #fef6dc, #f9e5a6);
                }
                .table-row-cell:filled:selected {
                    -fx-background-color: #8cc6ff;
                    -fx-text-fill: #0b1f47;
                }
                .label {
                    -fx-text-fill: #0b1f47;
                }
                .split-pane-divider {
                    -fx-background-color: #0f2a5f;
                }
                """;
        scene.getStylesheets().add("data:text/css," + defaultCSS);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
