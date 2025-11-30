package com.emr.gds.main.vaccine;

import javafx.stage.Stage;
import com.emr.gds.main.vaccine.VaccineSideEffect;
import javafx.fxml.FXML;

/**
 * Controller that connects VaccineView actions to VaccineService.
 */
public class VaccineController {

    private final VaccineService service;
    private final VaccineView view;

    public VaccineController(VaccineService service) {
        this.service = service;
        this.view = new VaccineView(this::handleAction);
    }

    private void handleAction(String text) {
        switch (text) {
            case "Quit" -> view.getStage().close();
            case "Side Effect" -> VaccineSideEffect.open();
            default -> {
                boolean ok = service.logVaccine(text);
                if (!ok) {
                    view.getStage().requestFocus();
                }
            }
        }
    }

    public void show() {
        Stage stage = view.getStage();
        if (!stage.isShowing()) {
            stage.show();
        } else {
            stage.toFront();
        }
    }
}
