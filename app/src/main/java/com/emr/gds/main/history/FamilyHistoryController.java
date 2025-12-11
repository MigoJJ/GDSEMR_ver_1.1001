package com.emr.gds.main.history;

import com.emr.gds.input.IAITextAreaManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FamilyHistoryController {

    @FXML private ComboBox<String> relationshipComboBox;
    @FXML private TextArea notesTextArea;
    
    @FXML private ListView<String> endocrineList;
    @FXML private ListView<String> cancerList;
    @FXML private ListView<String> cardioList;
    @FXML private ListView<String> geneticList;
    
    @FXML private TextField searchField;
    @FXML private TextArea historyTextArea;

    private IAITextAreaManager textAreaManager;
    private Map<String, String> abbrevMap;

    private ObservableList<String> endocrineData;
    private ObservableList<String> cancerData;
    private ObservableList<String> cardioData;
    private ObservableList<String> geneticData;

    private static final Path DATA_DIR = Paths.get("emr_fmh_data");
    private static final Path ENDOCRINE_FILE = DATA_DIR.resolve("endocrine.txt");
    private static final Path CANCER_FILE = DATA_DIR.resolve("cancer.txt");
    private static final Path CARDIO_FILE = DATA_DIR.resolve("cardiovascular.txt");
    private static final Path GENETIC_FILE = DATA_DIR.resolve("genetic.txt");

    public void setManagers(IAITextAreaManager manager, Map<String, String> abbrevMap) {
        this.textAreaManager = manager;
        this.abbrevMap = (abbrevMap != null) ? abbrevMap : Collections.emptyMap();
    }

    @FXML
    public void initialize() {
        // Init ComboBox
        relationshipComboBox.setItems(FXCollections.observableArrayList(
                "Mother", "Father", "Sister", "Brother",
                "Grandmother", "Grandfather", "Aunt", "Uncle", "Cousin", "Child"
        ));

        // Load Data
        loadAllConditions();

        // Bind Lists
        setupListView(endocrineList, endocrineData);
        setupListView(cancerList, cancerData);
        setupListView(cardioList, cardioData);
        setupListView(geneticList, geneticData);

        // Search
        searchField.textProperty().addListener((obs, old, val) -> filterLists(val));
        
        // Abbreviation expansion
        setupAbbreviation(notesTextArea);
        setupAbbreviation(historyTextArea);
    }

    private void setupListView(ListView<String> listView, ObservableList<String> data) {
        FilteredList<String> filtered = new FilteredList<>(data, p -> true);
        listView.setItems(filtered);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadAllConditions() {
        endocrineData = loadConditions(ENDOCRINE_FILE, getDefaultEndocrine());
        cancerData = loadConditions(CANCER_FILE, getDefaultCancer());
        cardioData = loadConditions(CARDIO_FILE, getDefaultCardiovascular());
        geneticData = loadConditions(GENETIC_FILE, getDefaultGenetic());
    }

    private ObservableList<String> loadConditions(Path file, List<String> defaults) {
        try {
            if (Files.exists(file)) {
                return FXCollections.observableArrayList(Files.readAllLines(file));
            }
        } catch (IOException e) {
            // ignore
        }
        return FXCollections.observableArrayList(defaults);
    }

    private void filterLists(String filter) {
        String lower = filter.toLowerCase();
        updateFilter(endocrineList, lower);
        updateFilter(cancerList, lower);
        updateFilter(cardioList, lower);
        updateFilter(geneticList, lower);
    }

    @SuppressWarnings("unchecked")
    private void updateFilter(ListView<String> listView, String filter) {
        FilteredList<String> fl = (FilteredList<String>) listView.getItems();
        fl.setPredicate(s -> s.toLowerCase().contains(filter));
    }

    @FXML
    private void handleAddEntry() {
        String relationship = relationshipComboBox.getValue();
        if (relationship == null || relationship.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select a relationship.");
            return;
        }

        StringBuilder entry = new StringBuilder();
        entry.append(relationship).append(":\n");

        String notes = notesTextArea.getText().trim();
        if (!notes.isEmpty()) {
            entry.append("  Notes: ").append(notes).append("\n");
        }

        boolean hasCondition = false;
        hasCondition |= appendSelected(entry, "Endocrine", endocrineList);
        hasCondition |= appendSelected(entry, "Cancer", cancerList);
        hasCondition |= appendSelected(entry, "Cardiovascular", cardioList);
        hasCondition |= appendSelected(entry, "Genetic", geneticList);

        if (!hasCondition && notes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "Please select at least one condition or add notes.");
            return;
        }

        historyTextArea.appendText(entry.toString().trim() + "\n\n");
        clearFormInputs();
    }

    private boolean appendSelected(StringBuilder sb, String title, ListView<String> listView) {
        ObservableList<String> selected = listView.getSelectionModel().getSelectedItems();
        if (!selected.isEmpty()) {
            sb.append("  ").append(title).append(": ")
              .append(String.join("; ", selected)).append("\n");
            return true;
        }
        return false;
    }

    private void clearFormInputs() {
        relationshipComboBox.setValue(null);
        notesTextArea.clear();
        endocrineList.getSelectionModel().clearSelection();
        cancerList.getSelectionModel().clearSelection();
        cardioList.getSelectionModel().clearSelection();
        geneticList.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddCondition() {
        // Find focused list view to add to
        ListView<String> target = null;
        if (endocrineList.isFocused()) target = endocrineList;
        else if (cancerList.isFocused()) target = cancerList;
        else if (cardioList.isFocused()) target = cardioList;
        else if (geneticList.isFocused()) target = geneticList;

        if (target == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Click on a condition list first (to give it focus).");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Condition");
        dialog.setHeaderText("Add to selected list");
        dialog.setContentText("Condition name:");
        
        ListView<String> finalTarget = target;
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            // Get source list from FilteredList
            FilteredList<String> fl = (FilteredList<String>) finalTarget.getItems();
            @SuppressWarnings("unchecked")
            ObservableList<String> source = (ObservableList<String>) fl.getSource();
            
            if (!trimmed.isEmpty() && !source.contains(trimmed)) {
                source.add(trimmed);
            }
        });
    }

    @FXML
    private void handleSaveLists() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.write(ENDOCRINE_FILE, endocrineData);
            Files.write(CANCER_FILE, cancerData);
            Files.write(CARDIO_FILE, cardioData);
            Files.write(GENETIC_FILE, geneticData);
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Condition lists saved successfully.");
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Could not save lists: " + ex.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        historyTextArea.clear();
    }

    @FXML
    private void handleSaveToEMR() {
        if (textAreaManager == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "EMR Manager not connected.");
            return;
        }
        String text = historyTextArea.getText();
        if (text.trim().isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "History is empty. Save anyway?", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
        }
        
        textAreaManager.insertBlockIntoArea(IAITextAreaManager.AREA_PMH, text, true);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Family History saved to EMR.");
        handleClose();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) historyTextArea.getScene().getWindow();
        stage.close();
    }

    private void setupAbbreviation(TextArea ta) {
        ta.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                expandAbbreviation(ta);
            }
        });
    }

    private void expandAbbreviation(TextArea ta) {
        int caret = ta.getCaretPosition();
        String text = ta.getText();
        String upToCaret = text.substring(0, caret);
        int start = Math.max(upToCaret.lastIndexOf(' '), upToCaret.lastIndexOf('\n')) + 1;
        
        if (start >= caret) return; 
        
        String word = upToCaret.substring(start).trim();
        if (word.startsWith(":") && abbrevMap.containsKey(word.substring(1))) {
            String replacement = abbrevMap.get(word.substring(1));
            ta.deleteText(start, caret);
            ta.insertText(start, replacement + " ");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Default Data Providers
    private List<String> getDefaultEndocrine() {
        return Arrays.asList("Type 1 Diabetes", "Type 2 Diabetes", "Hypothyroidism", "Hyperthyroidism", "Thyroid Cancer");
    }
    private List<String> getDefaultCancer() {
        return Arrays.asList("Breast Cancer", "Lung Cancer", "Prostate Cancer", "Colon Cancer", "Skin Cancer");
    }
    private List<String> getDefaultCardiovascular() {
        return Arrays.asList("Coronary Artery Disease", "Hypertension", "Heart Attack", "Stroke", "Arrhythmia");
    }
    private List<String> getDefaultGenetic() {
        return Arrays.asList("Cystic Fibrosis", "Huntington's Disease", "Down Syndrome", "Sickle Cell Anemia", "Hemophilia");
    }
}
