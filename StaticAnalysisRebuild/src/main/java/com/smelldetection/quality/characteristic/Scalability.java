package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Scalability {
    public static final int noESBUsage = GoodSmellFactor.noESB;
    public static final int noGreedyService = GoodSmellFactor.noServiceGreedy;
    public static final int correctServicesCut = GoodSmellFactor.correctServicesCut;
    public static final int noHardCode = GoodSmellFactor.noHardcode;
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static int weight = 1;

    public static int sum() {
        return noESBUsage + noGreedyService + correctServicesCut + noHardCode + hasServiceDiscoveryPattern;
    }

    public static double calculateScalability(QualityCharacteristicCalculationService calculationService) {
        return (noESBUsage * calculationService.getNoESBCoverage()
                + noGreedyService * calculationService.getNoGreedyService()
                + correctServicesCut * calculationService.getCorrectServicesCutCoverage()
                + noHardCode * calculationService.getNoHardCodeCoverage()
                + hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage()) / (double) sum();
    }
}
