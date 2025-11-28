package com.emr.gds.main.thyroid;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX form for Thyroid EMR entry.
 * Enhanced for Endocrinologists with TI-RADS, ATA Risk, and Dose Calculators.
 */
public class ThyroidPane extends VBox {

    private final ThyroidEntry entry;

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

    // Follow-up
    private final ComboBox<String> cmbFollowUpInterval = new ComboBox<>();
    private final TextArea txtFollowUpPlan = new TextArea();
    private final TextArea txtSummaryOutput = new TextArea();
    private final Button btnGenerateSummary = new Button("Generate Specialist Summary");

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
        txtTsh.setPromptText("TSH");
        txtFreeT4.setPromptText("fT4");
        txtFreeT3.setPromptText("fT3");
        txtTpoAb.setPromptText("TPOAb");
        txtTg.setPromptText("Tg");
        txtTgAb.setPromptText("TgAb");
        txtTrab.setPromptText("TRAb");
        txtCalcitonin.setPromptText("Calcitonin");
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
        txtSummaryOutput.setPrefRowCount(8);
    }

    private void buildLayout() {
        TitledPane overviewPane = createOverviewPane();
        TitledPane riskPane = createRiskPane();
        TitledPane labsPane = createLabsPane();
        TitledPane treatmentPane = createTreatmentPane();
        TitledPane followUpPane = createFollowUpPane();

        Accordion accordion = new Accordion(overviewPane, riskPane, labsPane, treatmentPane, followUpPane);
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

        grid.add(new Label("Categories:"), 0, row);
        VBox catBox = new VBox(5, 
            new HBox(10, chkHypo, chkHyper, chkNodule),
            new HBox(10, chkCancer, chkThyroiditis, chkGoiter)
        );
        grid.add(catBox, 1, row, 4, 1);
        row++;

        grid.add(new Label("Hypo/Hyper:"), 0, row);
        HBox etiologyBox = new HBox(10, cmbHypoEtiology, cmbHyperEtiology);
        grid.add(etiologyBox, 1, row, 4, 1);
        row++;
        
        HBox activeBox = new HBox(10, chkHypoOvert, chkHyperActive);
        grid.add(activeBox, 1, row, 4, 1);

        return new TitledPane("1. Overview & Patient", grid);
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

        return new TitledPane("2. Risk Stratification & Tools", root);
    }

    private TitledPane createLabsPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("TSH"), txtTsh, new Label("fT4"), txtFreeT4, new Label("fT3"), txtFreeT3);
        grid.addRow(1, new Label("TPOAb"), txtTpoAb, new Label("Tg"), txtTg, new Label("TgAb"), txtTgAb);
        grid.addRow(2, new Label("TRAb"), txtTrab, new Label("Calcitonin"), txtCalcitonin);
        grid.addRow(3, new Label("Date"), dpLastLabDate);

        return new TitledPane("3. Labs", grid);
    }

    private TitledPane createTreatmentPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.addRow(0, new Label("Levothyroxine (mcg)"), txtLt4Dose);
        grid.addRow(1, new Label("Antithyroid Drug"), txtAtdName, new Label("Dose (mg)"), txtAtdDose);
        grid.addRow(2, new Label("Beta Blocker"), txtBetaBlockerName, new Label("Dose"), txtBetaBlockerDose);

        return new TitledPane("4. Treatment", grid);
    }

    private TitledPane createFollowUpPane() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));

        HBox intBox = new HBox(10, new Label("Interval:"), cmbFollowUpInterval);
        intBox.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(
            intBox,
            new Label("Plan details:"),
            txtFollowUpPlan,
            btnGenerateSummary,
            txtSummaryOutput
        );
        return new TitledPane("5. Plan & Summary", box);
    }

    // --- Logic & Actions ---

    private void configureActions() {
        // Real-time Weight Calc
        txtWeight.textProperty().addListener((obs, oldVal, newVal) -> updateDoseEst());

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
        });
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
        StringBuilder sb = new StringBuilder();

        // 1. Header line
        if (e.getVisitType() != null) sb.append(e.getVisitType()).append(" visit. ");
        sb.append("Thyroid Specialist Evaluation.\n");

        // 2. Diagnosis Block
        sb.append("Dx: ");
        if (e.getCategories().isEmpty()) sb.append("Thyroid screening/evaluation. ");
        else {
            for (ThyroidEntry.MainCategory cat : e.getCategories()) {
                sb.append(cat).append(", ");
            }
        }
        // Remove trailing comma
        if (sb.toString().endsWith(", ")) sb.setLength(sb.length() - 2);
        sb.append(".\n");

        // 3. Clinical Status (Hypo/Hyper/Cancer)
        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPOTHYROIDISM)) {
            sb.append("- Hypothyroidism: ");
            if (e.getHypoEtiology() != null) sb.append(e.getHypoEtiology()).append(". ");
            sb.append(Boolean.TRUE.equals(e.isHypoOvert()) ? "Overt." : "Subclinical.");
            if (e.getLt4DoseMcgPerDay() != null) {
                sb.append(" Current LT4: ").append(e.getLt4DoseMcgPerDay()).append(" mcg.");
                if (e.getPatientWeightKg() != null) {
                     double est = ThyroidRiskCalculator.calculateFullReplacementDose(e.getPatientWeightKg());
                     sb.append(" (Est. replacement: ").append((int)est).append(" mcg).");
                }
            }
            sb.append("\n");
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPERTHYROIDISM)) {
            sb.append("- Hyperthyroidism: ");
            if (e.getHyperEtiology() != null) sb.append(e.getHyperEtiology()).append(". ");
            sb.append(Boolean.TRUE.equals(e.isHyperActive()) ? "Uncontrolled/Active." : "Controlled/Remission.");
            if (e.getAtdName() != null) {
                sb.append(" On ").append(e.getAtdName()).append(" ").append(e.getAtdDoseMgPerDay()).append(" mg.");
            }
            sb.append("\n");
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.CANCER)) {
            sb.append("- Thyroid Cancer: ");
            if (e.getAtaRisk() != null && !e.getAtaRisk().equals("Low Risk")) {
                sb.append(e.getAtaRisk()).append(" (based on path features). ");
            } else {
                sb.append("Low Risk Stratification. ");
            }
            if (e.getTg() != null) sb.append("Tg: ").append(e.getTg()).append(" ng/mL. ");
            sb.append("\n");
        }

        // 4. Labs Summary
        if (e.getLastLabDate() != null || e.getTsh() != null) {
            sb.append("- Labs");
            if (e.getLastLabDate() != null) sb.append(" (").append(e.getLastLabDate()).append(")");
            sb.append(": ");
            if (e.getTsh() != null) sb.append("TSH ").append(e.getTsh()).append("; ");
            if (e.getFreeT4() != null) sb.append("fT4 ").append(e.getFreeT4()).append("; ");
            if (e.getFreeT3() != null) sb.append("fT3 ").append(e.getFreeT3()).append("; ");
            if (e.getTpoAb() != null) sb.append("TPOAb ").append(e.getTpoAb()).append("; ");
            if (e.getTrab() != null) sb.append("TRAb ").append(e.getTrab()).append("; ");
            sb.append("\n");
        }

        // 5. Nodule / TI-RADS
        if (lblTiRadsResult.getText().contains("Score")) {
             sb.append("- Nodule Assessment: ").append(lblTiRadsResult.getText().replace("\n", ", ")).append("\n");
        }

        // 6. Plan
        sb.append("- Plan: ");
        if (e.getFollowUpInterval() != null) sb.append("Follow up in ").append(e.getFollowUpInterval()).append(". ");
        if (e.getFollowUpPlanText() != null) sb.append(e.getFollowUpPlanText());

        return sb.toString();
    }

    private Double parseDoubleOrNull(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try { return Double.parseDouble(text.trim()); } catch (Exception e) { return null; }
    }

    private String emptyToNull(String text) {
        return (text == null || text.isBlank()) ? null : text.trim();
    }
}