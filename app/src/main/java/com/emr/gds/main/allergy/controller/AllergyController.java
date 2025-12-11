package com.emr.gds.main.allergy.controller;

import com.emr.gds.main.allergy.model.AllergyCause;
import com.emr.gds.main.allergy.model.SymptomItem;
import com.emr.gds.main.allergy.service.AllergyDataService;
import com.emr.gds.main.allergy.view.AllergyView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AllergyController {

    private final AllergyView view;
    private final AllergyDataService dataService;
    private final String currentDate;

    public AllergyController() {
        this.view = new AllergyView();
        this.dataService = new AllergyDataService();
        this.currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

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
        view.getClearOutputButton().setOnAction(e -> view.getOutputArea().clear());
        view.getCopyClipboardButton().setOnAction(e -> copyToClipboard());

        // Search Field
        view.getSearchField().textProperty().addListener((obs, old, newVal) -> filterSymptoms(newVal));

        // Symptom selection count
        view.getCountLabel().textProperty().bind(Bindings.createStringBinding(
                () -> "Selected: " + countSelectedSymptoms(),
                Bindings.size(view.getSymptomTreeTable().getRoot().getChildren())
        ));
    }

    private void setupSymptomTable() {
        TreeItem<SymptomItem> root = new TreeItem<>(new SymptomItem("Root", "", false));
        root.setExpanded(true);

        Map<String, TreeItem<SymptomItem>> categories = new HashMap<>();
        dataService.getSymptomItems().forEach(item -> {
            categories.computeIfAbsent(item.getCategory(), cat -> {
                TreeItem<SymptomItem> catItem = new TreeItem<>(new SymptomItem(cat, "", false));
                catItem.setExpanded(true);
                root.getChildren().add(catItem);
                return catItem;
            }).getChildren().add(new TreeItem<>(item));
        });

        TreeTableColumn<SymptomItem, Boolean> checkCol = new TreeTableColumn<>("");
        checkCol.setPrefWidth(40);
        checkCol.setCellValueFactory(param -> param.getValue().getValue().selectedProperty().asObject());
        checkCol.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(checkCol));

        TreeTableColumn<SymptomItem, String> symptomCol = new TreeTableColumn<>("Symptom");
        symptomCol.setPrefWidth(500);
        symptomCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getSymptom()));

        view.getSymptomTreeTable().setRoot(root);
        view.getSymptomTreeTable().getColumns().addAll(checkCol, symptomCol);
        view.getSymptomTreeTable().setOnMouseClicked(e -> updateOutputFromSelection());

        addRecursiveListener(root);
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
                    view.getOutputArea().appendText("*** Allergic to: " + row.getItem().getName() + "\n");
                    scrollToBottom();
                }
            });
            return row;
        });
    }

    private void addRecursiveListener(TreeItem<SymptomItem> item) {
        if (item.getValue() != null) {
            item.getValue().selectedProperty().addListener((obs, was, is) -> updateOutputFromSelection());
        }
        item.getChildren().forEach(this::addRecursiveListener);
    }

    private void updateOutputFromSelection() {
        Set<String> selectedSymptoms = new HashSet<>();
        collectSelected(view.getSymptomTreeTable().getRoot(), selectedSymptoms);

        String newSymptomBlock;
        if (selectedSymptoms.isEmpty()) {
            newSymptomBlock = "▣ No allergy symptoms reported.\n\n";
        } else {
            newSymptomBlock = "▣ Reported Symptoms:\n" +
                    selectedSymptoms.stream()
                            .map(s -> " • " + s)
                            .collect(Collectors.joining("\n")) + "\n\n";
        }

        String existingText = view.getOutputArea().getText();
        String reportedHeader = "▣ Reported Symptoms:";
        String noSymptomsMessage = "▣ No allergy symptoms reported.";

        int blockStartIndex = existingText.indexOf(reportedHeader);
        if (blockStartIndex == -1) {
            blockStartIndex = existingText.indexOf(noSymptomsMessage);
        }

        if (blockStartIndex != -1) {
            // Find the end of the block, which is a double newline
            int blockEndIndex = existingText.indexOf("\n\n", blockStartIndex);
            if (blockEndIndex != -1) {
                String before = existingText.substring(0, blockStartIndex);
                String after = existingText.substring(blockEndIndex + 2); // Length of "\n\n"
                view.getOutputArea().setText(before + newSymptomBlock + after);
            } else {
                // Block start found, but not end. Assume it goes to the end of the text.
                String before = existingText.substring(0, blockStartIndex);
                view.getOutputArea().setText(before + newSymptomBlock);
            }
        } else {
            // No existing symptom block found, so we insert it intelligently
            String historyHeader = "▣ Allergy History";
            int headerIndex = existingText.indexOf(historyHeader);
            if (headerIndex != -1) {
                int insertAfterIndex = existingText.indexOf("\n\n", headerIndex);
                if (insertAfterIndex != -1) {
                    String before = existingText.substring(0, insertAfterIndex + 2);
                    String after = existingText.substring(insertAfterIndex + 2);

                    // Clean up common template lines from the 'after' part to prevent duplication
                    String cleanedAfter = after.replaceAll("(?m)^▣ .+\\R?", "")
                                               .replaceAll("(?m)^Patient explicitly denies .+\\R?", "")
                                               .replaceAll("(?m)^• .+\\R?", "")
                                               .replaceAll("(?m)^No known .+\\R?", "");

                    view.getOutputArea().setText(before + newSymptomBlock + cleanedAfter.trim());
                } else {
                    // If no double newline after header, just append after a single newline
                    view.getOutputArea().appendText("\n" + newSymptomBlock);
                }
            } else {
                // If no header at all, prepend the new block to the existing text
                view.getOutputArea().setText(newSymptomBlock + existingText);
            }
        }
        scrollToBottom();
    }

    private void collectSelected(TreeItem<SymptomItem> node, Set<String> result) {
        if (node.getValue() != null && node.getValue().isSelected() && !node.getValue().getSymptom().isEmpty()) {
            result.add(node.getValue().getCategory() + " → " + node.getValue().getSymptom());
        }
        node.getChildren().forEach(child -> collectSelected(child, result));
    }

    private int countSelectedSymptoms() {
        Set<String> selected = new HashSet<>();
        collectSelected(view.getSymptomTreeTable().getRoot(), selected);
        return selected.size();
    }

    private void filterSymptoms(String query) {
        String lowerQuery = (query == null) ? "" : query.toLowerCase();
        view.getSymptomTreeTable().getRoot().getChildren().forEach(categoryItem -> {
            boolean matches = categoryItem.getChildren().stream()
                    .anyMatch(symptomItem -> symptomItem.getValue().getSymptom().toLowerCase().contains(lowerQuery));
            categoryItem.setExpanded(matches || lowerQuery.isEmpty());
        });
    }

    private void resetToDefault() {
        uncheckAll();
        view.getOutputArea().setText(getDefaultTemplate());
    }

    private void denyAllSymptoms() {
        uncheckAll();
        view.getOutputArea().setText(getDenyAllTemplate());
    }

    private void denyAnaphylaxisOnly() {
        traverseAndSetIf(view.getSymptomTreeTable().getRoot(), SymptomItem::isAnaphylaxis, false);
        view.getOutputArea().appendText("\n▣ Patient specifically denies any history of anaphylaxis or life-threatening reactions.\n");
        updateOutputFromSelection();
    }

    private void uncheckAll() {
        traverseAndSet(view.getSymptomTreeTable().getRoot(), false);
    }

    private void traverseAndSet(TreeItem<SymptomItem> node, boolean value) {
        if (node.getValue() != null && !node.getValue().getSymptom().isEmpty()) {
            node.getValue().setSelected(value);
        }
        node.getChildren().forEach(child -> traverseAndSet(child, value));
    }

    private void traverseAndSetIf(TreeItem<SymptomItem> node, Predicate<SymptomItem> predicate, boolean value) {
        if (node.getValue() != null && predicate.test(node.getValue())) {
            node.getValue().setSelected(value);
        }
        node.getChildren().forEach(child -> traverseAndSetIf(child, predicate, value));
    }

    private void copyToClipboard() {
        ClipboardContent content = new ClipboardContent();
        content.putString(view.getOutputArea().getText());
        Clipboard.getSystemClipboard().setContent(content);
        showAlert("Copied!", "Allergy note copied to clipboard.");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.show();
    }

    private void scrollToBottom() {
        view.getOutputArea().setScrollTop(Double.MAX_VALUE);
    }

    // --- Text Templates ---

    private String getDefaultTemplate() {
        return String.format("""
            ▣ Allergy History (%s)

            ▣ Known Allergies: None reported as of %s
            ▣ Patient denies any history of anaphylaxis.
            ▣ No known drug, food, or environmental allergies.

            --------------------------------------------------
            Detailed symptom assessment performed.
            """, currentDate, currentDate);
    }

    private String getDenyAllTemplate() {
        return String.format("""
            ▣ Allergy History (%s)

            Patient explicitly denies ALL allergic symptoms including:
            • Skin reactions, swelling, respiratory distress
            • Gastrointestinal symptoms
            • Anaphylactic symptoms (airway, BP, consciousness)

            No known drug/food/environmental allergies at this time.
            """, currentDate);
    }
}
