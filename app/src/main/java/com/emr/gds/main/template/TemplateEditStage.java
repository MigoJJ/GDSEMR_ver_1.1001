package com.emr.gds.main.template;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class TemplateEditStage {

    public static void open(Consumer<String> onTemplateSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(TemplateEditStage.class.getResource("/fxml/template_editor.fxml"));
            Parent root = loader.load();

            TemplateEditController controller = loader.getController();
            // Initialize repository and callback
            controller.setRepository(new TemplateRepository());
            controller.setOnUseCallback(onTemplateSelected);

            Stage stage = new Stage();
            stage.setTitle("EMR Template Editor (JavaFX)");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
