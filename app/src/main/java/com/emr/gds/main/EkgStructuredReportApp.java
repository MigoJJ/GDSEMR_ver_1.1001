package com.emr.gds.main;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A JFrame-based application for systematic EKG (Electrocardiogram) analysis.
 * This tool provides a structured form for documenting EKG findings and generating a summary report.
 */
public class EkgStructuredReportApp extends JFrame {

    private final JTextArea findingsArea = new JTextArea(12, 40);
    private final JTextArea summaryArea = new JTextArea(3, 40);

    public EkgStructuredReportApp() {
        setTitle("EMR EKG Analysis");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650, 500);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centralPanel = new JPanel(new BorderLayout(5, 5));
        centralPanel.setBorder(BorderFactory.createTitledBorder("EKG Interpretation Input"));

        findingsArea.setLineWrap(true);
        findingsArea.setWrapStyleWord(true);
        findingsArea.setText("""
Rate:
Rhythm:
Axis:
Intervals (PR, QRS, QTc):
Hypertrophy:
Ischemia / ST-T changes:
Others:
""");
        centralPanel.add(new JScrollPane(findingsArea), BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.add(new JLabel("Summary / Impression:"), BorderLayout.NORTH);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryPanel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);

        JButton refButton = new JButton("EKG Reference");
        refButton.addActionListener(e -> openReference());
        summaryPanel.add(refButton, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JButton saveButton = new JButton("Save Report to EMR");
        saveButton.addActionListener(e -> saveReport());
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            findingsArea.setText("");
            summaryArea.setText("");
        });

        rightPanel.add(saveButton);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(clearButton);

        mainPanel.add(centralPanel, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        setContentPane(mainPanel);
    }

    private void openReference() {
        try {
            File file = new File("src/main/resources/text/EKG_reference.odt").getAbsoluteFile();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                JOptionPane.showMessageDialog(this, "Desktop operations not supported on this platform.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open reference: " + ex.getMessage());
        }
    }

    private void saveReport() {
        IAITextAreaManager manager = IAIMain.getTextAreaManager();
        if (manager == null || !manager.isReady()) {
            JOptionPane.showMessageDialog(this, "EMR is not ready. Please open the EMR first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reportText = findingsArea.getText().trim();
        String summaryText = summaryArea.getText().trim();
        if (reportText.isEmpty() && summaryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter findings or a summary before saving.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String stampedReport = String.format("\n< EKG Report - %s >\n%s\nSummary: %s",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE),
                reportText,
                summaryText.isEmpty() ? "(none)" : summaryText);

        manager.focusArea(5); // Target 'O>' area
        manager.insertLineIntoFocusedArea(stampedReport);
        JOptionPane.showMessageDialog(this, "EKG report saved to EMR.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EkgStructuredReportApp().setVisible(true));
    }
}
