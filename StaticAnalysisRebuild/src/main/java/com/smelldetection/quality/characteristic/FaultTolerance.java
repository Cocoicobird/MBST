package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class FaultTolerance {
    public static final int noCyclicReference = GoodSmellFactor.noCircleReference;
    public static final int noESBUsage = GoodSmellFactor.noESB;
    public static final int separatedDatabase = GoodSmellFactor.separatedDatabase;
    public static final int appropriateServiceIntimacy = GoodSmellFactor.appropriateServiceIntimacy;
    public static final int noWhateverTypes = GoodSmellFactor.noWhateverTypes;
    public static final int separatedLibraries = GoodSmellFactor.separatedDependency;
    public static int weight = 1;

    public static int sum() {
        return noCyclicReference + noESBUsage + separatedDatabase
                + appropriateServiceIntimacy + noWhateverTypes + separatedLibraries;
    }

    public static double calculateFaultTolerance(QualityCharacteristicCalculationService calculationService) {
        return (noCyclicReference * calculationService.getNoWhateverTypesCoverage()
                + noESBUsage * calculationService.getNoESBCoverage()
                + separatedDatabase * calculationService.getSeparatedDatabaseCoverage()
                + appropriateServiceIntimacy * calculationService.getAppropriateServiceIntimacyCoverage()
                + noWhateverTypes * calculationService.getNoWhateverTypesCoverage()
                + separatedLibraries * calculationService.getSeparatedDependencyCoverage()) / (double) sum();
    }
}
