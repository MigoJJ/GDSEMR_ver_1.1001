package com.emr.gds.main.template;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateEditController {

    public static final String[] TEXT_AREA_TITLES = {
            "CC>", "PI>", "ROS>", "PMH>", "S>",
            "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };

    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "^\\s*(CC>|PI>|ROS>|PMH>|S>|O>|Physical Exam>|A>|P>|Comment>)\\s*(.*)$"
    );

    @FXML private TableView<TemplateModel> templateTable;
    @FXML private TableColumn<TemplateModel, String> nameColumn;
    @FXML private TextField nameField;
    @FXML private TextArea contentArea;

    private TemplateRepository repository;
    private Consumer<String> onUseCallback;
    private TemplateModel selectedTemplate;

    public void setRepository(TemplateRepository repository) {
        this.repository = repository;
        loadTemplates();
    }

    public void setOnUseCallback(Consumer<String> callback) {
        this.onUseCallback = callback;
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        templateTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectTemplate(newVal);
            }
        });
    }

    private void loadTemplates() {
        if (repository == null) return;
        List<TemplateModel> templates = repository.getAllTemplates();
        ObservableList<TemplateModel> data = FXCollections.observableArrayList(templates);
        templateTable.setItems(data);
    }

    private void selectTemplate(TemplateModel template) {
        this.selectedTemplate = template;
        nameField.setText(template.getName());
        contentArea.setText(template.getContent());
    }

    @FXML
    private void handleNew() {
        templateTable.getSelectionModel().clearSelection();
        selectedTemplate = null;
        nameField.clear();
        contentArea.clear();
        nameField.requestFocus();
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String content = contentArea.getText();

        if (name.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Template name cannot be empty.");
            return;
        }

        if (repository == null) return;

        if (selectedTemplate == null) {
            repository.createTemplate(name, content);
        } else {
            repository.updateTemplate(selectedTemplate.getId(), name, content);
        }
        
        loadTemplates();
        handleNew(); // Reset selection after save
    }

    @FXML
    private void handleDelete() {
        if (selectedTemplate == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a template to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText(null);
        alert.setContentText("Delete this template?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repository.deleteTemplate(selectedTemplate.getId());
            loadTemplates();
            handleNew();
        }
    }

    @FXML
    private void handleUse() {
        String rawContent = contentArea.getText();
        if (rawContent.isEmpty()) return;

        LinkedHashMap<String, List<String>> sections = parseSections(rawContent);
        String finalOutput = buildOrderedOutput(sections);

        if (onUseCallback != null) {
            onUseCallback.accept(finalOutput);
        }

        // Close the window
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // --- Parser Logic (Ported from IAFMainEdit) ---

    private LinkedHashMap<String, List<String>> parseSections(String content) {
        LinkedHashMap<String, List<String>> sections = new LinkedHashMap<>();
        for (String title : TEXT_AREA_TITLES) {
            sections.put(title, new ArrayList<>());
        }
        String currentSection = null;
        for (String line : content.split("\\r?\\n", -1)) {
            Matcher m = HEADER_PATTERN.matcher(line);
            if (m.matches()) {
                currentSection = m.group(1);
                String afterHeader = m.group(2).trim();
                if (!afterHeader.isEmpty()) {
                    sections.get(currentSection).add(afterHeader);
                }
            } else if (currentSection != null) {
                sections.get(currentSection).add(line);
            } else {
                // If content appears before any header, put it in comments or ignore
                if (sections.containsKey("Comment>")) {
                    sections.get("Comment>").add(line);
                }
            }
        }
        return sections;
    }

    private String buildOrderedOutput(LinkedHashMap<String, List<String>> sections) {
        StringBuilder out = new StringBuilder();
        // Fixed order of sections in the EMR
        List<String> order = Arrays.asList("CC>", "PI>", "PMH>", "S>", "ROS>", "O>", "Physical Exam>", "A>", "P>", "Comment>");

        for (String label : order) {
            List<String> lines = sections.getOrDefault(label, Collections.emptyList());
            if (lines.isEmpty() || lines.stream().allMatch(String::isBlank)) continue;

            out.append(label);
            String firstLineContent = lines.get(0).trim();
            if (!firstLineContent.isEmpty()) {
                out.append(' ').append(firstLineContent);
            }
            out.append('\n');

            for (int i = 1; i < lines.size(); i++) {
                out.append("\t").append(lines.get(i)).append('\n');
            }
        }
        return out.toString().trim();
    }
}
