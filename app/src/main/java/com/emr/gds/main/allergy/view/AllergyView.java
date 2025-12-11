package com.emr.gds.main.allergy.view;

import com.emr.gds.main.allergy.model.AllergyCause;
import com.emr.gds.main.allergy.model.SymptomItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class AllergyView extends BorderPane {

    private final MenuBar menuBar;
    private final MenuItem saveMenuItem;
    private final MenuItem exitMenuItem;
    private final MenuItem defaultTemplateMenuItem;
    private final MenuItem allDeniedTemplateMenuItem;
    private final MenuItem anaDeniedTemplateMenuItem;
    private final TextField searchField;
    private final TableView<SymptomItem> symptomTable;
    private final TableView<AllergyCause> causeTable;
    private final TextArea outputArea;
    private final Button clearOutputButton;
    private final Button copyClipboardButton;
    private final Button saveEmrButton;
    private final Button quitButton;
    private final Label statusLabel;
    private final Label countLabel;

    public AllergyView() {
        // --- Menu ---
        saveMenuItem = new MenuItem("Copy to Clipboard");
        exitMenuItem = new MenuItem("Exit");
        Menu fileMenu = new Menu("File", null, saveMenuItem, new SeparatorMenuItem(), exitMenuItem);

        defaultTemplateMenuItem = new MenuItem("No Known Allergies (Default)");
        allDeniedTemplateMenuItem = new MenuItem("All Symptoms Denied");
        anaDeniedTemplateMenuItem = new MenuItem("Anaphylaxis Denied Only");
        Menu templatesMenu = new Menu("Templates", null, defaultTemplateMenuItem, allDeniedTemplateMenuItem, anaDeniedTemplateMenuItem);

        menuBar = new MenuBar(fileMenu, templatesMenu);
        menuBar.setUseSystemMenuBar(true);

        // --- Left Panel ---
        Label title = new Label("Symptom Checklist (Check all that apply)");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        searchField = new TextField();
        searchField.setPromptText("Search symptoms...");
        symptomTable = new TableView<>();
        symptomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox leftPanel = new VBox(10, title, searchField, symptomTable);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(680);
        VBox.setVgrow(symptomTable, Priority.ALWAYS);

        // --- Right Panel ---
        Label causeLabel = new Label("Common Allergy Causes (Click to add)");
        causeLabel.setStyle("-fx-font-weight: bold;");
        causeTable = new TableView<>();
        causeTable.setPrefWidth(340);
        Label outputLabel = new Label("Final Note Output");
        outputLabel.setStyle("-fx-font-weight: bold;");
        outputArea = new TextArea();
        outputArea.setWrapText(true);
        outputArea.setEditable(true);
        outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13;");
        clearOutputButton = new Button("Clear Output");
        copyClipboardButton = new Button("Copy to Clipboard");
        saveEmrButton = new Button("Save to EMR");
        quitButton = new Button("Quit");
        HBox buttonBox = new HBox(10, clearOutputButton, copyClipboardButton, saveEmrButton, quitButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        VBox rightPanel = new VBox(10, causeLabel, causeTable, new Separator(), outputLabel, outputArea, buttonBox);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;");
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        VBox.setVgrow(causeTable, Priority.ALWAYS);

        // --- Split Pane ---
        SplitPane splitPane = new SplitPane(leftPanel, rightPanel);
        splitPane.setDividerPositions(0.58, 0.42);

        // --- Status Bar ---
        statusLabel = new Label("Ready • " + LocalDate.now());
        statusLabel.setPadding(new Insets(8));
        statusLabel.setStyle("-fx-background-color: #e9ecef; -fx-font-size: 12;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        countLabel = new Label();
        HBox statusBar = new HBox(statusLabel, spacer, countLabel);

        // --- Root Layout ---
        setTop(menuBar);
        setCenter(splitPane);
        setBottom(statusBar);
    }

    // --- Getters for UI elements to be accessed by the controller ---

    public MenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public MenuItem getExitMenuItem() {
        return exitMenuItem;
    }

    public MenuItem getDefaultTemplateMenuItem() {
        return defaultTemplateMenuItem;
    }

    public MenuItem getAllDeniedTemplateMenuItem() {
        return allDeniedTemplateMenuItem;
    }

    public MenuItem getAnaDeniedTemplateMenuItem() {
        return anaDeniedTemplateMenuItem;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public TableView<SymptomItem> getSymptomTable() {
        return symptomTable;
    }

    public TableView<AllergyCause> getCauseTable() {
        return causeTable;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public Button getClearOutputButton() {
        return clearOutputButton;
    }

    public Button getCopyClipboardButton() {
        return copyClipboardButton;
    }

    public Button getSaveEmrButton() {
        return saveEmrButton;
    }

    public Button getQuitButton() {
        return quitButton;
    }

    public Label getCountLabel() {
        return countLabel;
    }
}
