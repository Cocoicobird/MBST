package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Modifiability {
    public static final int noESBUsage = GoodSmellFactor.noESB;
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int correctServicesCut = GoodSmellFactor.correctServicesCut;
    public static final int noHardCode = GoodSmellFactor.noHardcode;
    public static final int noHub = GoodSmellFactor.noHub;
    public static final int hasApiVersion = GoodSmellFactor.hasApiVersion;
    public static final int hasApiGateway = GoodSmellFactor.hasApiGateway;
    public static final int separatedDatabase = GoodSmellFactor.separatedDatabase;
    public static final int appropriateServiceIntimacy = GoodSmellFactor.appropriateServiceIntimacy;
    public static final int separatedLibraries = GoodSmellFactor.separatedDependency;
    public static int weight = 1;

    public static int sum() {
        return noESBUsage + noGodService + correctServicesCut + noHardCode + noHub
                + hasApiVersion + hasApiGateway + separatedDatabase + appropriateServiceIntimacy + separatedLibraries;
    }

    public static double calculateModifiability(QualityCharacteristicCalculationService calculationService) {
        return (noESBUsage * calculationService.getNoESBCoverage()
                + noGodService * calculationService.getNoGodServiceCoverage()
                + correctServicesCut * calculationService.getCorrectServicesCutCoverage()
                + noHardCode * calculationService.getNoHardCodeCoverage()
                + noHub * calculationService.getNoHubServiceCoverage()
                + hasApiVersion * calculationService.getHasApiVersionCoverage()
                + hasApiGateway * calculationService.getHasGatewayCoverage()
                + separatedDatabase * calculationService.getSeparatedDatabaseCoverage()
                + appropriateServiceIntimacy * calculationService.getAppropriateServiceIntimacyCoverage()
                + separatedLibraries * calculationService.getSeparatedDependencyCoverage()) / (double) sum();
    }
}
