package com.emr.gds.soap;

import com.emr.gds.input.IAITextAreaManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX dialog for Past Medical History (PMH).
 * Left column: disease/category checkboxes in a grid
 * Right column: aligned editable fields (TextAreas) for details.
 *
 * --- Features from original version retained ---
 * - Abbreviation expansion from DB with CSV fallback.
 * - "Open EMRFMH" button functionality.
 * - Save inserts into provided external TextArea or shows in output area.
 * - Quit closes ONLY this window.
 * - Robust threading and error handling.
 *
 * --- UPGRADES Inspired by Swing EMRPMH ---
 * - UI Layout: Conditions are now in a dynamic multi-column grid for better space usage.
 * - Live Summary: The output area at the bottom updates in real-time as checkboxes are toggled.
 * - More Conditions: The list of conditions is more comprehensive.
 * - Copy to Clipboard: A new "Copy" button to easily export the summary.
 * - Specific Logic: Special handling for "All denied allergies" on save.
 */
public class EMRPMH extends Application {

    // --- Integration points (optional for embedded use) ---
    private final IAITextAreaManager textAreaManager;  // may be null
    private final TextArea externalTarget;             // optional external target for saving

    // --- UI Components ---
    private Stage stage;
    private GridPane grid;
    private TextArea outputArea; // Now acts as a live summary pane

    private final Map<String, CheckBox> pmhChecks = new LinkedHashMap<>();
    private final Map<String, TextArea> pmhNotes = new LinkedHashMap<>();
    private final Map<String, String> abbrevMap;

    // UPGRADE: More comprehensive list of conditions from the Swing example
    private static final String[] CATEGORIES = {
            "Hypertension", "Dyslipidemia", "Diabetes Mellitus",
            "Thyroid Disease", "Asthma / COPD", "Pneumonia", "Tuberculosis (TB)",
            "Cardiovascular Disease", "AMI", "Angina Pectoris", "Arrhythmia",
            "Cerebrovascular Disease (CVA)", "Parkinson's Disease", "Cognitive Disorder", "Hearing Loss",
            "Chronic Kidney Disease (CKD)", "Gout", "Arthritis",
            "Cancer Hx", "Operation Hx",
            "GERD", "Hepatitis A / B",
            "Depression",
            "Allergy", "Food Allergy", "Injection Allergy", "Medication Allergy", 
            "All denied allergies...Food, Medication, Injection",
            "Others"
    };
    private static final List<String> DEFAULT_DOT_TARGETS = List.of(
            "Hypertension", "Dyslipidemia", "Diabetes Mellitus",
            "Thyroid Disease", "Asthma / COPD", "Tuberculosis (TB)",
            "Cardiovascular Disease",
            "Cerebrovascular Disease (CVA)", "Parkinson's Disease", "Cognitive Disorder",
            "Chronic Kidney Disease (CKD)", "Arthritis",
            "Cancer Hx", "Operation Hx",
            "GERD", "Hepatitis A / B",
            "All denied allergies...Food, Medication, Injection"
    );
    
    // UPGRADE: Define how many columns the grid should have
    private static final int NUM_COLUMNS = 4; // Increased columns: 3 -> 4

    // -------- Constructors --------
    public EMRPMH() { this(null, null, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager) { this(manager, null, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager, TextArea externalTarget) { this(manager, externalTarget, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager, TextArea externalTarget, Map<String, String> abbrevMap) {
        this.textAreaManager = manager;
        this.externalTarget = externalTarget;
        this.abbrevMap = (abbrevMap != null) ? abbrevMap : Collections.emptyMap();
    }

    // -------- JavaFX lifecycle --------
    @Override
    public void start(Stage primaryStage) {
        buildUI(primaryStage);
        primaryStage.show();
    }

    public void showDialog() {
        Platform.runLater(() -> {
            Stage s = new Stage();
            buildUI(s);
            s.initModality(Modality.NONE);
            s.show();
        });
    }

    // -------- UI builder --------
    private void buildUI(Stage s) {
        this.stage = s;
        s.setTitle("EMR - Past Medical History (PMH) - Upgraded");
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("pmh-root");

        Label title = new Label("Past Medical History");
        title.setFont(Font.font("Segoe UI", 17)); // Slightly increased: 15 -> 17
        title.setPadding(new Insets(0, 0, 15, 0));
        title.getStyleClass().add("pmh-title");
        root.setTop(title);

        // UPGRADE: Use a grid that supports multiple columns for a compact layout
        grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12); // Increased vertical gap
        grid.setPadding(new Insets(10));
        grid.getStyleClass().add("pmh-grid");

        for (int i = 0; i < NUM_COLUMNS; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / NUM_COLUMNS);
            grid.getColumnConstraints().add(col);
        }

        // Populate the grid
        int row = 0, col = 0;
        for (String key : CATEGORIES) {
            CheckBox cb = new CheckBox(key);
            cb.setFont(Font.font("Segoe UI", 11)); // Slightly increased: 9 -> 11
            cb.setTooltip(new Tooltip("Select if applicable: " + key));
            pmhChecks.put(key, cb);

            TextArea ta = new TextArea();
            ta.setPromptText("Details for " + key);
            ta.setWrapText(true);
            ta.setPrefRowCount(2); // Increased row count for better visibility
            ta.setFont(Font.font("Segoe UI", 11)); // Slightly increased: 9 -> 11
            pmhNotes.put(key, ta);
            
            // This VBox keeps the checkbox and its text area together vertically
            VBox cellBox = new VBox(6, cb, ta);
            VBox.setVgrow(ta, Priority.ALWAYS);
            cellBox.getStyleClass().add("pmh-cell");
            grid.add(cellBox, col, row);

            // UPGRADE: Add listener to update summary pane in real-time
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> updateLiveSummary());
            ta.textProperty().addListener((obs, oldVal, newVal) -> updateLiveSummary());

            addAbbreviationExpansionListener(ta);

            col++;
            if (col >= NUM_COLUMNS) {
                col = 0;
                row++;
            }
        }

        ScrollPane scroller = new ScrollPane(grid);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("pmh-scroll");
        root.setCenter(scroller);

        // Output / status area
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(8); // Increased rows
        outputArea.setWrapText(true);
        outputArea.setFont(Font.font("Consolas", 11)); // Slightly increased: 9 -> 11
        outputArea.setPromptText("Live summary of selected PMH will appear here.");
        outputArea.getStyleClass().add("pmh-output");
        root.setBottom(buildFooter(outputArea));

        Scene scene = new Scene(root, 1200, 1000); // Increased default size
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) { onQuit(); e.consume(); }
            if (e.isControlDown() && e.getCode() == KeyCode.ENTER) { onSave(); e.consume(); }
        });
        String css = buildThemeCss();
        String base64Css = Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        scene.getStylesheets().add("data:text/css;base64," + base64Css);
        s.setScene(scene);
        updateLiveSummary(); // Initial state
    }

    private VBox buildFooter(TextArea output) {
        Button btnSave = new Button("Save (Ctrl+Enter)");
        Button btnDefault = new Button("Default");
        Button btnClear = new Button("Clear");
        Button btnCopy = new Button("Copy to Clipboard"); // UPGRADE: New button
        Button btnFMH = new Button("Open EMRFMH");
        Button btnQuit = new Button("Quit");

        List.of(btnSave, btnDefault, btnClear, btnCopy, btnFMH, btnQuit).forEach(btn -> {
            btn.setFont(Font.font("Segoe UI", 11)); // Slightly increased: 9 -> 11
            btn.getStyleClass().add("pmh-btn");
        });
        
        // Assign specific classes for Gogh theming
        btnSave.getStyleClass().add("btn-save");
        btnDefault.getStyleClass().add("btn-default");
        btnClear.getStyleClass().add("btn-clear");
        btnCopy.getStyleClass().add("btn-copy");
        btnFMH.getStyleClass().add("btn-fmh");
        btnQuit.getStyleClass().add("btn-quit");

        btnSave.setOnAction(e -> onSave());
        btnDefault.setOnAction(e -> applyDefaultDots());
        btnClear.setOnAction(e -> {
            pmhChecks.values().forEach(cb -> cb.setSelected(false));
            pmhNotes.values().forEach(ta -> {
                ta.clear();
                ta.getStyleClass().remove("text-area-highlighted"); // Remove highlight on clear
            });
            // outputArea is cleared automatically by the listener
        });
        btnCopy.setOnAction(e -> onCopy()); // UPGRADE: Attach action
        btnFMH.setOnAction(e -> openEMRFMH());
        btnQuit.setOnAction(e -> onQuit());

        HBox buttons = new HBox(10, btnSave, btnDefault, btnClear, btnCopy, btnFMH, btnQuit);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        return new VBox(8, new Separator(), output, buttons);
    }

    private String buildThemeCss() {
        // High-visibility Clinical Theme with Van Gogh Inspired Highlights
        return """
                .pmh-root {
                    -fx-background-color: #f4f6f9; /* Clinical Light Gray */
                    -fx-font-family: 'Segoe UI', sans-serif;
                }
                .pmh-title {
                    -fx-text-fill: #1a1a1a;
                    -fx-font-weight: bold;
                    -fx-font-size: 17px;
                }
                .pmh-grid {
                    -fx-background-color: #ffffff;
                    -fx-background-radius: 4;
                    -fx-padding: 15;
                    -fx-border-color: #d1d5db;
                    -fx-border-width: 1;
                    -fx-border-radius: 4;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                }
                .pmh-cell {
                    -fx-background-color: #f9fafb;
                    -fx-background-radius: 4;
                    -fx-padding: 10;
                    -fx-border-color: #e5e7eb;
                    -fx-border-width: 1;
                    -fx-border-radius: 4;
                }
                .pmh-cell:hover {
                    -fx-background-color: #edf2f7;
                    -fx-border-color: #cbd5e0;
                }
                .check-box {
                    -fx-text-fill: #1f2937;
                    -fx-font-weight: bold;
                }
                .text-area {
                    -fx-font-family: 'Segoe UI', sans-serif;
                    -fx-font-size: 11px;
                    -fx-text-fill: #000000;
                    -fx-background-color: white;
                    -fx-control-inner-background: white;
                    -fx-border-color: #d1d5db;
                    -fx-border-radius: 3;
                }
                .text-area:focused {
                    -fx-border-color: #3b82f6;
                    -fx-effect: dropshadow(three-pass-box, rgba(59,130,246,0.2), 3, 0, 0, 0);
                }
                
                /* Highlighted Text Area (Tiny Yellow Gradient) */
                .text-area-highlighted {
                    -fx-control-inner-background: linear-gradient(to bottom, #ffffe0, #fffacd);
                    -fx-background-color: linear-gradient(to bottom, #ffffe0, #fffacd);
                }

                .pmh-scroll {
                    -fx-background-color: transparent;
                    -fx-background: transparent;
                }
                .scroll-pane > .viewport {
                    -fx-background-color: transparent;
                }
                .pmh-output {
                    -fx-background-color: #ffffff;
                    -fx-control-inner-background: #ffffff;
                    -fx-text-fill: #111827;
                    -fx-border-color: #9ca3af;
                    -fx-border-width: 1;
                    -fx-border-radius: 4;
                    -fx-font-family: 'Consolas', monospace;
                    -fx-font-size: 11px;
                }
                .pmh-btn {
                    -fx-background-radius: 4; -fx-border-radius: 4;
                    -fx-padding: 8 16 8 16; -fx-cursor: hand;
                    -fx-font-weight: bold; -fx-font-size: 11px;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);
                }
                
                /* Van Gogh Palette */
                .btn-save { /* Starry Night Blue */
                    -fx-background-color: #1d3b72; -fx-text-fill: white; -fx-border-color: #162d58;
                }
                .btn-save:hover { -fx-background-color: #2a5298; }
                
                .btn-default { /* Sunflowers Yellow */
                    -fx-background-color: #fbbf24; -fx-text-fill: #3e2723; -fx-border-color: #d97706;
                }
                .btn-default:hover { -fx-background-color: #f59e0b; }
                
                .btn-clear { /* Cafe Terrace Orange */
                    -fx-background-color: #f97316; -fx-text-fill: white; -fx-border-color: #c2410c;
                }
                .btn-clear:hover { -fx-background-color: #ea580c; }
                
                .btn-copy { /* Almond Blossom Teal */
                    -fx-background-color: #2dd4bf; -fx-text-fill: #0f172a; -fx-border-color: #0d9488;
                }
                .btn-copy:hover { -fx-background-color: #14b8a6; }
                
                .btn-fmh { /* Irises Purple */
                    -fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-border-color: #6d28d9;
                }
                .btn-fmh:hover { -fx-background-color: #7c3aed; }
                
                .btn-quit { /* Vineyard Red */
                    -fx-background-color: #ef4444; -fx-text-fill: white; -fx-border-color: #b91c1c;
                }
                .btn-quit:hover { -fx-background-color: #dc2626; }
                """;
    }
    
    // -------- Actions --------
    private void onSave() {
        String summary = buildSummaryText(true); // Get final text with special logic

        if (externalTarget != null) {
            int caret = externalTarget.getCaretPosition();
            externalTarget.insertText(caret, summary);
            // Also update the live summary to show what was inserted
            outputArea.setText("[Saved to external editor]\n" + summary);
        } else {
            // Fallback: show in the bottom output area
            outputArea.setText(summary);
        }
    }
    
    // UPGRADE: New "Copy to Clipboard" action
    private void onCopy() {
        String summary = buildSummaryText(false); // Get raw text without save logic
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(summary);
        clipboard.setContent(content);
        
        // Provide user feedback
        String originalText = outputArea.getText();
        outputArea.setText("[Summary copied to clipboard!]\n\n" + originalText);
        // Fade back to original text after a moment
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> outputArea.setText(originalText));
            }
        }, 2000);
    }

    private void applyDefaultDots() {
        DEFAULT_DOT_TARGETS.forEach(key -> {
            TextArea ta = pmhNotes.get(key);
            if (ta != null) {
                String text = ta.getText();
                if (text == null || text.isEmpty()) {
                    ta.setText(".");
                } else if (!text.trim().endsWith(".")) {
                    ta.appendText(".");
                }
                
                // Add highlight style class if not present
                if (!ta.getStyleClass().contains("text-area-highlighted")) {
                    ta.getStyleClass().add("text-area-highlighted");
                }
            }
        });
        updateLiveSummary();
    }

    private void onQuit() {
        if (stage != null) stage.close();
    }
    
    // UPGRADE: This method now drives the live summary
    private void updateLiveSummary() {
        String summary = buildSummaryText(false); // Build summary without save-specific logic
        outputArea.setText(summary);
    }

    /**
     * Builds the summary text from selected items.
     * @param applySaveLogic If true, applies special logic like the "All denied allergies" replacement.
     * @return The formatted summary string.
     */
    private String buildSummaryText(boolean applySaveLogic) {
        StringBuilder sb = new StringBuilder("Past Mdedical History-----------\n");
        boolean hasContent = false;
        List<String> lines = new ArrayList<>();

        boolean allDeniedSelected = pmhChecks.getOrDefault("All denied allergies...", new CheckBox()).isSelected();

        for (String key : CATEGORIES) {
            CheckBox cb = pmhChecks.get(key);
            String note = pmhNotes.get(key).getText().trim();

            if (cb.isSelected() || !note.isEmpty()) {
                hasContent = true;

                // UPGRADE: Special logic inspired by Swing version
                if (applySaveLogic && key.equals("All denied allergies...") && cb.isSelected()) {
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    lines.add("Allergy: As of " + date
                            + ", the patient denies any known allergies to food, injections, or medications.");
                    continue; // Skip the generic line for this
                }
                
                // Don't show specific allergies if "All denied" is checked.
                if (allDeniedSelected && key.contains("Allergy") && !key.equals("All denied allergies...")) {
                    continue;
                }

                StringBuilder line = new StringBuilder();
                line.append(cb.isSelected() ? "▣ " : "□ ").append(key);
                if (!note.isEmpty()) {
                    line.append(": ").append(note.replace("\n", " | "));
                }
                lines.add(line.toString());
            }
        }

        if (!hasContent) {
            return "PMH>\n(No items selected)";
        }
        // Checked items first, keep original relative order within groups.
        lines.stream()
                .sorted(Comparator.comparing((String line) -> line.startsWith("□ ")).thenComparing(lines::indexOf))
                .forEach(l -> sb.append(l).append("\n"));
        return sb.toString();
    }


    // ===============================================
    // Abbreviation & Other Helper Methods (Unchanged)
    // ===============================================
    private void addAbbreviationExpansionListener(TextArea ta) {
        ta.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (expandAbbreviationOnSpace(ta)) event.consume();
            }
        });
    }

    private boolean expandAbbreviationOnSpace(TextArea ta) {
        int caret = ta.getCaretPosition();
        String upToCaret = ta.getText(0, caret);
        int start = Math.max(upToCaret.lastIndexOf(' '), upToCaret.lastIndexOf('\n')) + 1;

        String word = upToCaret.substring(start).trim();
        if (!word.startsWith(":")) return false;

        String key = word.substring(1);
        String replacement = abbrevMap.get(key);
        if (replacement == null) return false;

        Platform.runLater(() -> {
            ta.deleteText(start, caret);
            ta.insertText(start, replacement + " ");
        });
        return true;
    }

    private void openEMRFMH() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Assuming EMRFMH is a Swing JFrame
                new EMRFMH(textAreaManager, abbrevMap).setVisible(true); // Example instantiation
            } catch (Throwable t) {
                showError("Unable to open EMRFMH", t);
            }
        });
    }

    private void showError(String header, Throwable t) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(header);
            a.setContentText(t.getClass().getSimpleName() + ": " + Optional.ofNullable(t.getMessage()).orElse(""));
            a.showAndWait();
        });
    }

    private void showInfo(String header, String content) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(header);
            a.setContentText(content);
            a.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
