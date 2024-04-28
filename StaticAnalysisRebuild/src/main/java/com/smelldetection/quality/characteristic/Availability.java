package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Availability {
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int hasHealthCheck = GoodSmellFactor.hasHealthCheck;
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static final int noWhateverTypes = GoodSmellFactor.noWhateverTypes;
    public static int weight = 1;

    public static int sum() {
        return noGodService + hasHealthCheck + hasServiceDiscoveryPattern + noWhateverTypes;
    }

    public static double calculateAvailability(QualityCharacteristicCalculationService calculationService) {
        return (noGodService * calculationService.getNoGodServiceCoverage()
                + hasHealthCheck * calculationService.getHasHealthCheckCoverage()
                + hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage()
                + noWhateverTypes * calculationService.getNoWhateverTypesCoverage()) / (double) sum();
    }
}
