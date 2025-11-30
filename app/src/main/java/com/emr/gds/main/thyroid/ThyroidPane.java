package com.emr.gds.main.thyroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.emr.gds.main.medication.controller.MainController;
import com.emr.gds.util.StageSizing;
import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX form for Thyroid EMR entry.
 * Enhanced for Endocrinologists with TI-RADS, ATA Risk, and Dose Calculators.
 */
public class ThyroidPane extends VBox {

    private static final Map<String, List<ThyroidEntry.Symptom>> SYMPTOM_GROUPS = buildSymptomGroups();
    private static final String[][] EXAM_SECTIONS = {
            {"Goiter Ruled", "Goiter ruled out", "Goiter ruled in Diffuse Enlargement",
                    "Goiter ruled in Nodular Enlargement", "Single Nodular Goiter", "Multiple Nodular Goiter"},
            {"Detect any nodules", "None", "Single nodule", "Multinodular Goiter"},
            {"Thyroid gland consistency", "Soft", "Soft to Firm", "Firm", "Cobble-stone", "Firm to Hard", "Hard"},
            {"Evaluate the thyroid gland for tenderness", "Tender", "Non-tender"},
            {"Systolic or continuous Bruit (y/n)", "Yes", "No"},
            {"DTR deep tendon reflex", "1+ = present but depressed", "2+ = normal / average",
                    "3+ = increased", "4+ = clonus", "Doctor has not performed DTR test"},
            {"TED: Thyroid Eye Disease", "Class 0: No signs or symptoms",
                    "Class 1: Only signs", "Class 2: Soft tissue involvement",
                    "Class 3: Proptosis", "Class 4: Extraocular muscle involvement",
                    "Class 5: Corneal involvement", "Class 6: Sight loss"}
    };
    private static final String EXAM_SEPARATOR = "--------------------------------------";
    private static final String EXAM_HEADER = "< Thyroid Physical Exam >";
    private static final Map<String, String> EXAM_LABELS = Map.of(
            "Goiter Ruled", "Goiter",
            "Detect any nodules", "Nodules",
            "Thyroid gland consistency", "Consistency",
            "Evaluate the thyroid gland for tenderness", "Tenderness",
            "Systolic or continuous Bruit (y/n)", "Systolic or continuous Bruit (y/n)",
            "DTR deep tendon reflex", "DTR",
            "TED: Thyroid Eye Disease", "TED"
    );
    private static final Map<String, String[]> CONDITION_GROUPS = buildConditionGroups();

    private final ThyroidEntry entry;
    private final LinkedHashMap<String, List<CheckBox>> examSectionMap = new LinkedHashMap<>();
    private final Map<String, List<CheckBox>> conditionGroupMap = new LinkedHashMap<>();
    private final Map<ThyroidEntry.Symptom, CheckBox> symptomCheckboxes = new LinkedHashMap<>();

    // --- UI Controls ---

    // Overview
    private final ComboBox<ThyroidEntry.VisitType> cmbVisitType = new ComboBox<>();
    private final TextField txtWeight = new TextField(); // Patient weight for dose calc

    private final CheckBox chkHypo = new CheckBox("Hypothyroidism");
    private final CheckBox chkHyper = new CheckBox("Hyperthyroidism");
    private final CheckBox chkNodule = new CheckBox("Thyroid nodule");
    private final CheckBox chkCancer = new CheckBox("Thyroid cancer");
    private final CheckBox chkThyroiditis = new CheckBox("Thyroiditis");
    private final CheckBox chkGoiter = new CheckBox("Goiter");

    private final ComboBox<ThyroidEntry.HypoEtiology> cmbHypoEtiology = new ComboBox<>();
    private final ComboBox<ThyroidEntry.HyperEtiology> cmbHyperEtiology = new ComboBox<>();
    private final CheckBox chkHypoOvert = new CheckBox("Overt hypo");
    private final CheckBox chkHyperActive = new CheckBox("Active hyper");
    private final Label symptomSummary = new Label("No symptoms selected");
    private final TextField txtSymptomNegatives = new TextField();

    // Risk & Calculators (New)
    private final Label lblLt4Est = new Label("Est. LT4: -");
    
    // ATA Risk
    private final CheckBox chkGrossExt = new CheckBox("Gross Extrathyroidal Ext.");
    private final CheckBox chkIncomplete = new CheckBox("Incomplete Resection");
    private final CheckBox chkDistantMets = new CheckBox("Distant Mets");
    private final CheckBox chkAggressive = new CheckBox("Aggressive Histology");
    private final CheckBox chkVascularInv = new CheckBox("Vascular Invasion");
    private final TextField txtLymphCount = new TextField();
    private final TextField txtNodeSize = new TextField();
    private final Label lblAtaRisk = new Label("ATA Risk: Low");

    // TI-RADS
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbComp = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbEcho = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbShape = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbMargin = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbFoci = new ComboBox<>();
    private final Label lblTiRadsResult = new Label("TI-RADS: -");

    // Labs
    private final TextField txtTsh = new TextField();
    private final TextField txtFreeT4 = new TextField();
    private final TextField txtFreeT3 = new TextField();
    private final TextField txtTpoAb = new TextField();
    private final TextField txtTg = new TextField();
    private final TextField txtTgAb = new TextField();
    private final TextField txtTrab = new TextField();
    private final TextField txtCalcitonin = new TextField();
    private final DatePicker dpLastLabDate = new DatePicker();

    // Treatment
    private final TextField txtLt4Dose = new TextField();
    private final TextField txtAtdName = new TextField();
    private final TextField txtAtdDose = new TextField();
    private final TextField txtBetaBlockerName = new TextField();
    private final TextField txtBetaBlockerDose = new TextField();
    private final Button btnOpenEmrHelper = new Button("Open EMR Helper");

    // Follow-up
    private final ComboBox<String> cmbFollowUpInterval = new ComboBox<>();
    private final TextArea txtFollowUpPlan = new TextArea();
    private final TextArea txtSummaryOutput = new TextArea();
    private final Button btnGenerateSummary = new Button("Generate Specialist Summary");
    private final Button btnSaveQuit = new Button("Save and Quit");

    public ThyroidPane(ThyroidEntry entry) {
        this.entry = (entry != null) ? entry : new ThyroidEntry();
        initControls();
        buildLayout();
        configureActions();
    }

    private void initControls() {
        setSpacing(8);
        setPadding(new Insets(10));

        // Overview
        cmbVisitType.getItems().addAll(ThyroidEntry.VisitType.values());
        cmbVisitType.setPromptText("Visit type...");
        txtWeight.setPromptText("Weight (kg)");
        txtWeight.setPrefWidth(80);

        cmbHypoEtiology.getItems().addAll(ThyroidEntry.HypoEtiology.values());
        cmbHypoEtiology.setPromptText("Hypo etiology...");
        cmbHyperEtiology.getItems().addAll(ThyroidEntry.HyperEtiology.values());
        cmbHyperEtiology.setPromptText("Hyper etiology...");

        symptomSummary.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        symptomSummary.setWrapText(true);
        symptomSummary.setMaxWidth(500);
        txtSymptomNegatives.setPromptText("Recent negatives (e.g., denies tremor, weight loss)");

        // Risk - ATA
        txtLymphCount.setPromptText("# Nodes");
        txtNodeSize.setPromptText("Max size (cm)");
        lblAtaRisk.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        // Risk - TI-RADS
        cmbComp.getItems().setAll(
                ThyroidRiskCalculator.TiRadsFeature.COMP_CYSTIC_SPONGI,
                ThyroidRiskCalculator.TiRadsFeature.COMP_MIXED,
                ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID
        );
        cmbComp.setPromptText("Composition");

        cmbEcho.getItems().setAll(
                ThyroidRiskCalculator.TiRadsFeature.ECHO_ANECHOIC,
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPER_ISO,
                ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPO,
                ThyroidRiskCalculator.TiRadsFeature.ECHO_VERY_HYPO
        );
        cmbEcho.setPromptText("Echogenicity");

        cmbShape.getItems().setAll(
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER,
                ThyroidRiskCalculator.TiRadsFeature.SHAPE_TALLER
        );
        cmbShape.setPromptText("Shape");

        cmbMargin.getItems().setAll(
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH,
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_LOBULATED,
                ThyroidRiskCalculator.TiRadsFeature.MARGIN_EXTRA
        );
        cmbMargin.setPromptText("Margin");

        cmbFoci.getItems().setAll(
                ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE,
                ThyroidRiskCalculator.TiRadsFeature.FOCI_MACRO,
                ThyroidRiskCalculator.TiRadsFeature.FOCI_RIM,
                ThyroidRiskCalculator.TiRadsFeature.FOCI_PUNCTATE
        );
        cmbFoci.setPromptText("Echogenic Foci");
        lblTiRadsResult.setWrapText(true);
        lblTiRadsResult.setStyle("-fx-font-weight: bold; -fx-text-fill: #8e44ad;");

        // Labs
        txtTsh.setPromptText("TSH (uIU/mL)");
        txtFreeT4.setPromptText("fT4 (ng/dL)");
        txtFreeT3.setPromptText("fT3 (pg/mL)");
        txtTpoAb.setPromptText("TPOAb (IU/mL)");
        txtTg.setPromptText("Tg (ng/mL)");
        txtTgAb.setPromptText("TgAb (IU/mL)");
        txtTrab.setPromptText("TRAb (IU/L)");
        txtCalcitonin.setPromptText("Calcitonin (pg/mL)");
        dpLastLabDate.setPromptText("Date");

        // Treatment
        txtLt4Dose.setPromptText("LT4 (mcg)");
        txtAtdName.setPromptText("ATD Name");
        txtAtdDose.setPromptText("Dose (mg)");
        txtBetaBlockerName.setPromptText("BB Name");
        txtBetaBlockerDose.setPromptText("BB Dose");

        // Follow up
        cmbFollowUpInterval.getItems().addAll("3 months", "6 months", "12 months", "Custom");
        cmbFollowUpInterval.setPromptText("Interval");
        txtFollowUpPlan.setPromptText("Tests, Imaging, etc.");
        txtFollowUpPlan.setPrefRowCount(3);
        txtSummaryOutput.setPromptText("Specialist summary...");
        txtSummaryOutput.setWrapText(true);
        txtSummaryOutput.setPrefRowCount(8);
    }

    private void buildLayout() {
        TitledPane overviewPane = createOverviewPane();
        TitledPane riskPane = createRiskPane();
        TitledPane symptomsPane = createSymptomsPane();
        TitledPane examPane = createExamPane();
        TitledPane labsPane = createLabsPane();
        TitledPane treatmentPane = createTreatmentPane();
        TitledPane followUpPane = createFollowUpPane();

        Accordion accordion = new Accordion(overviewPane, riskPane, symptomsPane, examPane, labsPane, treatmentPane, followUpPane);
        accordion.setExpandedPane(overviewPane);

        getChildren().add(accordion);
        VBox.setVgrow(accordion, Priority.ALWAYS);
    }

    private TitledPane createOverviewPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        int row = 0;
        grid.add(new Label("Visit Type:"), 0, row);
        grid.add(cmbVisitType, 1, row);
        grid.add(new Label("Weight (kg):"), 2, row);
        grid.add(txtWeight, 3, row);
        grid.add(lblLt4Est, 4, row);
        row++;

        VBox hypoBox = new VBox(6,
                new Label("Hypothyroidism"),
                new HBox(10, chkHypo, cmbHypoEtiology, chkHypoOvert)
        );

        VBox hyperBox = new VBox(6,
                new Label("Hyperthyroidism"),
                new HBox(10, chkHyper, cmbHyperEtiology, chkHyperActive)
        );

        grid.add(new Label("Categories:"), 0, row);
        VBox catBox = new VBox(12,
                hypoBox,
                hyperBox,
                new Separator(),
                new HBox(10, chkNodule, chkCancer, chkThyroiditis, chkGoiter)
        );
        grid.add(catBox, 1, row, 4, 1);
        row++;

        Label conditionsLabel = new Label("Condition checklist:");
        conditionsLabel.setStyle("-fx-font-weight: bold;");
        grid.add(conditionsLabel, 0, row);
        grid.add(buildConditionChecklist(), 1, row, 4, 1);
        row++;

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportHeight(420);

        return styledPane("1. Overview & Patient", scrollPane);
    }

    private TitledPane createSymptomsPane() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label intro = new Label("Select common thyroid-related symptoms");
        intro.setStyle("-fx-font-weight: bold;");

        VBox symptomBox = new VBox(12);
        symptomBox.setFillWidth(true);

        SYMPTOM_GROUPS.forEach((group, symptoms) -> {
            Label groupLabel = new Label(group);
            groupLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d3d8f;");
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(6);

            for (int i = 0; i < symptoms.size(); i++) {
                ThyroidEntry.Symptom symptom = symptoms.get(i);
                CheckBox cb = symptomCheckboxes.computeIfAbsent(symptom, s -> new CheckBox(s.getLabel()));
                cb.setOnAction(e -> updateSymptomSummary());

                int col = i % 2;
                int row = i / 2;
                grid.add(cb, col, row);
            }

            symptomBox.getChildren().addAll(groupLabel, grid, new Separator());
        });

        updateSymptomSummary();

        VBox negativesBox = new VBox(4, new Label("Recent negatives / denials:"), txtSymptomNegatives);
        root.getChildren().addAll(intro, symptomBox, symptomSummary, negativesBox);
        return styledPane("3. Symptoms", root);
    }

    private TitledPane createRiskPane() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // TI-RADS Section
        Label lblTirads = new Label("ACR TI-RADS Calculator");
        lblTirads.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        GridPane tiradsGrid = new GridPane();
        tiradsGrid.setHgap(10); 
        tiradsGrid.setVgap(5);
        tiradsGrid.addRow(0, new Label("Composition:"), cmbComp);
        tiradsGrid.addRow(1, new Label("Echogenicity:"), cmbEcho);
        tiradsGrid.addRow(2, new Label("Shape:"), cmbShape);
        tiradsGrid.addRow(3, new Label("Margin:"), cmbMargin);
        tiradsGrid.addRow(4, new Label("Echogenic Foci:"), cmbFoci);
        
        HBox tiradsBox = new HBox(20, tiradsGrid, lblTiRadsResult);
        HBox.setHgrow(lblTiRadsResult, Priority.ALWAYS);
        lblTiRadsResult.setMaxWidth(300);

        // ATA Risk Section
        Label lblAta = new Label("ATA Risk Stratification (DTC)");
        lblAta.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        GridPane ataGrid = new GridPane();
        ataGrid.setHgap(15);
        ataGrid.setVgap(5);
        ataGrid.add(chkGrossExt, 0, 0);
        ataGrid.add(chkIncomplete, 1, 0);
        ataGrid.add(chkDistantMets, 2, 0);
        ataGrid.add(chkAggressive, 0, 1);
        ataGrid.add(chkVascularInv, 1, 1);
        
        HBox nodeBox = new HBox(5, new Label("Nodes #"), txtLymphCount, new Label("Max Size"), txtNodeSize);
        ataGrid.add(nodeBox, 0, 2, 3, 1);

        root.getChildren().addAll(lblTirads, tiradsBox, new Separator(), lblAta, ataGrid, lblAtaRisk);

        return styledPane("2. Risk Stratification & Tools", root);
    }

    private TitledPane createExamPane() {
        VBox left = new VBox(10);
        VBox right = new VBox(10);
        left.setFillWidth(true);
        right.setFillWidth(true);

        int midpoint = (int) Math.ceil(EXAM_SECTIONS.length / 2.0);
        for (int idx = 0; idx < EXAM_SECTIONS.length; idx++) {
            String[] section = EXAM_SECTIONS[idx];
            if (section.length < 2) continue;
            Label label = new Label(section[0] + ":");
            VBox sectionBox = new VBox(4);
            sectionBox.getChildren().add(label);
            List<CheckBox> sectionChecks = new ArrayList<>();

            for (int i = 1; i < section.length; i++) {
                CheckBox cb = new CheckBox(section[i]);
                cb.setOnAction(e -> updatePhysicalExamNotes());
                sectionChecks.add(cb);
                sectionBox.getChildren().add(cb);
            }
            examSectionMap.put(section[0], sectionChecks);

            sectionBox.setFillWidth(true);
            if (idx < midpoint) {
                left.getChildren().add(sectionBox);
            } else {
                right.getChildren().add(sectionBox);
            }
        }

        HBox split = new HBox(20, left, right);
        split.setPadding(new Insets(10));
        split.setFillHeight(true);

        return styledPane("4. Physical Exam", split);
    }

    private TitledPane createLabsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.addRow(0,
                new Label("TSH (uIU/mL)"), txtTsh,
                new Label("fT4 (ng/dL)"), txtFreeT4,
                new Label("fT3 (pg/mL)"), txtFreeT3);
        grid.addRow(1,
                new Label("TPOAb (IU/mL)"), txtTpoAb,
                new Label("Tg (ng/mL)"), txtTg,
                new Label("TgAb (IU/mL)"), txtTgAb);
        grid.addRow(2,
                new Label("TRAb (IU/L)"), txtTrab,
                new Label("Calcitonin (pg/mL)"), txtCalcitonin);
        grid.addRow(3, new Label("Date"), dpLastLabDate);

        return styledPane("5. Labs", grid);
    }

    private TitledPane createTreatmentPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Levothyroxine (mcg)"), txtLt4Dose);
        grid.addRow(1, new Label("Antithyroid Drug"), txtAtdName, new Label("Dose (mg)"), txtAtdDose);
        grid.addRow(2, new Label("Beta Blocker"), txtBetaBlockerName, new Label("Dose"), txtBetaBlockerDose);
        HBox helperBox = new HBox(btnOpenEmrHelper);
        helperBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(helperBox, 0, 3, 4, 1);

        return styledPane("6. Treatment", grid);
    }

    private TitledPane createFollowUpPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        HBox intBox = new HBox(10, new Label("Interval:"), cmbFollowUpInterval);
        intBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttons = new HBox(10, btnGenerateSummary, btnSaveQuit);

        box.getChildren().addAll(
            intBox,
            new Label("Plan details:"),
            txtFollowUpPlan,
            buttons,
            txtSummaryOutput
        );
        return styledPane("7. Plan & Summary", box);
    }

    // --- Logic & Actions ---

    private void configureActions() {
        // Real-time Weight Calc
        txtWeight.textProperty().addListener((obs, oldVal, newVal) -> updateDoseEst());
        txtSymptomNegatives.textProperty().addListener((obs, oldVal, newVal) -> updateSymptomSummary());
        btnOpenEmrHelper.setOnAction(e -> openEmrMedicationHelper());

        // Real-time TI-RADS
        cmbComp.setOnAction(e -> updateTiRads());
        cmbEcho.setOnAction(e -> updateTiRads());
        cmbShape.setOnAction(e -> updateTiRads());
        cmbMargin.setOnAction(e -> updateTiRads());
        cmbFoci.setOnAction(e -> updateTiRads());

        // Real-time ATA
        chkGrossExt.setOnAction(e -> updateAtaRisk());
        chkIncomplete.setOnAction(e -> updateAtaRisk());
        chkDistantMets.setOnAction(e -> updateAtaRisk());
        chkAggressive.setOnAction(e -> updateAtaRisk());
        chkVascularInv.setOnAction(e -> updateAtaRisk());
        txtLymphCount.textProperty().addListener(e -> updateAtaRisk());
        txtNodeSize.textProperty().addListener(e -> updateAtaRisk());

        btnGenerateSummary.setOnAction(e -> {
            mapUiToEntry();
            String summary = buildSpecialistSummary(entry);
            txtSummaryOutput.setText(summary);
            entry.setProblemListSummary(summary);
            updatePhysicalExamNotes();
        });

        btnSaveQuit.setOnAction(e -> {
            mapUiToEntry();
            String summaryText = txtSummaryOutput.getText().trim();
            if (summaryText.isBlank()) {
                summaryText = buildSpecialistSummary(entry);
                txtSummaryOutput.setText(summaryText);
            }

            updatePhysicalExamNotes();
            summaryText = txtSummaryOutput.getText().trim();
            txtFollowUpPlan.setText(summaryText);
            entry.setProblemListSummary(summaryText);
            final String finalSummary = summaryText;

            IAIMain.getManagerSafely().ifPresentOrElse(
                manager -> {
                    String textToAppend = finalSummary.endsWith("\n") ? finalSummary : finalSummary + "\n";
                    manager.appendTextToSection(IAITextAreaManager.AREA_PI, textToAppend);
                },
                () -> new Alert(
                        Alert.AlertType.ERROR,
                        "EMR mainframe is not connected.\nOpen this tool from the EMR to enable saving."
                ).showAndWait()
            );

            Stage stage = (Stage) btnSaveQuit.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
    }

    private void updateSymptomSummary() {
        List<String> selected = symptomCheckboxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(e -> e.getKey().getLabel())
                .toList();
        String negatives = (txtSymptomNegatives.getText() != null)
                ? txtSymptomNegatives.getText().trim()
                : "";
        boolean hasNegatives = !negatives.isBlank();
        if (selected.isEmpty() && !hasNegatives) {
            symptomSummary.setText("No symptoms selected");
            return;
        }

        List<String> parts = new ArrayList<>();
        if (!selected.isEmpty()) {
            parts.add("Positive: " + String.join(", ", selected));
        }
        if (hasNegatives) {
            parts.add("Negatives: " + negatives);
        }
        symptomSummary.setText(String.join(" | ", parts));
    }

    private void openEmrMedicationHelper() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/main/medication/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setSelectedCategory("Thyroid");

            Stage stage = new Stage();
            stage.setTitle("EMR Helper – Thyroid");
            stage.setScene(new Scene(root));
            StageSizing.fitToScreen(stage, 0.8, 0.9, 1100, 700);
            stage.show();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Unable to open EMR Helper: " + ex.getMessage()).showAndWait();
        }
    }

    private void updateDoseEst() {
        Double w = parseDoubleOrNull(txtWeight.getText());
        if (w != null) {
            double dose = ThyroidRiskCalculator.calculateFullReplacementDose(w);
            lblLt4Est.setText("Est. Full Dose: " + (int)dose + " mcg");
        } else {
            lblLt4Est.setText("Est. LT4: -");
        }
    }

    private void updateTiRads() {
        if (cmbComp.getValue() != null && cmbEcho.getValue() != null && 
            cmbShape.getValue() != null && cmbMargin.getValue() != null && 
            cmbFoci.getValue() != null) {
            
            ThyroidRiskCalculator.TiRadsResult res = ThyroidRiskCalculator.calculateTiRads(
                cmbComp.getValue(), cmbEcho.getValue(), cmbShape.getValue(), 
                cmbMargin.getValue(), cmbFoci.getValue()
            );
            lblTiRadsResult.setText(String.format("Score: %d\nLevel: %s\nRec: %s", 
                res.score, res.level, res.recommendation));
        }
    }

    private void updateAtaRisk() {
        int nodes = 0;
        double size = 0.0;
        try { nodes = Integer.parseInt(txtLymphCount.getText().trim()); } catch(Exception ignored){}
        try { size = Double.parseDouble(txtNodeSize.getText().trim()); } catch(Exception ignored){}

        String risk = ThyroidRiskCalculator.calculateAtaRisk(
            chkGrossExt.isSelected(),
            chkIncomplete.isSelected(),
            chkDistantMets.isSelected(),
            chkAggressive.isSelected(),
            chkVascularInv.isSelected(),
            nodes,
            size
        );
        lblAtaRisk.setText("ATA Risk: " + risk);
    }

    private void mapUiToEntry() {
        entry.setVisitType(cmbVisitType.getValue());
        entry.setPatientWeightKg(parseDoubleOrNull(txtWeight.getText()));

        var cats = new java.util.ArrayList<ThyroidEntry.MainCategory>();
        if (chkHypo.isSelected()) cats.add(ThyroidEntry.MainCategory.HYPOTHYROIDISM);
        if (chkHyper.isSelected()) cats.add(ThyroidEntry.MainCategory.HYPERTHYROIDISM);
        if (chkNodule.isSelected()) cats.add(ThyroidEntry.MainCategory.NODULE);
        if (chkCancer.isSelected()) cats.add(ThyroidEntry.MainCategory.CANCER);
        if (chkThyroiditis.isSelected()) cats.add(ThyroidEntry.MainCategory.THYROIDITIS);
        if (chkGoiter.isSelected()) cats.add(ThyroidEntry.MainCategory.GOITER);
        entry.setCategories(cats);
        List<ThyroidEntry.Symptom> selectedSymptoms = symptomCheckboxes.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
        entry.setSymptoms(selectedSymptoms);
        entry.setSymptomNegatives(emptyToNull(txtSymptomNegatives.getText()));

        entry.setHypoEtiology(cmbHypoEtiology.getValue());
        entry.setHypoOvert(chkHypoOvert.isSelected());
        entry.setHyperEtiology(cmbHyperEtiology.getValue());
        entry.setHyperActive(chkHyperActive.isSelected());

        // Risk Data
        entry.setGrossExtrathyroidalExtension(chkGrossExt.isSelected());
        entry.setIncompleteResection(chkIncomplete.isSelected());
        entry.setDistantMetastases(chkDistantMets.isSelected());
        entry.setAggressiveHistology(chkAggressive.isSelected());
        entry.setVascularInvasion(chkVascularInv.isSelected());
        try { entry.setLymphNodeCount(Integer.parseInt(txtLymphCount.getText().trim())); } catch(Exception e){ entry.setLymphNodeCount(0); }
        entry.setLargestNodeSizeCm(parseDoubleOrNull(txtNodeSize.getText()));
        entry.setAtaRisk(lblAtaRisk.getText().replace("ATA Risk: ", ""));

        // Labs
        entry.setTsh(parseDoubleOrNull(txtTsh.getText()));
        entry.setFreeT4(parseDoubleOrNull(txtFreeT4.getText()));
        entry.setFreeT3(parseDoubleOrNull(txtFreeT3.getText()));
        entry.setTpoAb(parseDoubleOrNull(txtTpoAb.getText()));
        entry.setTg(parseDoubleOrNull(txtTg.getText()));
        entry.setTgAb(parseDoubleOrNull(txtTgAb.getText()));
        entry.setTrab(parseDoubleOrNull(txtTrab.getText()));
        entry.setCalcitonin(parseDoubleOrNull(txtCalcitonin.getText()));
        entry.setLastLabDate(dpLastLabDate.getValue());

        // Meds
        entry.setLt4DoseMcgPerDay(parseDoubleOrNull(txtLt4Dose.getText()));
        entry.setAtdName(emptyToNull(txtAtdName.getText()));
        entry.setAtdDoseMgPerDay(parseDoubleOrNull(txtAtdDose.getText()));
        entry.setBetaBlockerName(emptyToNull(txtBetaBlockerName.getText()));
        entry.setBetaBlockerDose(emptyToNull(txtBetaBlockerDose.getText()));

        // Plan
        entry.setFollowUpInterval(cmbFollowUpInterval.getValue());
        entry.setFollowUpPlanText(txtFollowUpPlan.getText());
    }

    private String buildSpecialistSummary(ThyroidEntry e) {
        List<String> lines = new ArrayList<>();
        String visit = (e.getVisitType() != null)
                ? e.getVisitType() + " visit"
                : "Thyroid specialist evaluation";
        lines.add("* Visit: " + visit);

        if (e.getCategories().isEmpty()) {
            lines.add("     | Dx: Thyroid screening/evaluation");
        } else {
            List<String> dx = e.getCategories().stream().map(Object::toString).toList();
            lines.add("     | Dx: " + String.join(", ", dx));
        }

        Map<String, List<String>> selectedConditions = collectSelectedConditions();
        if (!selectedConditions.isEmpty()) {
            lines.add("     | Conditions checklist:");
            for (var entryGroup : selectedConditions.entrySet()) {
                lines.add("     |   " + entryGroup.getKey() + ": " + String.join("; ", entryGroup.getValue()));
            }
        }
        String negatives = (e.getSymptomNegatives() != null) ? e.getSymptomNegatives().trim() : "";
        boolean hasNegatives = !negatives.isBlank();
        if (!e.getSymptoms().isEmpty() || hasNegatives) {
            List<String> syms = e.getSymptoms().stream().map(ThyroidEntry.Symptom::getLabel).toList();
            StringBuilder symptomLine = new StringBuilder("     | Symptoms: ");
            if (!syms.isEmpty()) {
                symptomLine.append(String.join("; ", syms));
            } else {
                symptomLine.append("None reported");
            }
            if (hasNegatives) {
                symptomLine.append("; Recent negatives: ").append(negatives);
            }
            lines.add(symptomLine.toString());
        }

        List<String> statusParts = new ArrayList<>();
        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPOTHYROIDISM)) {
            StringBuilder hypoLine = new StringBuilder("Hypothyroidism ");
            if (e.getHypoEtiology() != null) hypoLine.append(e.getHypoEtiology()).append(". ");
            hypoLine.append(Boolean.TRUE.equals(e.isHypoOvert()) ? "Overt." : "Subclinical.");
            if (e.getLt4DoseMcgPerDay() != null) {
                hypoLine.append(" LT4 ").append(e.getLt4DoseMcgPerDay()).append(" mcg.");
                if (e.getPatientWeightKg() != null) {
                    double est = ThyroidRiskCalculator.calculateFullReplacementDose(e.getPatientWeightKg());
                    hypoLine.append(" Est ").append((int) est).append(" mcg.");
                }
            }
            statusParts.add(hypoLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPERTHYROIDISM)) {
            StringBuilder hyperLine = new StringBuilder("Hyperthyroidism ");
            if (e.getHyperEtiology() != null) hyperLine.append(e.getHyperEtiology()).append(". ");
            hyperLine.append(Boolean.TRUE.equals(e.isHyperActive()) ? "Uncontrolled/Active." : "Controlled/Remission.");
            if (e.getAtdName() != null) {
                hyperLine.append(" On ").append(e.getAtdName()).append(" ").append(e.getAtdDoseMgPerDay()).append(" mg.");
            }
            statusParts.add(hyperLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.CANCER)) {
            StringBuilder caLine = new StringBuilder("Thyroid Cancer ");
            if (e.getAtaRisk() != null && !e.getAtaRisk().equals("Low Risk")) {
                caLine.append(e.getAtaRisk()).append(" (path features). ");
            } else {
                caLine.append("Low Risk Stratification. ");
            }
            if (e.getTg() != null) caLine.append("Tg: ").append(e.getTg()).append(" ng/mL. ");
            statusParts.add(caLine.toString().trim());
        }
        if (!statusParts.isEmpty()) {
            lines.add("     | Status: " + statusParts.getFirst());
            for (int i = 1; i < statusParts.size(); i++) {
                lines.add("     | " + statusParts.get(i));
            }
        }

        List<String> labs = new ArrayList<>();
        if (e.getTsh() != null) labs.add("TSH " + e.getTsh() + " uIU/mL");
        if (e.getFreeT4() != null) labs.add("fT4 " + e.getFreeT4() + " ng/dL");
        if (e.getFreeT3() != null) labs.add("fT3 " + e.getFreeT3() + " pg/mL");
        if (e.getTpoAb() != null) labs.add("TPOAb " + e.getTpoAb() + " IU/mL");
        if (e.getTg() != null) labs.add("Tg " + e.getTg() + " ng/mL");
        if (e.getTgAb() != null) labs.add("TgAb " + e.getTgAb() + " IU/mL");
        if (e.getTrab() != null) labs.add("TRAb " + e.getTrab() + " IU/L");
        if (e.getCalcitonin() != null) labs.add("Calcitonin " + e.getCalcitonin() + " pg/mL");
        if (!labs.isEmpty()) {
            String datePart = (e.getLastLabDate() != null) ? " (" + e.getLastLabDate() + ")" : "";
            lines.add("     | Labs" + datePart + ": " + String.join("; ", labs));
        }

        if (lblTiRadsResult.getText().contains("Score")) {
            lines.add("     | Nodule/TI-RADS: " + lblTiRadsResult.getText().replace("\n", ", "));
        }

        StringBuilder plan = new StringBuilder("Plan: ");
        if (e.getFollowUpInterval() != null) {
            plan.append("Follow up in ").append(e.getFollowUpInterval()).append(". ");
        }
        if (e.getFollowUpPlanText() != null && !e.getFollowUpPlanText().isBlank()) {
            String cleanPlan = e.getFollowUpPlanText().replaceAll("\\R+", "; ").trim();
            plan.append(cleanPlan);
        }
        lines.add("     | " + plan.toString().trim());

        return String.join("\n", lines);
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try { return Double.parseDouble(text.trim()); } catch (Exception e) { return null; }
    }

    private String emptyToNull(String text) {
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    private void updatePhysicalExamNotes() {
        String current = txtSummaryOutput.getText();
        List<String> lines = (current == null || current.isBlank())
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(current.split("\n", -1)));
        lines.removeIf(line -> line.trim().startsWith("Physical exam:"));

        int sepIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(EXAM_SEPARATOR)) {
                sepIndex = i;
                break;
            }
        }
        if (sepIndex >= 0) {
            lines = new ArrayList<>(lines.subList(0, sepIndex));
            while (!lines.isEmpty() && lines.getLast().isBlank()) {
                lines.removeLast();
            }
        }

        boolean anySelected = examSectionMap.values().stream()
                .flatMap(List::stream)
                .anyMatch(CheckBox::isSelected);

        if (anySelected) {
            lines.add(EXAM_SEPARATOR);
            lines.add(EXAM_HEADER);
            for (var entry : examSectionMap.entrySet()) {
                List<String> selected = entry.getValue().stream()
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .toList();
                if (!selected.isEmpty()) {
                    String label = EXAM_LABELS.getOrDefault(entry.getKey(), entry.getKey());
                    lines.add("     " + label + " :\t" + String.join("; ", selected));
                }
            }
            lines.add(EXAM_SEPARATOR);
        }

        txtSummaryOutput.setText(lines.isEmpty() ? "" : String.join("\n", lines));
    }

    private TitledPane styledPane(String title, Node content) {
        TitledPane pane = new TitledPane();
        pane.setContent(content);
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold; -fx-font-style: italic; -fx-font-size: 110%; -fx-text-fill: #0d3d8f;");
        pane.setGraphic(header);
        pane.setText(null);
        return pane;
    }

    private static Map<String, String[]> buildConditionGroups() {
        Map<String, String[]> map = new LinkedHashMap<>();
        map.put("Hypothyroidism", new String[]{
                "Primary hypothyroidism (thyroid gland failure)",
                " - Hashimoto's thyroiditis (chronic autoimmune thyroiditis)",
                " - Iodine deficiency",
                " - Post-ablative (radioiodine therapy, thyroidectomy)",
                " - Drug-induced (lithium, amiodarone, interferon-alpha)",
                " - Congenital hypothyroidism",
                " - Infiltrative diseases (amyloidosis, sarcoidosis, hemochromatosis)",
                "Secondary hypothyroidism (pituitary TSH deficiency)",
                "Tertiary hypothyroidism (hypothalamic TRH deficiency)",
                "Subclinical hypothyroidism"
        });
        map.put("Hyperthyroidism", new String[]{
                "Graves' disease (diffuse toxic goiter)",
                "Toxic multinodular goiter (Plummer's disease)",
                "Toxic adenoma (solitary autonomous nodule)",
                "Thyroiditis-associated thyrotoxicosis",
                " - Subacute (de Quervain's) thyroiditis",
                " - Silent (painless) thyroiditis",
                " - Postpartum thyroiditis",
                "Iodine-induced hyperthyroidism (Jod-Basedow phenomenon)",
                "TSH-secreting pituitary adenoma",
                "hCG-mediated thyrotoxicosis (gestational, trophoblastic tumors)",
                "Factitious thyrotoxicosis (exogenous thyroid hormone)",
                "Subclinical hyperthyroidism"
        });
        map.put("Thyroiditis", new String[]{
                "Acute (suppurative) thyroiditis",
                "Subacute (de Quervain's) thyroiditis",
                "Chronic autoimmune (Hashimoto's) thyroiditis",
                "Silent (painless) thyroiditis",
                "Postpartum thyroiditis",
                "Drug-induced thyroiditis",
                "Riedel's thyroiditis (fibrous thyroiditis)"
        });
        map.put("Goiter", new String[]{
                "Simple (nontoxic) goiter",
                "Endemic goiter (iodine deficiency)",
                "Sporadic goiter",
                "Multinodular goiter (toxic and nontoxic)",
                "Diffuse goiter (Graves' disease, thyroiditis)"
        });
        map.put("Thyroid Nodules", new String[]{
                "Benign thyroid nodules",
                " - Colloid nodules",
                " - Follicular adenoma",
                " - Thyroid cysts",
                "Malignant thyroid nodules (see thyroid cancer)"
        });
        map.put("Thyroid Cancer", new String[]{
                "Differentiated thyroid cancer",
                " - Papillary thyroid carcinoma (most common)",
                " - Follicular thyroid carcinoma",
                " - Hurthle cell carcinoma",
                "Medullary thyroid carcinoma (from C cells)",
                "Anaplastic (undifferentiated) thyroid carcinoma",
                "Primary thyroid lymphoma",
                "Metastatic disease to thyroid"
        });
        map.put("Congenital / Developmental", new String[]{
                "Congenital hypothyroidism",
                "Thyroid dysgenesis (agenesis, ectopic thyroid)",
                "Dyshormonogenesis (defects in thyroid hormone synthesis)",
                "Thyroglossal duct cyst",
                "Lingual thyroid"
        });
        map.put("Sick Euthyroid", new String[]{
                "Nonthyroidal illness syndrome",
                "Low T3 syndrome"
        });
        map.put("Thyroid Hormone Resistance", new String[]{
                "Resistance to thyroid hormone (RTH)",
                "TSH receptor mutations"
        });
        map.put("Pregnancy-Related", new String[]{
                "Gestational thyrotoxicosis (hyperemesis gravidarum-related)",
                "Postpartum thyroiditis",
                "Transient thyrotoxicosis of pregnancy"
        });
        return map;
    }

    private static Map<String, List<ThyroidEntry.Symptom>> buildSymptomGroups() {
        Map<String, List<ThyroidEntry.Symptom>> groups = new LinkedHashMap<>();
        List<ThyroidEntry.Symptom> hyper = new ArrayList<>();
        List<ThyroidEntry.Symptom> hypo = new ArrayList<>();
        List<ThyroidEntry.Symptom> general = new ArrayList<>();

        for (ThyroidEntry.Symptom symptom : ThyroidEntry.Symptom.values()) {
            String name = symptom.name();
            if (name.startsWith("HYPER_")) {
                hyper.add(symptom);
            } else if (name.startsWith("HYPO_")) {
                hypo.add(symptom);
            } else {
                general.add(symptom);
            }
        }

        hyper.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        hypo.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        general.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        groups.put("Hyperthyroidism", hyper);
        groups.put("Hypothyroidism", hypo);
        groups.put("General / Other", general);
        return groups;
    }

    private Node buildConditionChecklist() {
        VBox root = new VBox(8);
        root.setPadding(new Insets(4, 0, 0, 0));

        Label header = new Label("Common thyroid conditions (select multiple):");
        header.setStyle("-fx-text-fill: #0d3d8f;");
        root.getChildren().add(header);

        int groupIndex = 0;
        int groupCount = CONDITION_GROUPS.size();
        for (var entry : CONDITION_GROUPS.entrySet()) {
            Label groupLabel = new Label(entry.getKey());
            groupLabel.setStyle("-fx-font-weight: bold;");
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(6);

            String[] items = entry.getValue();
            List<CheckBox> groupChecks = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                CheckBox cb = new CheckBox(items[i]);
                groupChecks.add(cb);
                int col = i % 2;
                int row = i / 2;
                grid.add(cb, col, row);
            }
            conditionGroupMap.put(entry.getKey(), groupChecks);

            root.getChildren().addAll(groupLabel, grid);
            groupIndex++;
            if (groupIndex < groupCount) {
                root.getChildren().add(new Separator());
            }
        }
        return root;
    }

    private Map<String, List<String>> collectSelectedConditions() {
        Map<String, List<String>> selected = new LinkedHashMap<>();
        for (var entry : conditionGroupMap.entrySet()) {
            List<String> checked = entry.getValue().stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .toList();
            if (!checked.isEmpty()) {
                selected.put(entry.getKey(), checked);
            }
        }
        return selected;
    }
}
