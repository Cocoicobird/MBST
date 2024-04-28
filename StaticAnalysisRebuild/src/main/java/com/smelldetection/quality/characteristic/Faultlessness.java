package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Faultlessness {
    public static final int noWhateverTypes = GoodSmellFactor.noWhateverTypes;
    public static int weight = 1;

    public static int sum() {
        return noWhateverTypes;
    }

    public static double calculateFaultlessness(QualityCharacteristicCalculationService calculationService) {
        return noWhateverTypes * calculationService.getNoWhateverTypesCoverage() / (double) sum();
    }
}
