package com.emr.gds.main.ekg;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Controller that wires the EKG view with the service.
 */
public class EkgSimpleReportController {

    private final EkgSimpleReportView view;
    private final EkgReportService service;

    public EkgSimpleReportController(EkgSimpleReportView view, EkgReportService service) {
        this.view = view;
        this.service = service;
        hookActions();
    }

    private void hookActions() {
        view.getSaveButton().setOnAction(e -> save());
    }

    private void save() {
        var raw = view.getEkgFindingsArea().getText().trim();
        if (raw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please enter findings before saving.");
            return;
        }

        var formatted = service.formatReport(raw);
        boolean ok = service.pushToEmr(formatted);
        if (ok) {
            view.getStatusLabel().setText("Saved to EMR.");
            showAlert(Alert.AlertType.INFORMATION, "EKG Report saved.");
        } else {
            showAlert(Alert.AlertType.ERROR, "EMR bridge not ready. Open the EMR first.");
        }
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Platform.runLater(() -> {
            var alert = new Alert(type, msg);
            alert.setHeaderText(null);
            alert.setTitle("EKG");
            alert.showAndWait();
        });
    }
}
