package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Reusability {
    public static final int noBloatedService = GoodSmellFactor.noBloatedService;
    public static final int noDuplicatedService = GoodSmellFactor.noDuplicatedServices;
    public static final int noGreedyService = GoodSmellFactor.noServiceGreedy;
    public static int weight = 1;

    public static int sum() {
        return noBloatedService + noDuplicatedService + noGreedyService;
    }

    public static double calculateReusability(QualityCharacteristicCalculationService calculationService) {
        return (noBloatedService * calculationService.getNoBloatedServiceCoverage()
                + noDuplicatedService * calculationService.getNoDuplicatedServiceCoverage()
                + noGreedyService * calculationService.getNoGreedyService()) / (double) sum();
    }
}
