package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class TimeBehavior {
    public static final int noChattyService = GoodSmellFactor.noChattyService;
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int correctServicesCut = GoodSmellFactor.correctServicesCut;
    public static int weight = 1;

    public static int sum() {
        return noChattyService + noGodService + correctServicesCut;
    }

    public static double calculateTimeBehavior(QualityCharacteristicCalculationService calculationService) {
        return (noChattyService * calculationService.getNoChattyServiceCoverage()
                + noGodService * calculationService.getNoGodServiceCoverage()
                + correctServicesCut * calculationService.getCorrectServicesCutCoverage()) / (double) sum();
    }
}
