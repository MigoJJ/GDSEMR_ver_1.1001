package com.emr.gds.main.thyroid;

import com.emr.gds.main.service.EmrBridgeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thyroid-in-pregnancy helper UI (JavaFX) that merges previous Swing helpers
 * into a single pane and bridges into the EMR text areas via EmrBridgeService.
 *
 * Starting page shows common conditions; form allows quick CC/A/P insertion.
 */
public class ThyroidPregnancy extends VBox {

    private static final String[] QUICK_BUTTONS = {
            "New Patient for Pregnancy with Thyroid disease",
            "F/U Pregnancy with Normal Thyroid Function (TAb+)",
            "Infertility and Thyroid Function Evaluation",
            "F/U Pregnancy with Hyperthyroidism",
            "F/U Pregnancy with TSH low (Hyperthyroidism/GTT)",
            "F/U Pregnancy with Hypothyroidism",
            "F/U Pregnancy with TSH elevation (Subclinical Hypothyroidism)",
            "Postpartum Thyroiditis",
            "Support Files",
            "Quit"
    };

    private static final Map<String, String> DIAGNOSIS_CODES = new LinkedHashMap<>();
    private static final Map<String, String> HOSPITAL_CODES = new LinkedHashMap<>();

    static {
        DIAGNOSIS_CODES.put("o", "Hypothyroidism diagnosed");
        DIAGNOSIS_CODES.put("e", "Hyperthyroidism diagnosed");
        DIAGNOSIS_CODES.put("n", "TFT abnormality");

        HOSPITAL_CODES.put("c", "청담마리 산부인과");
        HOSPITAL_CODES.put("d", "도곡함춘 산부인과");
        HOSPITAL_CODES.put("o", "기타 산부인과");
    }

    private final TextField pregNumberField = new TextField();
    private final TextField weeksField = new TextField();
    private final TextField dueDateField = new TextField();
    private final TextField diagnosisCodeField = new TextField();
    private final TextField transferCodeField = new TextField();
    private final TextArea previewArea = new TextArea();

    private final EmrBridgeService bridgeService = new EmrBridgeService();

    public ThyroidPregnancy() {
        setPadding(new Insets(12));
        setSpacing(10);

        getChildren().addAll(
                buildConditionOverview(),
                new Separator(),
                buildForm(),
                buildQuickButtons(),
                buildPreview()
        );
        VBox.setVgrow(previewArea, Priority.ALWAYS);
    }

    private VBox buildConditionOverview() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(6, 6, 2, 6));

        box.getChildren().add(new Label("Thyroid Disorders in Pregnancy"));

        box.getChildren().add(sectionLabel("Hypothyroidism",
                "Overt Hypothyroidism",
                "Subclinical Hypothyroidism",
                "Isolated Maternal Hypothyroxinemia",
                "Hashimoto's Thyroiditis"));

        box.getChildren().add(sectionLabel("Hyperthyroidism",
                "Graves' Disease",
                "Gestational Transient Thyrotoxicosis (GTT)",
                "Hyperemesis Gravidarum (Thyroid-associated)",
                "Subclinical Hyperthyroidism"));

        box.getChildren().add(sectionLabel("Postpartum Conditions",
                "Postpartum Thyroiditis (Thyrotoxic and Hypothyroid phases)"));

        box.getChildren().add(sectionLabel("Structural Changes",
                "Goiter (Thyromegaly)",
                "Thyroid Nodules"));

        return box;
    }

    private VBox sectionLabel(String title, String... bullets) {
        VBox box = new VBox(2);
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(header);
        for (String bullet : bullets) {
            box.getChildren().add(new Label(" • " + bullet));
        }
        return box;
    }

    private GridPane buildForm() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        pregNumberField.setPromptText("e.g. 1");
        weeksField.setPromptText("e.g. 24");
        dueDateField.setPromptText("YYYY-MM-DD");
        diagnosisCodeField.setPromptText("Diagnosis code (o/e/n)");
        transferCodeField.setPromptText("Transfer code (c/d/o)");

        int row = 0;
        grid.add(new Label("Pregnancy #"), 0, row);
        grid.add(pregNumberField, 1, row++);

        grid.add(new Label("Weeks"), 0, row);
        grid.add(weeksField, 1, row++);

        grid.add(new Label("Due Date"), 0, row);
        grid.add(dueDateField, 1, row++);

        grid.add(new Label("Diagnosis code"), 0, row);
        grid.add(diagnosisCodeField, 1, row++);

        grid.add(new Label("Transferred from GY code"), 0, row);
        grid.add(transferCodeField, 1, row++);

        Button buildBtn = new Button("Build & Send to EMR");
        buildBtn.setOnAction(e -> {
            String formatted = formatPregnancyData();
            previewArea.setText(formatted);
            sendToEmr(formatted);
        });
        grid.add(buildBtn, 1, row);

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
        previewArea.setEditable(false);
        box.getChildren().addAll(lbl, previewArea);
        return box;
    }

    private void handleQuickAction(String label) {
        if ("Quit".equals(label)) {
            Stage stage = (Stage) getScene().getWindow();
            stage.close();
            return;
        }
        if ("Support Files".equals(label)) {
            previewArea.setText("Support files action not wired in this skeleton.");
            return;
        }

        if (label.startsWith("New Patient")) {
            String formatted = formatPregnancyData();
            previewArea.setText(formatted);
            sendToEmr(formatted);
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String baseCondition = label.replace("F/U ", "");

        String ccText = String.format("F/U [   ] weeks    %s%n\t%s", currentDate, baseCondition);
        String aText = String.format("%n  #  %s  [%s]", label, currentDate);
        String pText = String.format("...Plan F/U [   ] weeks%n\t %s", baseCondition);

        previewArea.setText(ccText + "\n" + aText + "\n" + pText);
        sendToEmrSections(ccText, aText, pText);
    }

    private String formatPregnancyData() {
        String pregNum = pregNumberField.getText().trim();
        String weeks = weeksField.getText().trim();
        String dueDate = dueDateField.getText().trim();
        String diag = convertCode(diagnosisCodeField.getText().trim(), DIAGNOSIS_CODES);
        String hospital = convertCode(transferCodeField.getText().trim(), HOSPITAL_CODES);

        return String.format("# %s pregnancy  %s weeks  Due-date %s%n\t%s at %s",
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
        return map.getOrDefault(code.toLowerCase(), "Unknown code: " + code);
    }

    private void sendToEmr(String block) {
        // default: CC and Assessment, and Plan as echo
        sendToEmrSections(block, block, block);
    }

    private void sendToEmrSections(String ccBlock, String aBlock, String pBlock) {
        bridgeService.insertBlock(0, ccBlock); // CC
        bridgeService.insertBlock(7, aBlock);  // Assessment
        bridgeService.insertBlock(8, pBlock);  // Plan
    }

    /**
     * Convenience launcher to open in its own window.
     */
    public static void openInNewWindow() {
        Stage stage = new Stage();
        ThyroidPregnancy root = new ThyroidPregnancy();
        stage.setTitle("Thyroid Pregnancy");
        stage.setScene(new Scene(root, 620, 720));
        stage.show();
    }
}
