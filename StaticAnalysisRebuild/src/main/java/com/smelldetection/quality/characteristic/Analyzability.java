package com.smelldetection.quality.characteristic;

import com.smelldetection.quality.GoodSmellFactor;
import com.smelldetection.service.QualityCharacteristicCalculationService;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class Analyzability {
    public static final int noCyclicReference = GoodSmellFactor.noCircleReference;
    public static final int noESBUsage = GoodSmellFactor.noESB;
    public static final int noGodService = GoodSmellFactor.noGodComponent;
    public static final int noGreedyService = GoodSmellFactor.noServiceGreedy;
    public static final int correctServicesCut = GoodSmellFactor.correctServicesCut;
    public static final int noHub = GoodSmellFactor.noHub;
    public static final int noLocalLogging = GoodSmellFactor.noLocalLogging;
    public static final int hasApiVersion = GoodSmellFactor.hasApiVersion;
    public static final int hasApiGateway = GoodSmellFactor.hasApiGateway;
    public static final int hasServiceDiscoveryPattern = GoodSmellFactor.hasServiceDiscoveryPattern;
    public static final int noScatteredFunctionality = GoodSmellFactor.noScatteredFunctionality;
    public static int weight = 1;

    public static int sum() {
        return noCyclicReference + noESBUsage + noGodService + noGreedyService + correctServicesCut + noHub
                + noLocalLogging + hasApiVersion + hasApiGateway + hasServiceDiscoveryPattern + noScatteredFunctionality;
    }

    public static double calculateAnalyzability(QualityCharacteristicCalculationService calculationService) {
        return (noCyclicReference * calculationService.getNoCyclicReferenceCoverage()
                + noESBUsage * calculationService.getNoESBCoverage()
                + noGodService * calculationService.getNoGodServiceCoverage()
                + noGreedyService * calculationService.getNoGreedyService()
                + correctServicesCut * calculationService.getCorrectServicesCutCoverage()
                + noHub * calculationService.getNoHubServiceCoverage()
                + noLocalLogging * calculationService.getNoLocalLoggingCoverage()
                + hasApiVersion * calculationService.getHasApiVersionCoverage()
                + hasApiGateway * calculationService.getHasGatewayCoverage()
                + hasServiceDiscoveryPattern * calculationService.getHasServiceDiscoveryPatternCoverage()
                + noScatteredFunctionality * calculationService.getNoScatterServiceCoverage()) / (double) sum();
    }
}
