package com.emr.gds.main.allergy.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AllergyCause {
    private final StringProperty name = new SimpleStringProperty();

    public AllergyCause(String name) {
        this.name.set(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }
}
