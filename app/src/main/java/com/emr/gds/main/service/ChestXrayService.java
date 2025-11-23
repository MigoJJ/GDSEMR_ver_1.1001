package com.emr.gds.main.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility service to send chest X-ray reports to EMR.
 */
public class ChestXrayService {
    private static final int EMR_TARGET_AREA_INDEX = 5;
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final EmrBridgeService bridge;

    public ChestXrayService(EmrBridgeService bridge) {
        this.bridge = bridge;
    }

    public boolean pushReport(String reportBody) {
        if (reportBody == null || reportBody.trim().isEmpty()) {
            return false;
        }
        String stampedReport = "\n< CHEST PA > %s\n%s".formatted(
                LocalDate.now().format(ISO_DATE_FORMATTER),
                reportBody.trim()
        );
        return bridge.insertLine(EMR_TARGET_AREA_INDEX, stampedReport);
    }
}
