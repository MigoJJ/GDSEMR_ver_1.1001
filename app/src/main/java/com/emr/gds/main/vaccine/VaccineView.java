package com.emr.gds.main.vaccine;

import com.emr.gds.main.VaccineConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

/**
 * View-only class that builds the Vaccine palette UI.
 */
public class VaccineView {

    private static final double FRAME_WIDTH = 500;
    private static final double FRAME_HEIGHT = 900;
    private static final Font LABEL_FONT = Font.font("Malgun Gothic", FontWeight.BOLD, 14);
    private static final Font BUTTON_FONT = Font.font("Malgun Gothic", FontWeight.NORMAL, 12);
    private static final String HEADER_STYLE = "-fx-background-color: #DCE6F0; -fx-padding: 6 0 6 0;";
    private static final String VACCINE_BUTTON_STYLE = "-fx-background-color: #FFFFFF;";
    private static final String SIDEEFFECT_BUTTON_STYLE = "-fx-background-color: #FFFBE1;";
    private static final String QUIT_BUTTON_STYLE = "-fx-background-color: #FFE0E0;";

    private final Stage stage = new Stage(StageStyle.UTILITY);

    public VaccineView(Consumer<String> onAction) {
        stage.setTitle("Vaccinations");
        stage.setWidth(FRAME_WIDTH);
        stage.setHeight(FRAME_HEIGHT);
        stage.initStyle(StageStyle.UTILITY);

        VBox root = new VBox(4);
        root.setStyle("-fx-background-color: #333333;");
        root.setPadding(Insets.EMPTY);

        for (String text : VaccineConstants.UI_ELEMENTS) {
            if (text.startsWith("###")) {
                root.getChildren().add(createHeaderLabel(text));
            } else {
                root.getChildren().add(createActionButton(text, onAction));
            }
        }

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        stage.setScene(new Scene(scrollPane));
        positionStageAtBottomRight(stage);
    }

    private Label createHeaderLabel(String text) {
        Label header = new Label(text.replace("###", "").trim());
        header.setFont(LABEL_FONT);
        header.setStyle(HEADER_STYLE);
        header.setAlignment(Pos.CENTER);
        header.setMaxWidth(Double.MAX_VALUE);
        return header;
    }

    private Button createActionButton(String text, Consumer<String> onAction) {
        Button button = new Button(text);
        button.setFont(BUTTON_FONT);
        button.setFocusTraversable(false);
        button.setMaxWidth(Double.MAX_VALUE);

        switch (text) {
            case "Side Effect" -> button.setStyle(SIDEEFFECT_BUTTON_STYLE);
            case "Quit" -> button.setStyle(QUIT_BUTTON_STYLE);
            default -> button.setStyle(VACCINE_BUTTON_STYLE);
        }

        button.setOnAction(e -> onAction.accept(text));
        return button;
    }

    private void positionStageAtBottomRight(Stage stage) {
        var screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMaxX() - stage.getWidth());
        stage.setY(screenBounds.getMaxY() - stage.getHeight());
    }

    public Stage getStage() {
        return stage;
    }
}
