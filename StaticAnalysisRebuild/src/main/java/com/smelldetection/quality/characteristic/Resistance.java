package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Resistance {
    public static final int separatedLibraries = GoodSmellFactor.separatedDependency;
    public static int weight = 1;

    public static int sum() {
        return separatedLibraries;
    }

    public static double calculateResistance(QualityCharacteristicCalculationService calculationService) {
        return separatedLibraries * calculationService.getSeparatedDependencyCoverage() / (double) sum();
    }
}
