package com.emr.gds.main.thyroid;

import com.emr.gds.main.service.EmrBridgeService;
import com.emr.gds.util.StageSizing;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thyroid-in-pregnancy helper UI (JavaFX) that merges previous Swing helpers
 * into a single pane and bridges into the EMR text areas via EmrBridgeService.
 *
 * Starting page shows common conditions; form allows quick CC/A/P insertion.
 */
public class ThyroidPregnancy extends BorderPane {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. CONSTANTS & STATIC DATA
    // ─────────────────────────────────────────────────────────────────────────

    private static final String[] QUICK_BUTTONS = {
            " Overt Hypothyroidism",
            " Subclinical Hypothyroidism",
            " Isolated Maternal Hypothyroxinemia",
            " Hashimoto's Thyroiditis",
            " Graves' Disease",
            " Gestational Transient Thyrotoxicosis (GTT)",
            " Hyperemesis Gravidarum (Thyroid-associated)",
            " Subclinical Hyperthyroidism",
            " Postpartum Thyroiditis (Thyrotoxic and Hypothyroid phases)",
            " Goiter (Thyromegaly)",
            " Thyroid Nodules",
            "Reference Table",
            "Support Files",
            "Quit"
    };

    private static final Map<String, String> DIAGNOSIS_CODES = new LinkedHashMap<>();
    private static final Map<String, String> HOSPITAL_CODES = new LinkedHashMap<>();

    static {
        DIAGNOSIS_CODES.put("o", "Overt Hypothyroidism");
        DIAGNOSIS_CODES.put("s", "Subclinical Hypothyroidism");
        DIAGNOSIS_CODES.put("i", "Isolated Maternal Hypothyroxinemia");
        DIAGNOSIS_CODES.put("hash", "Hashimoto's Thyroiditis");
        DIAGNOSIS_CODES.put("g", "Graves' Disease");
        DIAGNOSIS_CODES.put("gtt", "Gestational Transient Thyrotoxicosis (GTT)");
        DIAGNOSIS_CODES.put("he", "Hyperemesis Gravidarum (Thyroid-associated)");
        DIAGNOSIS_CODES.put("sh", "Subclinical Hyperthyroidism");
        DIAGNOSIS_CODES.put("ppt", "Postpartum Thyroiditis (Thyrotoxic and Hypothyroid phases)");
        DIAGNOSIS_CODES.put("goi", "Goiter (Thyromegaly)");
        DIAGNOSIS_CODES.put("nod", "Thyroid Nodules");

        HOSPITAL_CODES.put("c", "청담마리 산부인과");
        HOSPITAL_CODES.put("d", "도곡함춘 산부인과");
        HOSPITAL_CODES.put("o", "기타 산부인과");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. FIELDS (UI COMPONENTS & SERVICES)
    // ─────────────────────────────────────────────────────────────────────────

    // Form Fields
    private final TextField pregNumberField = new TextField();
    private final TextField weeksField = new TextField();
    private final TextField dueDateField = new TextField();
    private final TextField diagnosisCodeField = new TextField();
    private final TextField transferCodeField = new TextField();
    
    // Display/Input Areas
    private final TextArea previewArea = new TextArea();
    private final List<CheckBox> conditionCheckBoxes = new ArrayList<>();

    // Services
    private final EmrBridgeService bridgeService = new EmrBridgeService();

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CONSTRUCTOR & ENTRY POINTS
    // ─────────────────────────────────────────────────────────────────────────

    public ThyroidPregnancy() {
        initializeUI();
    }

    /**
     * Convenience launcher to open in its own window.
     */
    public static void openInNewWindow() {
        Stage stage = new Stage();
        ThyroidPregnancy root = new ThyroidPregnancy();
        stage.setTitle("Thyroid Pregnancy");
        stage.setScene(new Scene(root));
        // Reduced width to ~60% of screen
        StageSizing.fitToScreen(stage, 0.6, 0.9);
        stage.show();
    }

    private void initializeUI() {
        setPadding(new Insets(12));

        // Left Panel (Conditions)
        VBox leftPane = buildConditionOverview();
        setLeft(leftPane);

        // Center Panel (Form, Quick Buttons, Preview)
        VBox centerPane = new VBox(10);
        centerPane.setPadding(new Insets(0, 0, 0, 15));
        centerPane.getChildren().addAll(
                buildForm(),
                buildQuickButtons(),
                buildPreview()
        );
        VBox.setVgrow(previewArea, Priority.ALWAYS);

        setCenter(centerPane);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. UI BUILDER METHODS
    // ─────────────────────────────────────────────────────────────────────────

    private VBox buildConditionOverview() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(6, 6, 2, 6));

        // Hypothyroidism Section
        box.getChildren().add(createSectionLabel("Hypothyroidism"));
        box.getChildren().addAll(
                createConditionCheckbox("Overt Hypothyroidism"),
                createConditionCheckbox("Subclinical Hypothyroidism"),
                createConditionCheckbox("Isolated Maternal Hypothyroxinemia"),
                createConditionCheckbox("Hashimoto’s Thyroiditis")
        );
        box.getChildren().add(new Separator());

        // Hyperthyroidism Section
        box.getChildren().add(createSectionLabel("Hyperthyroidism"));
        box.getChildren().addAll(
                createConditionCheckbox("Graves’ Disease"),
                createConditionCheckbox("Gestational Transient Thyrotoxicosis (GTT)"),
                createConditionCheckbox("Hyperemesis Gravidarum (Thyroid-associated)"),
                createConditionCheckbox("Subclinical Hyperthyroidism")
        );
        box.getChildren().add(new Separator());

        // Postpartum Section
        box.getChildren().add(createSectionLabel("Postpartum Conditions"));
        box.getChildren().add(createConditionCheckbox("Postpartum Thyroiditis (Thyrotoxic & Hypothyroid phases)"));
        box.getChildren().add(new Separator());

        // Structural Changes Section
        box.getChildren().add(createSectionLabel("Structural Changes"));
        box.getChildren().addAll(
                createConditionCheckbox("Goiter (Thyromegaly)"),
                createConditionCheckbox("Thyroid Nodules")
        );

        return box;
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        // Setup text fields
        pregNumberField.setPromptText("e.g. 1");
        weeksField.setPromptText("e.g. 24");
        dueDateField.setPromptText("YYYY-MM-DD");
        diagnosisCodeField.setPromptText("Diagnosis code (o/e/n)");
        transferCodeField.setPromptText("Transfer code (c/d/o)");

        // Layout rows
        int row = 0;
        grid.addRow(row++, new Label("Pregnancy #"), pregNumberField);
        grid.addRow(row++, new Label("Weeks"), weeksField);
        grid.addRow(row++, new Label("Due Date"), dueDateField);
        grid.addRow(row++, new Label("Diagnosis code"), diagnosisCodeField);
        grid.addRow(row++, new Label("Transferred from GY code"), transferCodeField);

        // Buttons
        Button buildBtn = new Button("Build & Send to EMR");
        buildBtn.setOnAction(e -> handleBuildAndSend());

        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> handleClearAllFields());

        HBox buttonBox = new HBox(10, buildBtn, clearBtn);
        grid.add(buttonBox, 1, row);

        return grid;
    }

    private VBox buildQuickButtons() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(6, 0, 6, 0));

        for (String text : QUICK_BUTTONS) {
            Button btn = new Button(text);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> handleQuickAction(text));
            box.getChildren().add(btn);
        }
        return box;
    }

    private VBox buildPreview() {
        VBox box = new VBox(4);
        Label lbl = new Label("Preview / Last sent");
        
        previewArea.setWrapText(true);
        previewArea.setEditable(true);

        Button savePreviewBtn = new Button("Save Preview to EMR");
        savePreviewBtn.setMaxWidth(Double.MAX_VALUE);
        
        savePreviewBtn.setOnAction(e -> {
            String content = previewArea.getText();
            // Split into CC and Assessment based on the line starting with "#"
            // Regex looks for a newline followed by optional whitespace and then "#"
            String[] parts = content.split("(?=\\n\\s*#)", 2);
            
            if (parts.length > 1) {
                bridgeService.insertBlock(0, parts[0].trim()); // CC
                bridgeService.insertBlock(7, parts[1].trim()); // Assessment
            } else {
                // Fallback: if no split found, send everything to Assessment
                bridgeService.insertBlock(7, content);
            }
            handleClearAllFields();
            closeWindow();
        });

        box.getChildren().addAll(lbl, previewArea, savePreviewBtn);
        return box;
    }

    private VBox createSectionLabel(String title, String... bullets) {
        VBox box = new VBox(2);
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(header);
        for (String bullet : bullets) {
            box.getChildren().add(new Label(" • " + bullet));
        }
        return box;
    }

    private CheckBox createConditionCheckbox(String name) {
        CheckBox cb = new CheckBox(name);
        conditionCheckBoxes.add(cb);
        cb.setOnAction(e -> updateDiagnosisField());
        return cb;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. ACTION HANDLERS
    // ─────────────────────────────────────────────────────────────────────────

    private void handleBuildAndSend() {
        String formatted = formatPregnancyData();
        previewArea.setText(formatted);
        sendToEmr(formatted);
        
        // Close the window after sending
        Stage stage = (Stage) getScene().getWindow();
        if (stage != null) stage.close();
    }

    private void handleClearAllFields() {
        pregNumberField.clear();
        weeksField.clear();
        dueDateField.clear();
        diagnosisCodeField.clear();
        transferCodeField.clear();
        previewArea.clear();
        
        for (CheckBox cb : conditionCheckBoxes) {
            cb.setSelected(false);
        }
    }

    private void handleQuickAction(String label) {
        if ("Quit".equals(label)) {
            closeWindow();
            return;
        }
        if ("Reference Table".equals(label)) {
            openConditionTableWindow();
            return;
        }
        if ("Support Files".equals(label)) {
            previewArea.setText("Support files action not wired in this skeleton.");
            return;
        }
        if (label.startsWith("New Patient")) {
            handleBuildAndSend();
            return;
        }

        // Generate text for standard condition buttons
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String baseCondition = label.replace("F/U ", "");

        String ccText = String.format("F/U Pregnancy [   ] weeks    %s%n\t%s", currentDate, baseCondition);
        String aText = String.format("#  %s  [%s]%n\t ...Plan F/U pregnancy [   ] weeks", baseCondition, currentDate);

        previewArea.setText(ccText +"\n" + aText);
        sendToEmrSections("", "", "");
 
    }

    private void closeWindow() {
        Stage stage = (Stage) getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. REFERENCE WINDOW (CONDITION TABLE)
    // ─────────────────────────────────────────────────────────────────────────

    private void openConditionTableWindow() {
        Stage stage = new Stage();
        stage.setTitle("Thyroid Pregnancy Condition Reference");
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(buildConditionTable());
        VBox.setVgrow(root.getChildren().get(0), Priority.ALWAYS);
        
        stage.setScene(new Scene(root));
        stage.setWidth(400);
        stage.setHeight(600);
        stage.show();
    }

    private TableView<ThyroidPregnancyConditionRow> buildConditionTable() {
        TableView<ThyroidPregnancyConditionRow> table = new TableView<>();

        // Columns
        TableColumn<ThyroidPregnancyConditionRow, String> conditionCol = new TableColumn<>("Condition");
        conditionCol.setCellValueFactory(new PropertyValueFactory<>("condition"));

        TableColumn<ThyroidPregnancyConditionRow, String> tshCol = new TableColumn<>("TSH");
        tshCol.setCellValueFactory(new PropertyValueFactory<>("tsh"));

        TableColumn<ThyroidPregnancyConditionRow, String> ft4Col = new TableColumn<>("Free T4");
        ft4Col.setCellValueFactory(new PropertyValueFactory<>("ft4"));

        TableColumn<ThyroidPregnancyConditionRow, String> abCol = new TableColumn<>("Antibodies");
        abCol.setCellValueFactory(new PropertyValueFactory<>("antibodies"));

        TableColumn<ThyroidPregnancyConditionRow, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().addAll(conditionCol, tshCol, ft4Col, abCol, notesCol);

        // Data
        ObservableList<ThyroidPregnancyConditionRow> items = FXCollections.observableArrayList(
                new ThyroidPregnancyConditionRow("Overt Hypothyroidism", "↑ (임신 삼분기 상한치 초과, 예: >4.0)", "↓", "TPOAb ±, TgAb ±", "명백한 기능저하, LT4 치료 필요"),
                new ThyroidPregnancyConditionRow("Subclinical Hypothyroidism", "↑", "정상", "TPOAb ±", "무증상/경도, TPOAb·TSH 수준 따라 치료"),
                new ThyroidPregnancyConditionRow("Isolated Maternal Hypothyroxinemia", "정상", "↓ (정상 하한 미만)", "대개 음성", "TSH 정상, Free T4만 감소; 가이드라인별 관리 다름"),
                new ThyroidPregnancyConditionRow("Hashimoto’s Thyroiditis", "정상~↑", "정상 또는 ↓", "TPOAb/TgAb 양성", "자가면역; 임신 중/산후 기능저하 악화 가능"),
                new ThyroidPregnancyConditionRow("Graves’ Disease", "현저히 ↓ 또는 미측정", "↑ (Free T4/T3)", "TRAb 양성", "diffuse goiter, +/- 안병증; 태아·신생아 영향 가능"),
                new ThyroidPregnancyConditionRow("Gestational Transient Thyrotoxicosis (GTT)", "↓", "경도~중등도 ↑", "TRAb 음성", "임신 1기, hCG 관련 일과성; 대개 자연 호전"),
                new ThyroidPregnancyConditionRow("Hyperemesis Gravidarum (thyroid-associated)", "↓", "↑ (정도 다양)", "대개 음성", "심한 구토·체중감소 동반; 구토 호전 시 갑상선 수치도 호전"),
                new ThyroidPregnancyConditionRow("Subclinical Hyperthyroidism", "경도 ↓", "정상", "대개 음성", "무증상; 임신 초기 생리적 TSH 감소와 감별 필요, 대개 관찰"),
                new ThyroidPregnancyConditionRow("Postpartum Thyroiditis (PPT)", "초기: ↓ → 후반: ↑", "초기: ↑ → 후반: ↓", "TPOAb 양성 (TRAb 음성)", "산후 1년 이내, thyrotoxic → hypothyroid → 회복(또는 영구 저하)")
        );

        table.setItems(items);
        return table;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. BUSINESS LOGIC & HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void updateDiagnosisField() {
        List<String> selected = new ArrayList<>();
        for (CheckBox cb : conditionCheckBoxes) {
            if (cb.isSelected()) {
                selected.add(cb.getText());
            }
        }
        diagnosisCodeField.setText(String.join(", ", selected));
    }

    private String formatPregnancyData() {
        String pregNum = pregNumberField.getText().trim();
        String weeks = weeksField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String diag = convertCode(diagnosisCodeField.getText().trim(), DIAGNOSIS_CODES);
        String hospital = convertCode(transferCodeField.getText().trim(), HOSPITAL_CODES);

        return String.format("#  [ %s ]  pregnancy  [ %s ] weeks  Due-date %s%n	%s at %s",
                pregNum,
                weeks,
                dueDate,
                diag,
                hospital);
    }

    private String convertCode(String code, Map<String, String> map) {
        if (code == null || code.isBlank()) {
            return "Unknown";
        }
        return map.getOrDefault(code.toLowerCase(), code);
    }

    private void sendToEmr(String block) {
        // Send block to both CC and Assessment as default
        sendToEmrSections(block, block, block);
    }

    private void sendToEmrSections(String ccBlock, String aBlock, String pBlock) {
        bridgeService.insertBlock(0, ccBlock); // CC
        bridgeService.insertBlock(7, aBlock);  // Assessment
        // bridgeService.insertBlock(8, pBlock);  // Plan (commented out in original)

    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. INNER CLASSES
    // ─────────────────────────────────────────────────────────────────────────

    public static class ThyroidPregnancyConditionRow {
        private final SimpleStringProperty condition;
        private final SimpleStringProperty tsh;
        private final SimpleStringProperty ft4;
        private final SimpleStringProperty antibodies;
        private final SimpleStringProperty notes;

        public ThyroidPregnancyConditionRow(String condition, String tsh, String ft4, String antibodies, String notes) {
            this.condition = new SimpleStringProperty(condition);
            this.tsh = new SimpleStringProperty(tsh);
            this.ft4 = new SimpleStringProperty(ft4);
            this.antibodies = new SimpleStringProperty(antibodies);
            this.notes = new SimpleStringProperty(notes);
        }

        public String getCondition() { return condition.get(); }
        public String getTsh() { return tsh.get(); }
        public String getFt4() { return ft4.get(); }
        public String getAntibodies() { return antibodies.get(); }
        public String getNotes() { return notes.get(); }
    }
}
