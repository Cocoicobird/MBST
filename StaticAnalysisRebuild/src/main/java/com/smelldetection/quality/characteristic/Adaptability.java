package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Adaptability {
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static int weight = 1;

    public static int sum() {
        return hasServiceDiscoveryPattern;
    }

    public static double calculateAdaptability(QualityCharacteristicCalculationService calculationService) {
        return hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage() / (double) sum();
    }
}
