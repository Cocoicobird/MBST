package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Installability {
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static final int separatedLibraries = GoodSmellFactor.separatedDependency;
    public static int weight = 1;

    public static int sum() {
        return hasServiceDiscoveryPattern + separatedLibraries;
    }

    public static double calculateInstallability(QualityCharacteristicCalculationService calculationService) {
        return (hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage()
                + separatedLibraries * calculationService.getSeparatedDependencyCoverage()) / (double) sum();
    }
}
