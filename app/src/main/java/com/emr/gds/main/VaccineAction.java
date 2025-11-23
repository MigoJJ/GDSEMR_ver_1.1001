package com.emr.gds.main;

import com.emr.gds.main.service.EmrBridgeService;
import com.emr.gds.main.vaccine.VaccineController;
import com.emr.gds.main.vaccine.VaccineService;

/**
 * A JavaFX tool window for quickly logging vaccine administrations.
 * This window appears as a bottom-right overlay and provides buttons for common vaccines.
 */
public class VaccineAction {

    private static VaccineController controller;

    /**
     * Opens the vaccine logging window. If the window is already open, it brings it to the front.
     */
    public static void open() {
        if (controller == null) {
            controller = new VaccineController(new VaccineService(new EmrBridgeService()));
        }
        controller.show();
    }
}
