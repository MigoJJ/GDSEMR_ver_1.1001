package com.emr.gds.main.medication.controller;

import com.emr.gds.main.medication.db.DatabaseManager;
import com.emr.gds.main.medication.model.MedicationItem;
import com.emr.gds.main.medication.model.MedicationGroup;
import com.emr.gds.util.StageSizing;
import com.emr.gds.main.service.EmrBridgeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    @FXML private TextArea outputTextArea;
    @FXML private TabPane mainTabPane;
    @FXML private Label selectionLabel;
    @FXML private Button btnEdit, btnDelete, btnSave;

    private final DatabaseManager dbManager = new DatabaseManager();
    private final EmrBridgeService emrBridge = new EmrBridgeService();

    private MedicationItem activeItem;
    private ListView<MedicationItem> activeListView;

    public void setSelectedCategory(String categoryName) {
        loadAllTabs();
        selectTabByName(categoryName);
    }

    private void loadAllTabs() {
        mainTabPane.getTabs().clear();
        for (String cat : dbManager.getOrderedCategories()) {
            Accordion accordion = new Accordion();
            for (MedicationGroup group : dbManager.getMedicationData().getOrDefault(cat, java.util.List.of())) {
                ListView<MedicationItem> lv = createListView(group.medications());
                TitledPane tp = new TitledPane(group.title(), lv);
                accordion.getPanes().add(tp);
            }
            mainTabPane.getTabs().add(new Tab(cat, accordion));
        }
    }

    private ListView<MedicationItem> createListView(List<MedicationItem> items) {
        ListView<MedicationItem> lv = new ListView<>();
        lv.getItems().addAll(items);
        lv.setOnMouseClicked(e -> {
            MedicationItem item = lv.getSelectionModel().getSelectedItem();
            if (item != null && !isSeparator(item.getText())) {
                outputTextArea.appendText(item.getText() + "\n");
                copyToClipboard(item.getText());
            }
        });
        lv.getSelectionModel().selectedItemProperty().addListener((obs, old, nv) -> updateSelection(lv, nv));
        return lv;
    }

    private void updateSelection(ListView<MedicationItem> lv, MedicationItem item) {
        if (item != null && !isSeparator(item.getText())) {
            activeItem = item;
            activeListView = lv;
            selectionLabel.setText(item.getText());
        } else {
            activeItem = null;
        }
        btnEdit.setDisable(activeItem == null);
        btnDelete.setDisable(activeItem == null);
        refreshSaveButton();
    }

    private void refreshSaveButton() {
        btnSave.setDisable(!dbManager.hasPendingChanges());
    }

    @FXML
    private void findMedication() {
        TextInputDialog searchDialog = new TextInputDialog();
        searchDialog.setTitle("Find Medication");
        searchDialog.setHeaderText("Search for a medication across all categories");
        searchDialog.setContentText("Query:");

        Optional<String> searchResult = searchDialog.showAndWait();
        searchResult.ifPresent(query -> {
            String lowerQuery = query.toLowerCase();
            List<MedicationItem> matches = new ArrayList<>();
            
            for (List<MedicationGroup> groups : dbManager.getMedicationData().values()) {
                for (MedicationGroup group : groups) {
                    for (MedicationItem item : group.medications()) {
                        if (item.getText().toLowerCase().contains(lowerQuery)) {
                            matches.add(item);
                        }
                    }
                }
            }

            if (matches.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No medications found matching: " + query);
            } else {
                Dialog<MedicationItem> resultsDialog = new Dialog<>();
                resultsDialog.setTitle("Search Results");
                resultsDialog.setHeaderText("Found " + matches.size() + " matches. Double-click to select.");
                
                ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
                resultsDialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);
                
                ListView<MedicationItem> listView = new ListView<>();
                listView.setItems(FXCollections.observableArrayList(matches));
                listView.setPrefHeight(300);
                listView.setPrefWidth(400);
                
                listView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        MedicationItem selected = listView.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            resultsDialog.setResult(selected);
                            resultsDialog.close();
                        }
                    }
                });

                resultsDialog.getDialogPane().setContent(listView);
                
                resultsDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == selectButtonType) {
                        return listView.getSelectionModel().getSelectedItem();
                    }
                    return null;
                });

                resultsDialog.showAndWait().ifPresent(item -> {
                    outputTextArea.appendText(item.getText() + "\n");
                    copyToClipboard(item.getText());
                });
            }
        });
    }

    @FXML
    private void addMedication() {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) return;
        String category = selectedTab.getText();

        List<MedicationGroup> groups = dbManager.getMedicationData().get(category);
        if (groups == null || groups.isEmpty()) return;

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Medication");
        dialog.setHeaderText("Add new medication to " + category);

        ButtonType loginButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> groupCombo = new ComboBox<>();
        groupCombo.setItems(FXCollections.observableArrayList(groups.stream().map(MedicationGroup::title).collect(Collectors.toList())));
        groupCombo.getSelectionModel().selectFirst();

        TextField itemText = new TextField();
        itemText.setPromptText("Medication Name & Dosage");

        grid.add(new Label("Group:"), 0, 0);
        grid.add(groupCombo, 1, 0);
        grid.add(new Label("Text:"), 0, 1);
        grid.add(itemText, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> itemText.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(groupCombo.getValue(), itemText.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            if (!pair.getValue().isBlank()) {
                MedicationItem newItem = new MedicationItem(pair.getValue());
                dbManager.addItem(category, pair.getKey(), newItem);
                refreshCurrentTab();
                refreshSaveButton();
            }
        });
    }

    @FXML
    private void editItem() {
        if (activeItem == null) return;

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Medication");
        dialog.setHeaderText("Edit medication text");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextArea textArea = new TextArea(activeItem.getText());
        textArea.setWrapText(true);
        textArea.setPrefHeight(150);
        textArea.setPrefWidth(400);

        dialog.getDialogPane().setContent(textArea);

        Platform.runLater(textArea::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return textArea.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newText -> {
            if (!newText.equals(activeItem.getText())) {
                activeItem.setText(newText);
                activeListView.refresh();
                dbManager.markDirty();
                refreshSaveButton();
            }
        });
    }

    @FXML
    private void deleteItem() {
        if (activeItem == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Item");
        alert.setHeaderText("Are you sure you want to delete this item?");
        alert.setContentText(activeItem.getText());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Remove from DB logic
            dbManager.removeItem(activeItem);
            // Remove from UI list
            activeListView.getItems().remove(activeItem);
            
            activeItem = null;
            selectionLabel.setText("No medication selected");
            btnEdit.setDisable(true);
            btnDelete.setDisable(true);
            refreshSaveButton();
        }
    }

    @FXML
    private void saveChanges() {
        dbManager.commitPending();
        refreshSaveButton();
        showAlert(Alert.AlertType.INFORMATION, "Saved", "Changes have been saved to the current session.");
    }

    @FXML private void copyAll() {
        copyToClipboard(outputTextArea.getText());
    }

    @FXML private void saveToEmr() {
        String text = outputTextArea.getText().trim();
        if (text.isEmpty()) return;
        var manager = emrBridge.getManager();
        if (manager.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "EMR Not Ready", "Please open the EMR first.");
            return;
        }
        manager.get().focusArea(9);
        manager.get().insertBlockIntoFocusedArea(text + ".");
        showAlert(Alert.AlertType.INFORMATION, "Success", "Saved to EMR comment field.");
    }

    @FXML private void clearOutput() { outputTextArea.clear(); }

    @FXML private void backToLauncher() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/emr/gds/main/medication/launcher.fxml"));
        Stage stage = (Stage) mainTabPane.getScene().getWindow();
        stage.setScene(new Scene(root));
        StageSizing.fitToScreen(stage, 0.35, 0.85, 400, 600);
    }

    @FXML private void quitApp() {
        Stage stage = (Stage) mainTabPane.getScene().getWindow();
        stage.close();
    }

    private void selectTabByName(String name) {
        mainTabPane.getTabs().stream()
                .filter(t -> t.getText().equals(name))
                .findFirst()
                .ifPresent(t -> mainTabPane.getSelectionModel().select(t));
    }

    private void refreshCurrentTab() {
        Tab selectedTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            String category = selectedTab.getText();
            // Rebuild accordion for this tab
            Accordion accordion = new Accordion();
            for (MedicationGroup group : dbManager.getMedicationData().getOrDefault(category, java.util.List.of())) {
                ListView<MedicationItem> lv = createListView(group.medications());
                TitledPane tp = new TitledPane(group.title(), lv);
                accordion.getPanes().add(tp);
            }
            selectedTab.setContent(accordion);
        }
    }

    private boolean isSeparator(String text) {
        return text.trim().matches("^-{3,}$|^---.*---$|^\\.{3,}$");
    }

    private void copyToClipboard(String text) {
        if (!text.isBlank()) {
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(
                    java.util.Map.of(javafx.scene.input.DataFormat.PLAIN_TEXT, text)
            );
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }
}
