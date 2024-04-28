package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Testability {
    public static final int noBloatedService = GoodSmellFactor.noBloatedService;
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int noUnnecessarySettings = GoodSmellFactor.noUnnecessarySettings;
    public static int weight = 1;

    public static int sum() {
        return noBloatedService + noGodService + noUnnecessarySettings;
    }

    public static double calculateTestability(QualityCharacteristicCalculationService calculationService) {
        return (noBloatedService * calculationService.getNoBloatedServiceCoverage()
                + noGodService * calculationService.getNoGodServiceCoverage()
                + noUnnecessarySettings * calculationService.getNoUnnecessarySettingsCoverage()) / (double) sum();
    }
}
