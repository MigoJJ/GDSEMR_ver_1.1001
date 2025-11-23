package com.emr.gds.main;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A JavaFX application for Osteoporosis Risk Assessment based on DEXA scan results.
 * Provides UI for inputting patient data and calculates risk based on T-score and other factors.
 */
public class DexaRiskAssessmentApp extends Stage {

    private TextField txtAge;
    private TextField txtTscore;
    private TextField txtBMI;
    private TextField txtFractureHistory;
    private TextArea txtReport;

    public DexaRiskAssessmentApp() {
        setTitle("Osteoporosis Risk Assessment (DEXA)");
        initUI();
    }

    private void initUI() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        txtAge = new TextField();
        txtTscore = new TextField();
        txtBMI = new TextField();
        txtFractureHistory = new TextField();

        grid.add(new Label("Age:"), 0, 0);
        grid.add(txtAge, 1, 0);
        grid.add(new Label("T-score:"), 0, 1);
        grid.add(txtTscore, 1, 1);
        grid.add(new Label("BMI:"), 0, 2);
        grid.add(txtBMI, 1, 2);
        grid.add(new Label("Fracture history (yes/no):"), 0, 3);
        grid.add(txtFractureHistory, 1, 3);

        Button btnAssess = new Button("Assess Risk");
        btnAssess.setOnAction(e -> assessRisk());

        Button btnSave = new Button("Save to EMR");
        btnSave.setOnAction(e -> saveToEmr());

        HBox buttonBox = new HBox(10, btnAssess, btnSave);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        txtReport = new TextArea();
        txtReport.setPromptText("Generated report will appear here...");
        txtReport.setWrapText(true);
        txtReport.setPrefRowCount(10);

        VBox root = new VBox(15, grid, buttonBox, txtReport);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 650, 500);
        setScene(scene);
    }

    private void assessRisk() {
        String age = txtAge.getText().trim();
        String tscore = txtTscore.getText().trim();
        String bmi = txtBMI.getText().trim();
        String fracture = txtFractureHistory.getText().trim();

        StringBuilder report = new StringBuilder();
        report.append("Osteoporosis Risk Assessment (DEXA)\n");
        report.append("-------------------------------\n");
        report.append("Age: ").append(age).append("\n");
        report.append("T-score: ").append(tscore).append("\n");
        report.append("BMI: ").append(bmi).append("\n");
        report.append("Fracture history: ").append(fracture.isEmpty() ? "Not provided" : fracture).append("\n\n");

        double t = parseDouble(tscore);
        if (!Double.isNaN(t)) {
            if (t <= -2.5) {
                report.append("Risk Category: High risk of osteoporosis.\n");
                report.append("Recommendation: Consider pharmacologic therapy and lifestyle modifications.\n");
            } else if (t <= -1.0) {
                report.append("Risk Category: Osteopenia.\n");
                report.append("Recommendation: Lifestyle modifications and monitoring.\n");
            } else {
                report.append("Risk Category: Normal bone density.\n");
                report.append("Recommendation: Routine follow-up.\n");
            }
        } else {
            report.append("Risk Category: Unable to determine (invalid T-score input).\n");
        }

        txtReport.setText(report.toString());
    }

    private void saveToEmr() {
        String report = txtReport.getText();
        if (report == null || report.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Nothing to save", "Please assess risk before saving to EMR.");
            return;
        }

        IAITextAreaManager emrManager = IAIMain.getTextAreaManager();
        if (emrManager == null || !emrManager.isReady()) {
            showAlert(Alert.AlertType.ERROR, "EMR not ready", "EMR text areas not available.");
            return;
        }

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String stampedReport = String.format("< DEXA Report - %s >\n%s", date, report.trim());

        emrManager.focusArea(5); // Objective area
        emrManager.insertLineIntoFocusedArea(stampedReport + "\n");
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Report inserted into EMR.");
    }

    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle("DEXA");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }
}
