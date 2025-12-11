package com.emr.gds.main.history;

import com.emr.gds.input.IAITextAreaManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class FamilyHistoryStage {

    public static void open(IAITextAreaManager textAreaManager, Map<String, String> abbrevMap) {
        try {
            FXMLLoader loader = new FXMLLoader(FamilyHistoryStage.class.getResource("/fxml/family_history.fxml"));
            Parent root = loader.load();

            FamilyHistoryController controller = loader.getController();
            controller.setManagers(textAreaManager, abbrevMap);

            Stage stage = new Stage();
            stage.setTitle("Endocrinology - Family Medical History (JavaFX)");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
