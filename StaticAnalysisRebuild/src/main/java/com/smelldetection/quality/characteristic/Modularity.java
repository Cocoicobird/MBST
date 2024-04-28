package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Modularity {
    public static final int noBloatedService = GoodSmellFactor.noBloatedService;
    public static final int noDuplicatedService = GoodSmellFactor.noDuplicatedServices;
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int noGreedyService = GoodSmellFactor.noServiceGreedy;
    public static final int correctServicesCut = GoodSmellFactor.correctServicesCut;
    public static final int noScatteredFunctionality = GoodSmellFactor.noScatteredFunctionality;
    public static final int separatedDatabase = GoodSmellFactor.separatedDatabase;
    public static final int appropriateServiceIntimacy = GoodSmellFactor.appropriateServiceIntimacy;
    public static final int separatedLibraries = GoodSmellFactor.separatedDependency;
    public static int weight = 1;

    public static int sum() {
        return noBloatedService + noDuplicatedService + noGodService + noGreedyService + correctServicesCut
                + noScatteredFunctionality + separatedDatabase + appropriateServiceIntimacy + separatedLibraries;
    }

    public static double calculateModularity(QualityCharacteristicCalculationService calculationService) {
        return (noBloatedService * calculationService.getNoBloatedServiceCoverage()
                + noDuplicatedService * calculationService.getNoDuplicatedServiceCoverage()
                + noGodService * calculationService.getNoGodServiceCoverage()
                + noGreedyService * calculationService.getNoGreedyService()
                + correctServicesCut * calculationService.getCorrectServicesCutCoverage()
                + noScatteredFunctionality * calculationService.getNoScatterServiceCoverage()
                + separatedDatabase * calculationService.getSeparatedDatabaseCoverage()
                + appropriateServiceIntimacy * calculationService.getAppropriateServiceIntimacyCoverage()
                + separatedLibraries * calculationService.getSeparatedDependencyCoverage()) / (double) sum();
    }
}
