package com.emr.gds.main.allergy.controller;

import com.emr.gds.main.allergy.model.AllergyCause;
import com.emr.gds.main.allergy.model.SymptomItem;
import com.emr.gds.main.allergy.service.AllergyDataService;
import com.emr.gds.main.allergy.view.AllergyView;
import com.emr.gds.main.service.EmrBridgeService;
import com.emr.gds.input.IAITextAreaManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllergyController {

    private final AllergyView view;
    private final AllergyDataService dataService;
    private final EmrBridgeService emrBridgeService;
    private final String currentDate;
    private final ObservableSet<String> selectedCauses = FXCollections.observableSet(new LinkedHashSet<>());
    private final ObservableList<SymptomItem> symptoms;
    private final FilteredList<SymptomItem> filteredSymptoms;
    private TemplateMode templateMode = TemplateMode.DEFAULT;

    private enum TemplateMode {
        DEFAULT,
        DENY_ALL,
        ANAPHYLAXIS_DENIED
    }

    public AllergyController() {
        this.view = new AllergyView();
        this.dataService = new AllergyDataService();
        this.emrBridgeService = new EmrBridgeService();
        this.currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.symptoms = FXCollections.observableArrayList(dataService.getSymptomItems());
        this.filteredSymptoms = new FilteredList<>(symptoms, s -> true);

        setupSymptomTable();
        setupBindings();
        setupCauseTable();
        resetToDefault();
    }

    public AllergyView getView() {
        return view;
    }

    private void setupBindings() {
        // Menu Actions
        view.getSaveMenuItem().setOnAction(e -> copyToClipboard());
        view.getExitMenuItem().setOnAction(e -> System.exit(0));
        view.getDefaultTemplateMenuItem().setOnAction(e -> resetToDefault());
        view.getAllDeniedTemplateMenuItem().setOnAction(e -> denyAllSymptoms());
        view.getAnaDeniedTemplateMenuItem().setOnAction(e -> denyAnaphylaxisOnly());

        // Button Actions
        view.getClearOutputButton().setOnAction(e -> clearAllData());
        view.getCopyClipboardButton().setOnAction(e -> copyToClipboard());
        view.getSaveEmrButton().setOnAction(e -> saveToEmr());
        view.getQuitButton().setOnAction(e -> closeWindow());

        // Search Field
        view.getSearchField().textProperty().addListener((obs, old, newVal) -> filterSymptoms(newVal));
    }

    private void setupSymptomTable() {
        TableColumn<SymptomItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getCategory()));
        categoryCol.setPrefWidth(220);

        TableColumn<SymptomItem, String> symptomCol = new TableColumn<>("Symptom");
        symptomCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSymptom()));
        symptomCol.setPrefWidth(480);

        SortedList<SymptomItem> sortedSymptoms = new SortedList<>(filteredSymptoms);
        sortedSymptoms.comparatorProperty().bind(view.getSymptomTable().comparatorProperty());

        view.getSymptomTable().getColumns().setAll(categoryCol, symptomCol);
        view.getSymptomTable().setItems(sortedSymptoms);
        view.getSymptomTable().setRowFactory(tv -> createSymptomRow());
    }

    private TableRow<SymptomItem> createSymptomRow() {
        TableRow<SymptomItem> row = new TableRow<>();

        row.setOnMouseClicked(e -> {
            if (!row.isEmpty()) {
                SymptomItem item = row.getItem();
                item.setSelected(!item.isSelected());
                renderNote();
                updateRowHighlight(row, item);
            }
        });

        row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowHighlight(row, newItem));
        return row;
    }

    private void updateRowHighlight(TableRow<SymptomItem> row, SymptomItem item) {
        row.setStyle((item != null && item.isSelected()) ? "-fx-background-color: #e8f6ff;" : "");
    }

    private void setupCauseTable() {
        view.getCauseTable().setItems(dataService.getAllergyCauses());
        TableColumn<AllergyCause, String> col = new TableColumn<>("Known Allergens / Triggers");
        col.setCellValueFactory(c -> c.getValue().nameProperty());
        view.getCauseTable().getColumns().add(col);

        view.getCauseTable().setRowFactory(tv -> {
            TableRow<AllergyCause> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getClickCount() == 1) {
                    toggleCause(row.getItem().getName());
                }
            });
            return row;
        });
    }

    private void renderNote() {
        List<SymptomItem> selectedSymptoms = symptoms.stream()
                .filter(SymptomItem::isSelected)
                .toList();

        StringBuilder builder = new StringBuilder();
        builder.append("▣ Allergy History (").append(currentDate).append(")\n\n");

        if (templateMode == TemplateMode.DENY_ALL) {
            builder.append("""
                    Patient explicitly denies ALL allergic symptoms including:
                    • Skin reactions, swelling, respiratory distress
                    • Gastrointestinal symptoms
                    • Anaphylactic symptoms (airway, BP, consciousness)

                    No known drug/food/environmental allergies at this time.

                    """);
        } else {
            appendKnownAllergiesSection(builder);
            appendSymptomsSection(selectedSymptoms, builder);

            if (templateMode == TemplateMode.ANAPHYLAXIS_DENIED) {
                builder.append("▣ Patient specifically denies any history of anaphylaxis or life-threatening reactions.\n\n");
            }
        }

        view.getOutputArea().setText(builder.toString());
        updateSelectedCount(selectedSymptoms.size());
        scrollToBottom();
        view.getSymptomTable().refresh();
    }

    private void appendKnownAllergiesSection(StringBuilder builder) {
        if (selectedCauses.isEmpty()) {
            builder.append("▣ Known Allergies: None reported as of ").append(currentDate).append("\n");
            builder.append("▣ No known drug, food, or environmental allergies.\n\n");
        } else {
            builder.append("*** Documented Allergens / Triggers:***\n");
            selectedCauses.forEach(cause -> builder.append(" • ").append(cause).append("\n"));
            builder.append("\n");
        }
    }

    private void appendSymptomsSection(List<SymptomItem> selectedSymptoms, StringBuilder builder) {
        if (selectedSymptoms.isEmpty()) {
            return;
        }

        Map<String, List<String>> grouped = new LinkedHashMap<>();
        selectedSymptoms.forEach(item -> grouped
                .computeIfAbsent(item.getCategory(), k -> new ArrayList<>())
                .add(item.getSymptom()));

        builder.append("< Reported Symptoms >:\n");
        grouped.forEach((category, symptoms) -> builder
                .append(" • ")
                .append(category)
                .append(": ")
                .append(String.join(", ", symptoms))
                .append("\n"));
        builder.append("\n");
    }

    private void filterSymptoms(String query) {
        String lowerQuery = (query == null) ? "" : query.toLowerCase();
        filteredSymptoms.setPredicate(item -> lowerQuery.isEmpty()
                || item.getSymptom().toLowerCase().contains(lowerQuery)
                || item.getCategory().toLowerCase().contains(lowerQuery));
    }

    private void resetToDefault() {
        uncheckAll();
        selectedCauses.clear();
        templateMode = TemplateMode.DEFAULT;
        renderNote();
    }

    private void denyAllSymptoms() {
        uncheckAll();
        selectedCauses.clear();
        templateMode = TemplateMode.DENY_ALL;
        renderNote();
    }

    private void denyAnaphylaxisOnly() {
        symptoms.stream()
                .filter(SymptomItem::isAnaphylaxis)
                .forEach(item -> item.setSelected(false));
        selectedCauses.clear();
        templateMode = TemplateMode.ANAPHYLAXIS_DENIED;
        renderNote();
    }

    private void uncheckAll() {
        symptoms.forEach(item -> item.setSelected(false));
        view.getSymptomTable().refresh();
    }

    private void clearAllData() {
        uncheckAll();
        selectedCauses.clear();
        templateMode = TemplateMode.DEFAULT;
        view.getOutputArea().clear();
        updateSelectedCount(0);
    }

    private void toggleCause(String name) {
        if (selectedCauses.contains(name)) {
            selectedCauses.remove(name);
        } else {
            selectedCauses.add(name);
        }
        templateMode = TemplateMode.DEFAULT;
        renderNote();
    }

    private void updateSelectedCount(int symptomCount) {
        view.getCountLabel().setText("Selected: " + symptomCount + " symptoms • " + selectedCauses.size() + " allergen(s)");
    }

    private void copyToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(view.getOutputArea().getText());
        Clipboard.getSystemClipboard().setContent(content);
        showAlert("Copied!", "Allergy note copied to clipboard.");
    }

    private void saveToEmr() {
        String note = view.getOutputArea().getText().trim();
        if (note.isEmpty()) {
            showAlert("Nothing to Save", "Generate the note before saving to EMR.");
            return;
        }

        boolean saved = emrBridgeService.insertBlock(IAITextAreaManager.AREA_PMH, note);
        if (!saved) {
            showAlert("Save Failed", "EMR is not available. Open the main EMR first.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) view.getQuitButton().getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.show();
    }

    private void scrollToBottom() {
        view.getOutputArea().setScrollTop(Double.MAX_VALUE);
    }
}
