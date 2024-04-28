package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Confidentiality {
    public static final int separatedDatabase = GoodSmellFactor.separatedDatabase;
    public static final int appropriateServiceIntimacy = GoodSmellFactor.appropriateServiceIntimacy;
    public static int weight = 1;

    public static int sum() {
        return separatedDatabase + appropriateServiceIntimacy;
    }

    public static double calculateConfidentiality(QualityCharacteristicCalculationService calculationService) {
        return (separatedDatabase * calculationService.getSeparatedDatabaseCoverage()
                + appropriateServiceIntimacy * calculationService.getAppropriateServiceIntimacyCoverage()) / (double) sum();
    }
}
