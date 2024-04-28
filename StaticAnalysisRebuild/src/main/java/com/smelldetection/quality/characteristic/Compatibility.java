package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Compatibility {
    public static final int goodRestfulApiDesign = GoodSmellFactor.goodRestfulApiDesign;
    public static int weight = 1;

    public static int sum() {
        return goodRestfulApiDesign;
    }

    public static double calculateCompatibility(QualityCharacteristicCalculationService calculationService) {
        return goodRestfulApiDesign * calculationService.getGoodRestfulApiDesignCoverage() / (double) sum();
    }
}
