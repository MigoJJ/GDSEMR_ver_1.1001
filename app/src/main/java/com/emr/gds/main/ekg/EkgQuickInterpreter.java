package com.emr.gds.main.ekg;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * SimpleEKGInterpreter - Ultra-fast EKG reporting tool
 * Replaces old EKG.java - Doctor-approved, minimal clicks
 */
public class EkgQuickInterpreter extends Stage {

    private static EkgQuickInterpreter active;

    private final TextArea findingsArea = new TextArea();
    private final TextArea impressionArea = new TextArea();
    private final TextArea commentsArea = new TextArea();

    public EkgQuickInterpreter() {
        setTitle("Quick EKG Interpreter - EMR Ready");
        initModality(Modality.NONE);
        setScene(new Scene(buildUI(), 820, 560));
    }

    public static void open() {
        if (active != null && active.isShowing()) {
            active.toFront();
            return;
        }
        active = new EkgQuickInterpreter();
        active.show();
        active.setOnHidden(e -> active = null);
    }

    private BorderPane buildUI() {
        findingsArea.setPromptText("Findings");
        impressionArea.setPromptText("Impression");
        commentsArea.setPromptText("Comments");
        findingsArea.setWrapText(true);
        impressionArea.setWrapText(true);
        commentsArea.setWrapText(true);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setLeft(buildSnippetPane());
        root.setCenter(buildEditorPane());
        root.setBottom(buildActions());
        return root;
    }

    private VBox buildSnippetPane() {
        VBox snippetBox = new VBox(8);
        snippetBox.setPadding(new Insets(8));
        snippetBox.setPrefWidth(220);

        Label header = new Label("Quick Snippets");
        header.getStyleClass().add("section-title");
        snippetBox.getChildren().add(header);

        addSnippetButton(snippetBox, "Normal sinus rhythm", "Normal sinus rhythm");
        addSnippetButton(snippetBox, "LVH", "Left ventricular hypertrophy criteria met");
        addSnippetButton(snippetBox, "AF", "Atrial fibrillation with controlled ventricular response");
        addSnippetButton(snippetBox, "RBBB", "Right bundle branch block");
        addSnippetButton(snippetBox, "Old MI", "Findings consistent with prior MI");
        addSnippetButton(snippetBox, "Ischemia", "Non-specific ST-T changes suggestive of ischemia");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        snippetBox.getChildren().add(spacer);
        return snippetBox;
    }

    private VBox buildEditorPane() {
        VBox editor = new VBox(12);
        editor.setPadding(new Insets(8, 0, 0, 12));
        editor.getChildren().add(labeledArea("Findings", findingsArea, 180));
        editor.getChildren().add(labeledArea("Impression", impressionArea, 120));
        editor.getChildren().add(labeledArea("Comments", commentsArea, 100));
        return editor;
    }

    private VBox labeledArea(String label, TextArea area, double minHeight) {
        area.setMinHeight(minHeight);
        ScrollPane scroll = new ScrollPane(area);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(minHeight + 40);

        VBox box = new VBox(6);
        box.getChildren().addAll(new Label(label), scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return box;
    }

    private HBox buildActions() {
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            findingsArea.clear();
            impressionArea.clear();
            commentsArea.clear();
        });

        Button saveBtn = new Button("Save to EMR");
        saveBtn.setDefaultButton(true);
        saveBtn.setOnAction(e -> saveToEmr());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10, spacer, clearBtn, saveBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));
        return actions;
    }

    private void addSnippetButton(VBox parent, String label, String text) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> appendSnippet(text));
        VBox.setMargin(btn, new Insets(2, 0, 0, 0));
        parent.getChildren().add(btn);
    }

    private void appendSnippet(String text) {
        findingsArea.appendText(text + "\n");
    }

    private void saveToEmr() {
        IAITextAreaManager manager = IAIMain.getManagerSafely()
                .filter(IAITextAreaManager::isReady)
                .orElse(null);

        if (manager == null) {
            showAlert(AlertType.WARNING, "Not Ready", "EMR text areas are not ready yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("EKG INTERPRETATION\n");
        sb.append("Findings: ").append(findingsArea.getText().trim()).append("\n");
        sb.append("Impression: ").append(impressionArea.getText().trim()).append("\n");
        sb.append("Comments: ").append(commentsArea.getText().trim()).append("\n");
        sb.append("\n— End of EKG Report —");

        String report = sb.toString();
        String stampedReport = String.format("\n< EKG Report > %s\n%s",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE), report);

        manager.focusArea(5);
        manager.insertLineIntoFocusedArea(stampedReport);

        showAlert(AlertType.INFORMATION, "Saved", "EKG report saved to EMR.");
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(this);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
