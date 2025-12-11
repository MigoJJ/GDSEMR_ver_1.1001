package com.emr.gds.main.allergy.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SymptomItem {
    private final String category;
    private final String symptom;
    private final boolean isAnaphylaxis;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public SymptomItem(String category, String symptom, boolean isAnaphylaxis) {
        this.category = category;
        this.symptom = symptom;
        this.isAnaphylaxis = isAnaphylaxis;
    }

    public String getCategory() {
        return category;
    }

    public String getSymptom() {
        return symptom;
    }

    public boolean isAnaphylaxis() {
        return isAnaphylaxis;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean v) {
        selected.set(v);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
