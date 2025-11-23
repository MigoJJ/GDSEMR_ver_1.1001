package com.emr.gds.main.ekg;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;

/**
 * Pure view for the simple EKG report form.
 */
public class EkgSimpleReportView {

    private final TextArea ekgFindingsArea = new TextArea("""
EKG INTERPRETATION REPORT
- Rhythm:
- Axis:
- PR / QRS / QTc:
- ST-T changes:
- Impression:
""");
    private final Button saveButton = new Button("Save to EMR");
    private final Label statusLabel = new Label();

    public EkgSimpleReportView() {
        ekgFindingsArea.setWrapText(true);
        ekgFindingsArea.setPrefRowCount(12);
    }

    public Parent createContent() {
        var container = new BorderPane();
        container.setPadding(new Insets(12));
        container.setTop(new Label("Enter EKG Findings:"));
        container.setCenter(ekgFindingsArea);

        var footer = new VBox(6, saveButton, statusLabel);
        footer.setPadding(new Insets(8, 0, 0, 0));
        statusLabel.setStyle("-fx-text-fill: #888;");
        container.setBottom(footer);
        return container;
    }

    public TextArea getEkgFindingsArea() {
        return ekgFindingsArea;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
