package com.emr.gds.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Utility to size stages to a comfortable, screen-aware default.
 */
public final class StageSizing {

    private static final double DEFAULT_WIDTH_RATIO = 0.7;
    private static final double DEFAULT_HEIGHT_RATIO = 0.9;
    private static final double MIN_WIDTH = 900;
    private static final double MIN_HEIGHT = 750;

    private StageSizing() {
    }

    public static void fitToScreen(Stage stage) {
        fitToScreen(stage, DEFAULT_WIDTH_RATIO, DEFAULT_HEIGHT_RATIO);
    }

    public static void fitToScreen(Stage stage, double widthRatio, double heightRatio) {
        fitToScreen(stage, widthRatio, heightRatio, MIN_WIDTH, MIN_HEIGHT);
    }

    public static void fitToScreen(Stage stage, double widthRatio, double heightRatio, double minWidth, double minHeight) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double effectiveMinWidth = Math.min(minWidth, bounds.getWidth());
        double effectiveMinHeight = Math.min(minHeight, bounds.getHeight());

        double width = clamp(bounds.getWidth() * widthRatio, effectiveMinWidth, bounds.getWidth());
        double height = clamp(bounds.getHeight() * heightRatio, effectiveMinHeight, bounds.getHeight());

        stage.setMinWidth(effectiveMinWidth);
        stage.setMinHeight(effectiveMinHeight);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
