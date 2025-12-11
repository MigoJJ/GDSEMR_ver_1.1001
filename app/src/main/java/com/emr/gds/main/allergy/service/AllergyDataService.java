package com.emr.gds.main.allergy.service;

import com.emr.gds.main.allergy.model.AllergyCause;
import com.emr.gds.main.allergy.model.SymptomItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class AllergyDataService {

    public List<SymptomItem> getSymptomItems() {
        return List.of(
                new SymptomItem("Skin reactions", "Rash", false),
                new SymptomItem("Skin reactions", "Hives (raised, itchy spots)", false),
                new SymptomItem("Skin reactions", "Itching", false),

                new SymptomItem("Swelling", "Swelling of mouth, face, lips, tongue, throat", false),
                new SymptomItem("Swelling", "Angioedema", false),

                new SymptomItem("Respiratory symptoms", "Wheezing", false),
                new SymptomItem("Respiratory symptoms", "Cough", false),
                new SymptomItem("Respiratory symptoms", "Shortness of breath", false),

                new SymptomItem("Gastrointestinal", "Nausea", false),
                new SymptomItem("Gastrointestinal", "Vomiting", false),
                new SymptomItem("Gastrointestinal", "Abdominal cramps", false),

                new SymptomItem("Other", "Runny nose", false),
                new SymptomItem("Other", "Itchy, watery eyes", false),
                new SymptomItem("Other", "Dizziness", false),

                new SymptomItem("Anaphylaxis", "Difficulty swallowing", true),
                new SymptomItem("Anaphylaxis", "Airway tightening", true),
                new SymptomItem("Anaphylaxis", "Drop in blood pressure", true),
                new SymptomItem("Anaphylaxis", "Rapid weak pulse", true),
                new SymptomItem("Anaphylaxis", "Loss of consciousness", true)
        );
    }

    public ObservableList<AllergyCause> getAllergyCauses() {
        return FXCollections.observableArrayList(
                new AllergyCause("Penicillin & derivatives"),
                new AllergyCause("Cephalosporins"),
                new AllergyCause("Sulfa drugs"),
                new AllergyCause("NSAIDs (ibuprofen, aspirin)"),
                new AllergyCause("Contrast dye"),
                new AllergyCause("Latex"),
                new AllergyCause("Peanuts"),
                new AllergyCause("Tree nuts"),
                new AllergyCause("Shellfish"),
                new AllergyCause("Egg"),
                new AllergyCause("Milk"),
                new AllergyCause("Wheat"),
                new AllergyCause("Soy"),
                new AllergyCause("Dust mites"),
                new AllergyCause("Animal dander"),
                new AllergyCause("Bee venom")
        );
    }
}
