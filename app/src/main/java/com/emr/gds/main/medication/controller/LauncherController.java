package com.emr.gds.main.medication.controller;

import com.emr.gds.main.medication.db.DatabaseManager;
import com.emr.gds.util.StageSizing;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LauncherController {

    @FXML private VBox categoryContainer;

    private final DatabaseManager dbManager = new DatabaseManager();

    @FXML
    public void initialize() {
        dbManager.createTables();
        dbManager.ensureSeedData();

        var categories = dbManager.getOrderedCategories();
        String btnStyle = """
            -fx-background-color: #dce4ff;
            -fx-border-color: #b0c4de;
            -fx-border-width: 1;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-padding: 15;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            """;

        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle(btnStyle);
            btn.setOnAction(e -> openMainView(cat));
            categoryContainer.getChildren().add(btn);
        }
    }

    private void openMainView(String category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/main/medication/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setSelectedCategory(category);

            Stage stage = (Stage) categoryContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EMR Helper – " + category);
            StageSizing.fitToScreen(stage, 0.8, 0.9, 1100, 700);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onQuit() {
        Stage stage = (Stage) categoryContainer.getScene().getWindow();
        stage.close();
    }
}
