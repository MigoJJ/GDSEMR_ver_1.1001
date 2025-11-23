package com.emr.gds.main.vaccine;

import com.emr.gds.main.service.EmrBridgeService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles business logic for vaccine logging and EMR insertion.
 */
public class VaccineService {

    private static final int AREA_PRESENT_ILLNESS = 1;
    private static final int AREA_ASSESSMENT = 7;
    private static final int AREA_PLAN = 8;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final EmrBridgeService bridge;

    public VaccineService(EmrBridgeService bridge) {
        this.bridge = bridge;
    }

    public boolean logVaccine(String vaccineName) {
        var subjectiveNote = """
The patient visits for Vaccination
  [ ✔ ]  no allergy to eggs, chicken, or any other component of the vaccine.
  [ ✔ ]  no s/p Guillain-Barré syndrome.
  [ ✔ ]  no adverse reactions to previous vaccines.
  [ ✔ ]  no immunosuppression.
""";
        var today = DATE_FORMAT.format(new Date());
        var assessment = "\n #  " + vaccineName + "  [" + today + "]";
        var plan = "...Vaccination as scheduled";

        var okSubjective = bridge.insertBlock(AREA_PRESENT_ILLNESS, subjectiveNote);
        var okAssessment = bridge.insertLine(AREA_ASSESSMENT, assessment);
        var okPlan = bridge.insertLine(AREA_PLAN, plan);
        return okSubjective && okAssessment && okPlan;
    }
}
