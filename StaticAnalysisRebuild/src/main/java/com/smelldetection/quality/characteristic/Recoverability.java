package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Recoverability {
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static final int noWhateverTypes = GoodSmellFactor.noWhateverTypes;
    public static int weight = 1;

    public static int sum() {
        return hasServiceDiscoveryPattern + noWhateverTypes;
    }

    public static double calculateRecoverability(QualityCharacteristicCalculationService calculationService) {
        return (hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage()
                + noWhateverTypes * calculationService.getNoWhateverTypesCoverage()) / (double) sum();
    }
}
