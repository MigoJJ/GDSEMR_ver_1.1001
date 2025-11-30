package com.emr.gds.main.glp1;

import com.emr.gds.input.IAIFxTextAreaManager;
import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;

import com.emr.gds.util.StageSizing;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Demo launcher for Glp1SemaglutidePane.
 * You can run this as a standalone JavaFX application.
 */
public class Glp1SemaglutideMain extends Application {

    @Override
    public void start(Stage stage) {
        Glp1SemaglutidePane medPane = new Glp1SemaglutidePane();

        // Output area for problem-list text
        TextArea txtOutput = new TextArea();
        txtOutput.setPromptText("Problem-list output will appear here...");
        txtOutput.setPrefRowCount(8);
        txtOutput.setWrapText(true);

        // Buttons
        Button btnExport = new Button("Generate to EMR");
        Button btnSave   = new Button("Save to EMR");
        Button btnClear  = new Button("Clear All");
        Button btnQuit   = new Button("Quit");

        btnExport.setOnAction(e -> txtOutput.setText(medPane.toProblemListString()));
        btnClear.setOnAction(e -> {
            medPane.clearAll();
            txtOutput.clear();
        });
        btnSave.setOnAction(e -> {
            String content = txtOutput.getText().trim();
            if (content.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "Nothing to save. Generate text first.").showAndWait();
                return;
            }

            IAIMain.getManagerSafely().ifPresentOrElse(
                    manager -> {
                        String textToAppend = content.endsWith("\n") ? content : content + "\n";
                        manager.appendTextToSection(IAITextAreaManager.AREA_PI, textToAppend);

                        String assessmentLine = medPane.toAssessmentSummary();
                        if (!assessmentLine.isBlank()) {
                            manager.appendTextToSection(IAIFxTextAreaManager.AREA_A, assessmentLine + "\n");
                        }

                        new Alert(Alert.AlertType.INFORMATION, "Saved to PI> and A> in EMR.").showAndWait();
                    },
                    () -> new Alert(
                            Alert.AlertType.ERROR,
                            "EMR mainframe is not connected.\nOpen this tool from the EMR to enable saving."
                    ).showAndWait()
            );
        });
        btnQuit.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, btnExport, btnSave, btnClear, btnQuit);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(6));

        BorderPane bottomPane = new BorderPane();
        bottomPane.setTop(new Separator());
        bottomPane.setCenter(txtOutput);
        bottomPane.setBottom(buttonBox);

        BorderPane root = new BorderPane();
        root.setCenter(medPane);
        root.setBottom(bottomPane);

        Scene scene = new Scene(root);
        stage.setTitle("GLP-1RA (Semaglutide) - EMR Module Demo");
        stage.setScene(scene);
        // Use a narrower width ratio (0.5) for this specific module
        StageSizing.fitToScreen(stage, 0.5, 0.9);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
