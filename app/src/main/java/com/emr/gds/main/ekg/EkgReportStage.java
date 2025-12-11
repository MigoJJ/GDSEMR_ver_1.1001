package com.emr.gds.main.ekg;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EkgReportStage {

    public static void open() {
        try {
            FXMLLoader loader = new FXMLLoader(EkgReportStage.class.getResource("/fxml/ekg_report.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("EMR EKG Analysis (JavaFX)");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
