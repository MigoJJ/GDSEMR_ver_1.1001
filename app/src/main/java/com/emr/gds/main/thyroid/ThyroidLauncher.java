package com.emr.gds.main.thyroid;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Central entry points for thyroid-related UIs.
 */
public final class ThyroidLauncher {

    private ThyroidLauncher() {
    }

    /**
     * Opens the general thyroid EMR pane in its own window.
     */
    public static void openThyroidEmr() {
        ThyroidEntry entry = new ThyroidEntry();
        ThyroidPane root = new ThyroidPane(entry);
        Stage stage = new Stage();
        stage.setTitle("Thyroid EMR");
        stage.setScene(new Scene(root, 900, 650));
        stage.show();
    }

    /**
     * Opens the pregnancy-focused thyroid helper in its own window.
     */
    public static void openThyroidPregnancy() {
        ThyroidPregnancy root = new ThyroidPregnancy();
        Stage stage = new Stage();
        stage.setTitle("Thyroid Pregnancy");
        stage.setScene(new Scene(root, 620, 720));
        stage.show();
    }
}
