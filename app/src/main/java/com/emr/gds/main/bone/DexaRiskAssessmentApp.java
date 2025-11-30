package com.emr.gds.main.bone;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Enhanced Osteoporosis Risk Assessment Tool (DEXA)
 * Combines clean UI, full clinical risk factors, and reliable EMR integration
 */
public class DexaRiskAssessmentApp extends Application {

    private TextField scoreField, ageField;
    private ComboBox<String> genderComboBox;
    private TextArea outputTextArea;
    private CheckBox fragilityFractureCheckBox, menopauseCheckBox, hrtCheckBox, tahCheckBox, stonesCheckBox;
    private ToggleGroup scoreTypeToggleGroup;
    private RadioButton tScoreRadioButton, zScoreRadioButton;

    /**
     * Opens the DEXA window from an already running JavaFX application.
     */
    public static void open() {
        Platform.runLater(() -> {
            try {
                new DexaRiskAssessmentApp().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Osteoporosis Risk Assessment (DEXA)");
        initComponents();

        Scene scene = new Scene(createLayout(), 600, 680);
        scene.getStylesheets().add(""); // optional: add CSS later
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        scoreField.requestFocus();
    }

    private void initComponents() {
        scoreField = new TextField();
        scoreField.setPrefWidth(120);
        scoreField.setFont(Font.font("System", FontWeight.BOLD, 15));
        scoreField.setStyle("-fx-alignment: CENTER; -fx-background-color: #f0f8ff;");

        ageField = new TextField();
        ageField.setPrefWidth(120);
        ageField.setFont(Font.font("System", FontWeight.BOLD, 15));
        ageField.setStyle("-fx-alignment: CENTER; -fx-background-color: #f0f8ff;");

        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Female", "Male");
        genderComboBox.setValue("Female");

        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setWrapText(true);
        outputTextArea.setStyle("-fx-control-inner-background: #fafafa; -fx-font-family: 'Consolas';");

        fragilityFractureCheckBox = new CheckBox("Fragility Fracture");
        menopauseCheckBox = new CheckBox("Postmenopausal");
        hrtCheckBox = new CheckBox("On HRT");
        tahCheckBox = new CheckBox("TAH (Total Abdominal Hysterectomy)");
        stonesCheckBox = new CheckBox("History of Kidney Stones");

        scoreTypeToggleGroup = new ToggleGroup();
        tScoreRadioButton = new RadioButton("T-Score");
        zScoreRadioButton = new RadioButton("Z-Score");
        tScoreRadioButton.setToggleGroup(scoreTypeToggleGroup);
        zScoreRadioButton.setToggleGroup(scoreTypeToggleGroup);
        tScoreRadioButton.setSelected(true);
    }

    private BorderPane createLayout() {
        BorderPane root = new BorderPane();

        // Top: Output Area
        ScrollPane scrollPane = new ScrollPane(outputTextArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(220);
        root.setTop(scrollPane);
        BorderPane.setMargin(scrollPane, new Insets(10));

        // Left: Info Panel
        root.setLeft(createInfoPanel());

        // Center: Input Panel
        root.setCenter(createInputPanel());

        // Bottom: Buttons
        root.setBottom(createButtonPanel());

        return root;
    }

    private VBox createInfoPanel() {
        TextArea info = new TextArea("""
                Z-Score is used for:
                • Children & adolescents
                • Premenopausal women (<50 yrs)
                • Men under 50 years

                Consider checking:
                • Serum Calcium, Phosphorus
                • 25-OH Vitamin D
                • Renal function (eGFR, Cr)
                • Bone turnover markers (CTX, P1NP)
                """);
        info.setEditable(false);
        info.setWrapText(true);
        info.setStyle("-fx-background-color: #e8f4f8; -fx-font-size: 12px;");
        VBox box = new VBox(info);
        box.setPadding(new Insets(15));
        box.setPrefWidth(300);
        box.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT)));
        return box;
    }

    private GridPane createInputPanel() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Score:"), 0, 0);
        grid.add(scoreField, 1, 0);
        grid.add(new Label("Age:"), 0, 1);
        grid.add(ageField, 1, 1);
        grid.add(new Label("Gender:"), 0, 2);
        grid.add(genderComboBox, 1, 2);

        HBox scoreTypeBox = new HBox(15, tScoreRadioButton, zScoreRadioButton);
        scoreTypeBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(new Label("Score Type:"), 0, 3);
        grid.add(scoreTypeBox, 1, 3);

        // Clinical Factors
        VBox factors = new VBox(8,
                menopauseCheckBox,
                fragilityFractureCheckBox,
                hrtCheckBox,
                tahCheckBox,
                stonesCheckBox);
        TitledPane tp = new TitledPane("Clinical Risk Factors", factors);
        tp.setCollapsible(false);
        grid.add(tp, 0, 4, 2, 1);

        return grid;
    }

    private HBox createButtonPanel() {
        Button assessBtn = new Button("Assess Risk");
        assessBtn.setOnAction(e -> assessRisk());

        Button clearBtn = new Button("Clear All");
        clearBtn.setOnAction(e -> clearAll());

        Button saveBtn = new Button("Save to EMR");
        saveBtn.setOnAction(e -> saveToEmr());

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());

        HBox box = new HBox(12, assessBtn, clearBtn, saveBtn, closeBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f5f5f5;");
        return box;
    }

    private void assessRisk() {
        try {
            double score = Double.parseDouble(scoreField.getText().trim());
            int age = Integer.parseInt(ageField.getText().trim());
            String gender = genderComboBox.getValue();
            boolean hasFracture = fragilityFractureCheckBox.isSelected();
            boolean isMenopausal = menopauseCheckBox.isSelected();
            boolean onHrt = hrtCheckBox.isSelected();
            boolean hasTah = tahCheckBox.isSelected();
            boolean hasStones = stonesCheckBox.isSelected();
            boolean isTScore = tScoreRadioButton.isSelected();

            String report = generateReport(score, isTScore, age, gender, hasFracture, isMenopausal, onHrt, hasTah, hasStones);
            outputTextArea.setText(report);

        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid numbers for Score and Age.");
        }
    }

    private String generateReport(double score, boolean isTScore, int age, String gender,
                                  boolean hasFracture, boolean isMenopausal, boolean onHrt, boolean hasTah, boolean hasStones) {

        String scoreType = isTScore ? "T-Score" : "Z-Score";
        String diagnosis;

        if (isTScore) {
            if (score <= -2.5) {
                diagnosis = hasFracture ? "Severe Osteoporosis" : "Osteoporosis";
            } else if (score < -1.0) {
                diagnosis = "Osteopenia";
            } else {
                diagnosis = "Normal Bone Density";
            }
        } else {
            diagnosis = (score <= -2.0) ? "Below expected range for age" : "Within expected range for age";
        }

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("< DEXA Report - %s >\n", date));
        sb.append(String.format("Diagnosis: %s (%s: %.1f)\n", diagnosis, scoreType, score));
        sb.append(String.format("Patient: %d-year-old %s\n", age, gender));

        if ("Female".equals(gender)) {
            sb.append(String.format("Clinical Factors → Menopausal: %s | Fragility Fx: %s | On HRT: %s | TAH: %s | Kidney Stones: %s\n",
                    boolToYN(isMenopausal), boolToYN(hasFracture), boolToYN(onHrt), boolToYN(hasTah), boolToYN(hasStones)));
        } else {
            sb.append(String.format("Clinical Factors → Fragility Fx: %s | Kidney Stones: %s\n",
                    boolToYN(hasFracture), boolToYN(hasStones)));
        }

        sb.append("\nComment>\n");
        sb.append(String.format("# %s based on %s of %.1f.\n", diagnosis, scoreType, score));
        if (isTScore && score <= -2.5) {
            sb.append("# Consider bisphosphonate, denosumab, or anabolic therapy.\n");
        } else if (isTScore && score <= -1.0) {
            sb.append("# Lifestyle modification, calcium + vitamin D, repeat DEXA in 2–3 years.\n");
        }
        return sb.toString();
    }

    private String boolToYN(boolean b) { return b ? "Yes" : "No"; }

    private void saveToEmr() {
        String report = outputTextArea.getText();
        if (report == null || report.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nothing to Save", "Please assess risk first.");
            return;
        }

        IAITextAreaManager manager = IAIMain.getTextAreaManager();
        if (manager == null || !manager.isReady()) {
            showAlert(Alert.AlertType.ERROR, "EMR Not Ready", "Cannot connect to EMR text areas.");
            return;
        }

        String stampedReport = report.trim();
        manager.focusArea(5);  // Objective area
        manager.insertLineIntoFocusedArea("\n" + stampedReport + "\n");

        showAlert(Alert.AlertType.INFORMATION, "Success", "DEXA report saved to EMR (Objective area).");
        clearAll();
    }

    private void clearAll() {
        scoreField.clear();
        ageField.clear();
        genderComboBox.setValue("Female");
        fragilityFractureCheckBox.setSelected(false);
        menopauseCheckBox.setSelected(false);
        hrtCheckBox.setSelected(false);
        tahCheckBox.setSelected(false);
        stonesCheckBox.setSelected(false);
        tScoreRadioButton.setSelected(true);
        outputTextArea.clear();
        scoreField.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("DEXA Assessment");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Launch helper if needed
    public static void launchApp() {
        launch();
    }
    
    public static void showWindow() {
        new Thread(() -> Application.launch(DexaRiskAssessmentApp.class)).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
