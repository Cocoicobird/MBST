package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class ResourceUtilization {
    public static final int noHardCode = GoodSmellFactor.noHardcode;
    public static int weight = 1;

    public static int sum() {
        return noHardCode;
    }

    public static double calculateResourceUtilization(QualityCharacteristicCalculationService calculationService) {
        return noHardCode * calculationService.getNoHardCodeCoverage() / (double) sum();
    }
}
