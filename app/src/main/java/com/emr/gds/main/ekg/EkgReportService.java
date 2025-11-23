package com.emr.gds.main.ekg;

import com.emr.gds.main.service.EmrBridgeService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Domain logic for formatting and pushing EKG reports into the EMR.
 */
public class EkgReportService {

    private static final int EMR_TARGET_AREA_INDEX = 5; // objective area
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final EmrBridgeService bridge;

    public EkgReportService(EmrBridgeService bridge) {
        this.bridge = bridge;
    }

    public String formatReport(String rawText) {
        return "\n< EKG Report - %s >\n%s".formatted(LocalDate.now().format(DATE_FORMATTER), rawText.trim());
    }

    public boolean pushToEmr(String formattedReport) {
        return bridge.insertLine(EMR_TARGET_AREA_INDEX, formattedReport);
    }
}
