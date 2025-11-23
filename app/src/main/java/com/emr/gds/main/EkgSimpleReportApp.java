package com.emr.gds.main;

import com.emr.gds.main.ekg.EkgReportService;
import com.emr.gds.main.ekg.EkgSimpleReportController;
import com.emr.gds.main.ekg.EkgSimpleReportView;
import com.emr.gds.main.service.EmrBridgeService;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX version of the simple EKG report window.
 * Delegates to Controller/Service/View to reduce UI/business coupling.
 */
public class EkgSimpleReportApp extends Stage {

    private static EkgSimpleReportApp active;

    private final EkgSimpleReportView view;
    private final EkgSimpleReportController controller;

    public EkgSimpleReportApp() {
        setTitle("Simple EKG Interpretation");
        initModality(Modality.NONE);
        view = new EkgSimpleReportView();
        controller = new EkgSimpleReportController(view, new EkgReportService(new EmrBridgeService()));
        setScene(new Scene(view.createContent(), 650, 500));
    }

    public static void open() {
        if (active != null && active.isShowing()) {
            active.toFront();
            return;
        }
        active = new EkgSimpleReportApp();
        active.show();
        active.setOnHidden(e -> active = null);
    }
}
