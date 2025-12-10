package com.emr.gds.main.glp1;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

/**
 * EMR UI component for:
 *   MEDICATION - GLP-1RA (SEMAGLUTIDE)
 *   (Checklist, Dose, Follow-up, Contraindications)
 *
 * Features:
 *   - Validation and null safety
 *   - Layout and spacing tuned for EMR use
 *   - Event listeners for linked controls
 *   - Problem-list style text serialization
 */
public class Glp1SemaglutidePane extends VBox {

    // Checklist / status
    private final CheckBox chkOnTherapy  = new CheckBox("On therapy");
    private final CheckBox chkForT2dm    = new CheckBox("Indication: T2DM");
    private final CheckBox chkForObesity = new CheckBox("Indication: Obesity / OW");
    private final CheckBox chkForAscvd   = new CheckBox("Indication: ASCVD risk lower");

    // Dose
    private final ComboBox<String> cmbBrand = new ComboBox<>();
    private final ComboBox<String> cmbDose  = new ComboBox<>();
    private final TextField txtDoseCustom   = new TextField();

    // Follow-up
    private final DatePicker dpNextFollowUp             = new DatePicker();
    private final ComboBox<String> cmbFollowUpInterval  = new ComboBox<>();
    private final TextArea txtFollowUpNotes             = new TextArea();

    // Contraindications
    private final CheckBox chkMtcMen2      = new CheckBox("Personal/family hx MTC or MEN2");
    private final CheckBox chkPancreatitis = new CheckBox("Hx pancreatitis (caution)");
    private final CheckBox chkPregnancy    = new CheckBox("Pregnancy / planning pregnancy");
    private final CheckBox chkSevereGi     = new CheckBox("Severe GI disease / gastroparesis");
    private final TextArea txtOtherContra  = new TextArea();

    // Validation warning label
    private final Label lblValidation = new Label();

    public Glp1SemaglutidePane() {
        setSpacing(10);
        setPadding(new Insets(12));
        getStyleClass().add("glp1-semaglutide-pane");

        // === Header ===
        Label lblHeader = new Label("MEDICATION - GLP-1RA (SEMAGLUTIDE)");
        lblHeader.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        // Sections
        TitledPane paneChecklist = createChecklistPane();
        TitledPane paneDose      = createDosePane();
        TitledPane paneFollowUp  = createFollowUpPane();
        TitledPane paneContra    = createContraindicationsPane();

        // Validation warning
        lblValidation.setStyle("-fx-text-fill: #d9534f; -fx-font-size: 11px;");
        lblValidation.setWrapText(true);

        // Listeners
        setupEventListeners();

        VBox content = new VBox(12, lblHeader, paneChecklist, paneDose, paneFollowUp, paneContra, lblValidation);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-padding: 0;");

        getChildren().add(scrollPane);

        // Default follow-up date = today
        if (dpNextFollowUp.getValue() == null) {
            dpNextFollowUp.setValue(LocalDate.now());
        }
    }

    // ===== UI Construction Helpers =====

    private TitledPane createChecklistPane() {
        VBox box = new VBox(6, chkOnTherapy, chkForT2dm, chkForObesity, chkForAscvd);
        box.setPadding(new Insets(8));

        TitledPane pane = new TitledPane("Checklist / Indication", box);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createDosePane() {
        // Brand options
        cmbBrand.getItems().setAll(
                "Ozempic (semaglutide, SC weekly)",
                "Wegovy (semaglutide, SC weekly)",
                "Rybelsus (semaglutide, PO daily)",
                "Mounjaro (tirzepatide, SC weekly)"
        );
        cmbBrand.setPromptText("Select brand / route...");
        cmbBrand.setPrefWidth(260);

        // Dose combo – items will be filled depending on brand
        cmbDose.setPromptText("Select dose...");
        cmbDose.setPrefWidth(250);

        // When brand changes → update dose options
        cmbBrand.setOnAction(e -> {
            cmbDose.getItems().clear();
            cmbDose.getSelectionModel().clearSelection();

            String brand = cmbBrand.getValue();
            if (brand == null) {
                return;
            }

            if (brand.startsWith("Ozempic")) {
                // Ozempic (Semaglutide): 0.25 mg, 0.5 mg, 1.0 mg, 2.0 mg weekly
                cmbDose.getItems().setAll(
                        "0.25 mg weekly",
                        "0.5 mg weekly",
                        "1.0 mg weekly",
                        "2.0 mg weekly"
                );
            } else if (brand.startsWith("Wegovy")) {
                // Wegovy (Semaglutide): 0.25 mg, 0.5 mg, 1.0 mg, 1.7 mg, 2.4 mg weekly
                cmbDose.getItems().setAll(
                        "0.25 mg weekly",
                        "0.5 mg weekly",
                        "1.0 mg weekly",
                        "1.7 mg weekly",
                        "2.4 mg weekly"
                );
            } else if (brand.startsWith("Rybelsus")) {
                // Rybelsus (PO daily): 3 mg, 7 mg, 14 mg
                cmbDose.getItems().setAll(
                        "3 mg PO daily",
                        "7 mg PO daily",
                        "14 mg PO daily"
                );
            } else if (brand.startsWith("Mounjaro")) {
                // Mounjaro (Tirzepatide): 2.5, 5, 7.5, 10, 12.5, 15 mg weekly
                cmbDose.getItems().setAll(
                        "2.5 mg weekly (initiation, 4 wks)",
                        "5.0 mg weekly",
                        "7.5 mg weekly",
                        "10.0 mg weekly",
                        "12.5 mg weekly",
                        "15.0 mg weekly (max)"
                );
            }
        });

        txtDoseCustom.setPromptText("Custom dose (e.g., 1.5 mg weekly)");
        txtDoseCustom.setPrefWidth(250);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        grid.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 2;");

        Label lblBrand = new Label("Brand / Route:");
        lblBrand.setStyle("-fx-font-weight: bold;");
        grid.add(lblBrand, 0, 0);
        grid.add(cmbBrand, 1, 0);

        Label lblDose = new Label("Dose (preset):");
        lblDose.setStyle("-fx-font-weight: bold;");
        grid.add(lblDose, 0, 1);
        grid.add(cmbDose, 1, 1);

        Label lblCustom = new Label("Dose (custom):");
        lblCustom.setStyle("-fx-font-weight: bold;");
        grid.add(lblCustom, 0, 2);
        grid.add(txtDoseCustom, 1, 2);

        ColumnConstraints col0 = new ColumnConstraints(100, 120, Double.MAX_VALUE);
        ColumnConstraints col1 = new ColumnConstraints(250, 250, Double.MAX_VALUE);
        grid.getColumnConstraints().addAll(col0, col1);

        TitledPane pane = new TitledPane("Dose", grid);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createFollowUpPane() {
        cmbFollowUpInterval.getItems().addAll(
                "Every 4 weeks",
                "Every 8 weeks",
                "Every 12 weeks",
                "PRN / as needed"
        );
        cmbFollowUpInterval.setPromptText("Select interval...");
        cmbFollowUpInterval.setPrefWidth(250);

        txtFollowUpNotes.setPromptText("Follow-up notes (e.g., monitor weight, HbA1c, GI symptoms...)");
        txtFollowUpNotes.setPrefRowCount(4);
        txtFollowUpNotes.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(8));
        grid.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 2;");

        Label lblDate = new Label("Next visit:");
        lblDate.setStyle("-fx-font-weight: bold;");
        grid.add(lblDate, 0, 0);
        grid.add(dpNextFollowUp, 1, 0);

        Label lblInterval = new Label("Interval:");
        lblInterval.setStyle("-fx-font-weight: bold;");
        grid.add(lblInterval, 0, 1);
        grid.add(cmbFollowUpInterval, 1, 1);

        Label lblNotes = new Label("Notes:");
        lblNotes.setStyle("-fx-font-weight: bold;");
        GridPane.setValignment(lblNotes, VPos.TOP);
        grid.add(lblNotes, 0, 2);
        grid.add(txtFollowUpNotes, 1, 2);

        ColumnConstraints col0 = new ColumnConstraints(100, 120, Double.MAX_VALUE);
        ColumnConstraints col1 = new ColumnConstraints(250, 300, Double.MAX_VALUE);
        grid.getColumnConstraints().addAll(col0, col1);

        TitledPane pane = new TitledPane("Follow-up", grid);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    private TitledPane createContraindicationsPane() {
        VBox checks = new VBox(6, chkMtcMen2, chkPancreatitis, chkPregnancy, chkSevereGi);

        txtOtherContra.setPromptText("Other contraindications / cautions...");
        txtOtherContra.setPrefRowCount(3);
        txtOtherContra.setWrapText(true);

        VBox box = new VBox(10, checks, txtOtherContra);
        box.setPadding(new Insets(8));
        box.setStyle(
                "-fx-border-color: #ffe6e6; " +
                "-fx-border-radius: 2; " +
                "-fx-background-color: #fff9f9;"
        );

        TitledPane pane = new TitledPane("Contraindications / Cautions", box);
        pane.setExpanded(true);
        pane.setCollapsible(true);
        return pane;
    }

    // ===== Event Listeners & Validation =====

    private void setupEventListeners() {
        chkOnTherapy.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
        chkMtcMen2.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
        chkPregnancy.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void validateForm() {
        StringBuilder warnings = new StringBuilder();

        if (chkMtcMen2.isSelected()) {
            warnings.append("Warning: MTC/MEN2 history - GLP-1RA is contraindicated. ");
        }
        if (chkPregnancy.isSelected()) {
            warnings.append("Warning: Pregnancy - GLP-1RA is contraindicated. ");
        }
        if (chkOnTherapy.isSelected() && cmbBrand.getValue() == null) {
            warnings.append("Warning: Please select a brand/route. ");
        }

        lblValidation.setText(warnings.toString());
    }

    // ===== Public API =====

    /**
     * Returns a formatted problem-list style text block for EMR storage.
     */
    public String toProblemListString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MEDICATION - GLP-1RA (SEMAGLUTIDE)\n");

        // Status / Indication
        sb.append("   Status / Indication:\n");
        if (chkOnTherapy.isSelected()) {
            sb.append("      - On therapy\n");
        } else {
            sb.append("      - NOT currently on therapy\n");
        }
        if (chkForT2dm.isSelected())    sb.append("      - Indication: T2DM\n");
        if (chkForObesity.isSelected()) sb.append("      - Indication: Obesity / overweight\n");
        if (chkForAscvd.isSelected())   sb.append("      - Indication: ASCVD risk reduction\n");

        // Dose
        sb.append("   Dose:\n");
        String brand      = cmbBrand.getValue();
        String dosePreset = cmbDose.getValue();
        String customDose = txtDoseCustom.getText().trim();

        if (brand != null && !brand.isEmpty()) {
            sb.append("      - Brand / Route: ").append(brand).append("\n");
        }
        if (dosePreset != null && !dosePreset.isEmpty()) {
            sb.append("      - Dose: ").append(dosePreset).append("\n");
        }
        if (!customDose.isEmpty()) {
            sb.append("      - Custom dose: ").append(customDose).append("\n");
        }
        if (brand == null && dosePreset == null && customDose.isEmpty()) {
            sb.append("      - [NO DOSE SPECIFIED]\n");
        }

        // Follow-up
        sb.append("   Follow-up:\n");
        LocalDate nextVisit = dpNextFollowUp.getValue();
        if (nextVisit != null) {
            sb.append("      - Next visit: ").append(nextVisit).append("\n");
        }
        String interval = cmbFollowUpInterval.getValue();
        if (interval != null && !interval.isEmpty()) {
            sb.append("      - Interval: ").append(interval).append("\n");
        }
        String notes = txtFollowUpNotes.getText().trim();
        if (!notes.isEmpty()) {
            sb.append("      - Notes: ").append(notes).append("\n");
        }

        // Contraindications
        sb.append("   Contraindications / Cautions:\n");
        boolean hasContra = false;
        if (chkMtcMen2.isSelected()) {
            sb.append("      - CONTRAINDICATED: Personal/family history MTC or MEN2\n");
            hasContra = true;
        }
        if (chkPancreatitis.isSelected()) {
            sb.append("      - CAUTION: History of pancreatitis\n");
            hasContra = true;
        }
        if (chkPregnancy.isSelected()) {
            sb.append("      - CONTRAINDICATED: Pregnancy or planning pregnancy\n");
            hasContra = true;
        }
        if (chkSevereGi.isSelected()) {
            sb.append("      - CAUTION: Severe GI disease or gastroparesis\n");
            hasContra = true;
        }
        String otherContra = txtOtherContra.getText().trim();
        if (!otherContra.isEmpty()) {
            sb.append("      - Other: ").append(otherContra).append("\n");
            hasContra = true;
        }
        if (!hasContra) {
            sb.append("      - None documented\n");
        }

        return sb.toString();
    }

    /**
     * Concise summary for Assessment (A>): brand, dose, and next visit date.
     */
    public String toAssessmentSummary() {
        String brand = cmbBrand.getValue() == null ? "" : cmbBrand.getValue().trim();
        String presetDose = cmbDose.getValue() == null ? "" : cmbDose.getValue().trim();
        String customDose = txtDoseCustom.getText() == null ? "" : txtDoseCustom.getText().trim();
        String dose = !presetDose.isEmpty() ? presetDose : customDose;
        LocalDate nextVisit = dpNextFollowUp.getValue();

        if (brand.isEmpty() && dose.isEmpty() && nextVisit == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("GLP-1RA (Semaglutide): ");
        boolean hasEntry = false;

        if (!brand.isEmpty()) {
            sb.append("Brand: ").append(brand);
            hasEntry = true;
        }
        if (!dose.isEmpty()) {
            if (hasEntry) sb.append(" | ");
            sb.append("Dose: ").append(dose);
            hasEntry = true;
        }
        if (nextVisit != null) {
            if (hasEntry) sb.append(" | ");
            sb.append("Next visit: ").append(nextVisit);
        }

        return sb.toString();
    }

    /**
     * Reset all fields to default / empty state.
     */
    public void clearAll() {
        chkOnTherapy.setSelected(false);
        chkForT2dm.setSelected(false);
        chkForObesity.setSelected(false);
        chkForAscvd.setSelected(false);

        cmbBrand.setValue(null);
        cmbDose.setValue(null);
        txtDoseCustom.clear();

        dpNextFollowUp.setValue(LocalDate.now());
        cmbFollowUpInterval.setValue(null);
        txtFollowUpNotes.clear();

        chkMtcMen2.setSelected(false);
        chkPancreatitis.setSelected(false);
        chkPregnancy.setSelected(false);
        chkSevereGi.setSelected(false);
        txtOtherContra.clear();

        lblValidation.setText("");
    }

    /**
     * Check if critical contraindications are present.
     */
    public boolean hasCriticalContraindications() {
        return chkMtcMen2.isSelected() || chkPregnancy.isSelected();
    }
}
