package com.emr.gds.main.ekg;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.ButtonType;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EkgReportController {

    @FXML
    private TextArea findingsArea;

    @FXML
    private TextArea summaryArea;

    @FXML
    public void initialize() {
        findingsArea.setText("""
Rate:
Rhythm:
Axis:
Intervals (PR, QRS, QTc):
Hypertrophy:
Ischemia / ST-T changes:
Others:
""");
    }

    @FXML
    private void handleSave() {
        IAITextAreaManager manager = IAIMain.getTextAreaManager();
        if (manager == null || !manager.isReady()) {
            showAlert(Alert.AlertType.ERROR, "Error", "EMR is not ready. Please open the EMR first.");
            return;
        }

        String reportText = findingsArea.getText().trim();
        String summaryText = summaryArea.getText().trim();

        if (reportText.isEmpty() && summaryText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter findings or a summary before saving.");
            return;
        }

        String stampedReport = String.format("\n< EKG Report - %s >\n%s\nSummary: %s",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                reportText,
                summaryText.isEmpty() ? "(none)" : summaryText);

        manager.focusArea(5); // Target 'O>' area
        manager.insertLineIntoFocusedArea(stampedReport);
        
        showAlert(Alert.AlertType.INFORMATION, "Success", "EKG report saved to EMR.");
    }

    @FXML
    private void handleClear() {
        findingsArea.setText("");
        summaryArea.setText("");
    }

    @FXML
    private void handleReference() {
        try {
            // Note: This path assumes running from project root, similar to legacy behavior.
            File file = new File("src/main/resources/text/EKG_reference.odt").getAbsoluteFile();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Desktop operations not supported on this platform.");
            }
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open reference: " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
