package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class PerformanceEfficiency {
    public static final int noChattyService = GoodSmellFactor.noChattyService;
    public static int weight = 1;

    public static int sum() {
        return noChattyService;
    }

    public static double calculatePerformanceEfficiency(QualityCharacteristicCalculationService calculationService) {
        return noChattyService * calculationService.getNoChattyServiceCoverage() / (double) sum();
    }
}
