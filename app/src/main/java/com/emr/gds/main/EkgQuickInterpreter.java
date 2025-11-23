package com.emr.gds.main;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * SimpleEKGInterpreter - Ultra-fast EKG reporting tool
 * Replaces old EKG.java - Doctor-approved, minimal clicks
 */
public class EkgQuickInterpreter extends JFrame {

    private final JTextArea findingsArea = new JTextArea(10, 30);
    private final JTextArea impressionArea = new JTextArea(4, 30);
    private final JTextArea commentsArea = new JTextArea(3, 30);

    public EkgQuickInterpreter() {
        setTitle("Quick EKG Interpreter - EMR Ready");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left: predefined snippets
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(new JLabel("Quick Snippets"));
        left.add(Box.createVerticalStrut(5));

        addSnippetButton(left, "Normal sinus rhythm", "Normal sinus rhythm");
        addSnippetButton(left, "LVH", "Left ventricular hypertrophy criteria met");
        addSnippetButton(left, "AF", "Atrial fibrillation with controlled ventricular response");
        addSnippetButton(left, "RBBB", "Right bundle branch block");
        addSnippetButton(left, "Old MI", "Findings consistent with prior MI");
        addSnippetButton(left, "Ischemia", "Non-specific ST-T changes suggestive of ischemia");
        left.add(Box.createVerticalGlue());

        // Center: input areas
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(labeledArea("Findings", findingsArea));
        center.add(Box.createVerticalStrut(10));
        center.add(labeledArea("Impression", impressionArea));
        center.add(Box.createVerticalStrut(10));
        center.add(labeledArea("Comments", commentsArea));

        // Bottom: actions
        JButton saveBtn = new JButton("Save to EMR");
        saveBtn.addActionListener(e -> saveToEmr());

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            findingsArea.setText("");
            impressionArea.setText("");
            commentsArea.setText("");
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(clearBtn);
        actions.add(saveBtn);

        root.add(left, BorderLayout.WEST);
        root.add(center, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel labeledArea(String label, JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(400, area.getRows() * 20));
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void addSnippetButton(JPanel parent, String label, String text) {
        JButton btn = new JButton(label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> appendSnippet(text));
        parent.add(btn);
        parent.add(Box.createVerticalStrut(5));
    }

    private void appendSnippet(String text) {
        findingsArea.append(text + "\n");
    }

    private void saveToEmr() {
        IAITextAreaManager manager = IAIMain.getTextAreaManager();
        if (manager == null || !manager.isReady()) {
            JOptionPane.showMessageDialog(this, "EMR text areas are not ready.", "Not Ready", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("EKG INTERPRETATION\n");
        sb.append("Findings: ").append(findingsArea.getText().trim()).append("\n");
        sb.append("Impression: ").append(impressionArea.getText().trim()).append("\n");
        sb.append("Comments: ").append(commentsArea.getText().trim()).append("\n");
        sb.append("\n— End of EKG Report —");

        String report = sb.toString();
        String stampedReport = String.format("\n< EKG Report > %s\n%s",
                LocalDate.now().format(DateTimeFormatter.ISO_DATE), report);

        manager.focusArea(5);
        manager.insertLineIntoFocusedArea(stampedReport);

        JOptionPane.showMessageDialog(this, "EKG report saved to EMR!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
