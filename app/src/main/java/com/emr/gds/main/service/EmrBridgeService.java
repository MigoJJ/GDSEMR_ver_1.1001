package com.emr.gds.main.service;

import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;

import java.util.Optional;

/**
 * Thin wrapper around the global EMR bridge to keep UI layers free of static lookups.
 */
public class EmrBridgeService {

    public Optional<IAITextAreaManager> getManager() {
        return Optional.ofNullable(IAIMain.getTextAreaManager())
                .filter(IAITextAreaManager::isReady);
    }

    public boolean insertLine(int areaIndex, String line) {
        return getManager()
                .map(manager -> {
                    manager.focusArea(areaIndex);
                    manager.insertLineIntoFocusedArea(line);
                    return true;
                })
                .orElse(false);
    }

    public boolean insertBlock(int areaIndex, String block) {
        return getManager()
                .map(manager -> {
                    manager.focusArea(areaIndex);
                    manager.insertBlockIntoFocusedArea(block);
                    return true;
                })
                .orElse(false);
    }
}
