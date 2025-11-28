package com.emr.gds.main.thyroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Endocrinology-specific calculations and risk assessments.
 */
public class ThyroidRiskCalculator {

    /**
     * Estimates full replacement Levothyroxine dose (1.6 mcg/kg).
     * For elderly or cardiac patients, start lower.
     */
    public static double calculateFullReplacementDose(double weightKg) {
        return Math.round(weightKg * 1.6);
    }

    /**
     * Simplified ATA Risk Stratification for Differentiated Thyroid Cancer.
     * Returns a risk level string (Low, Intermediate, High).
     */
    public static String calculateAtaRisk(
            boolean grossExtrathyroidalExtension,
            boolean incompleteResection,
            boolean distantMetastases,
            boolean aggressiveHistology, // e.g., Tall cell, hobnail, columnar
            boolean vascularInvasion,
            int numberOfLymphNodes,
            double largestNodeSizeCm
    ) {
        // HIGH RISK
        if (grossExtrathyroidalExtension || incompleteResection || distantMetastases) {
            return "High Risk";
        }
        
        // INTERMEDIATE RISK
        if (aggressiveHistology || vascularInvasion || numberOfLymphNodes > 5 || largestNodeSizeCm > 3.0) {
            return "Intermediate Risk";
        }

        // LOW RISK (Default if no adverse features)
        return "Low Risk";
    }

    /**
     * ACR TI-RADS Calculator.
     * Returns the TI-RADS level and recommendation.
     */
    public static TiRadsResult calculateTiRads(
            TiRadsFeature composition,
            TiRadsFeature echogenicity,
            TiRadsFeature shape,
            TiRadsFeature margin,
            TiRadsFeature echogenicFoci
    ) {
        int score = 0;
        score += composition.points;
        score += echogenicity.points;
        score += shape.points;
        score += margin.points;
        score += echogenicFoci.points;

        String level;
        String recommendation;

        if (score == 0) {
            level = "TR1 (Benign)";
            recommendation = "No FNA required.";
        } else if (score < 2) { // Should not happen given 0 is min, but for safety
             level = "TR1 (Benign)";
             recommendation = "No FNA required.";
        } else if (score == 2) {
            level = "TR2 (Not Suspicious)";
            recommendation = "No FNA required.";
        } else if (score == 3) {
            level = "TR3 (Mildly Suspicious)";
            recommendation = "FNA if ≥ 2.5 cm. Follow-up if ≥ 1.5 cm.";
        } else if (score >= 4 && score <= 6) {
            level = "TR4 (Moderately Suspicious)";
            recommendation = "FNA if ≥ 1.5 cm. Follow-up if ≥ 1.0 cm.";
        } else {
            level = "TR5 (Highly Suspicious)";
            recommendation = "FNA if ≥ 1.0 cm. Follow-up if ≥ 0.5 cm.";
        }

        return new TiRadsResult(score, level, recommendation);
    }

    public static class TiRadsResult {
        public final int score;
        public final String level;
        public final String recommendation;

        public TiRadsResult(int score, String level, String recommendation) {
            this.score = score;
            this.level = level;
            this.recommendation = recommendation;
        }
    }

    public enum TiRadsFeature {
        // Composition
        COMP_CYSTIC_SPONGI("Cystic or almost completely cystic / Spongiform", 0),
        COMP_MIXED("Mixed cystic and solid", 1),
        COMP_SOLID("Solid or almost completely solid", 2),

        // Echogenicity
        ECHO_ANECHOIC("Anechoic", 0),
        ECHO_HYPER_ISO("Hyperechoic or Isoechoic", 1),
        ECHO_HYPO("Hypoechoic", 2),
        ECHO_VERY_HYPO("Very hypoechoic", 3),

        // Shape
        SHAPE_WIDER("Wider-than-tall", 0),
        SHAPE_TALLER("Taller-than-wide", 3),

        // Margin
        MARGIN_SMOOTH("Smooth / Ill-defined", 0),
        MARGIN_LOBULATED("Lobulated or irregular", 2),
        MARGIN_EXTRA("Extrathyroidal extension", 3),

        // Echogenic Foci
        FOCI_NONE("None or large comet-tail artifacts", 0),
        FOCI_MACRO("Macrocalcifications", 1),
        FOCI_RIM("Peripheral (rim) calcifications", 2),
        FOCI_PUNCTATE("Punctate echogenic foci", 3);

        public final String description;
        public final int points;

        TiRadsFeature(String description, int points) {
            this.description = description;
            this.points = points;
        }
        
        @Override
        public String toString() {
            return description + " (" + points + "pts)";
        }
    }
}
