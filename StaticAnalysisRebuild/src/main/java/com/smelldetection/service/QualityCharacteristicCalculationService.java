package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceGreedyItem;
import com.smelldetection.entity.smell.detail.*;
import com.smelldetection.quality.characteristic.*;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@Service
public class QualityCharacteristicCalculationService {
    public static int microserviceCount;
    public static double qualityScore;
    private Map<String, Double> microserviceWeights;
    private double noBloatedServiceCoverage;
    private double noChattyServiceCoverage;
    private double noCyclicReferenceCoverage;
    private double noDuplicatedServiceCoverage;
    private double noESBCoverage;
    private double noGodServiceCoverage;
    private double noGreedyService;
    private double noHardCodeCoverage;
    private double noHubServiceCoverage;
    private double noLocalLoggingCoverage;
    private double hasApiVersionCoverage;
    private double hasGatewayCoverage;
    private double hasHealthCheckCoverage;
    private double hasServiceDiscoveryPatternCoverage;
    private double goodRestfulApiDesignCoverage;
    private double noScatterServiceCoverage;
    private double separatedDatabaseCoverage;
    private double appropriateServiceIntimacyCoverage;
    private double separatedDependencyCoverage;
    private double noUnnecessarySettingsCoverage;
    private double noWhateverTypesCoverage;
    private double correctServicesCutCoverage;

    public QualityCharacteristicCalculationService() {
        this.microserviceWeights = new HashMap<>();
    }

    public void calculateStaticAnalysisQualityScore() {
        qualityScore = 0.0;
        int weightSum = sumQualityCharacteristicWeight();
        qualityScore = (Adaptability.weight * Adaptability.calculateAdaptability(this)
                + Analyzability.weight * Analyzability.calculateAnalyzability(this)
                + Availability.weight * Availability.calculateAvailability(this)
                + Compatibility.weight * Compatibility.calculateCompatibility(this)
                + Confidentiality.weight * Confidentiality.calculateConfidentiality(this)
                + Faultlessness.weight * Faultlessness.calculateFaultlessness(this)
                + FaultTolerance.weight * FaultTolerance.calculateFaultTolerance(this)
                + Installability.weight * Installability.calculateInstallability(this)
                + Modifiability.weight * Modifiability.calculateModifiability(this)
                + Modularity.weight * Modularity.calculateModularity(this)
                + PerformanceEfficiency.weight * PerformanceEfficiency.calculatePerformanceEfficiency(this)
                + Recoverability.weight * Recoverability.calculateRecoverability(this)
                + Resistance.weight * Resistance.calculateResistance(this)
                + ResourceUtilization.weight * ResourceUtilization.calculateResourceUtilization(this)
                + Reusability.weight * Reusability.calculateReusability(this)
                + Scalability.weight * Scalability.calculateScalability(this)
                + Testability.weight * Testability.calculateTestability(this)
                + TimeBehavior.weight * TimeBehavior.calculateTimeBehavior(this)) * 100 / (double) weightSum;
    }

    public int sumQualityCharacteristicWeight() {
        return Adaptability.weight + Analyzability.weight + Availability.weight + Compatibility.weight
                + Confidentiality.weight + Faultlessness.weight + FaultTolerance.weight + Installability.weight
                + Modifiability.weight + Modularity.weight + PerformanceEfficiency.weight + Recoverability.weight
                + Resistance.weight + ResourceUtilization.weight + Reusability.weight + Scalability.weight
                + Testability.weight + TimeBehavior.weight;
    }

    public void processStaticAnalysisResult(Map<String, Object> result) {
        setMicroserviceRankDetails((List<MicroserviceRankDetail>) result.get("microserviceRank"));
        calculateNoBloatedServiceCoverage((BloatedServiceDetail) result.get("bloatedService"));
        calculateNoChattyServiceCoverage((ChattyServiceDetail) result.get("chattyService"));
        calculateNoCyclicReferenceCoverage((CyclicReferenceDetail) result.get("cyclicReference"));
        calculateNoDuplicatedServiceCoverage((Set<List<DuplicatedServiceDetail>>) result.get("duplicatedService"));
        calculateNoESBUsageCoverage((ESBUsageDetail) result.get("esbUsage"));
        calculateNoGodServiceCoverage((GodServiceDetail) result.get("godService"));
        calculateNoGreedyServiceCoverage((ServiceGreedyDetail) result.get("greedyService"));
        calculateNoHardCodeCoverage((HardCodeDetail) result.get("hardCode"));
        calculateNoHubCoverage((HubServiceDetail) result.get("hubService"));
        calculateNoLocalLoggingCoverage((LocalLoggingDetail) result.get("localLogging"));
        calculateHasApiVersionCoverage((ApiVersionDetail) result.get("noApiVersion"));
        calculateHasGatewayCoverage((NoGatewayDetail) result.get("noGateway"));
        calculateHasHealthCheckCoverage((NoHealthCheckAndNoServiceDiscoveryPatternDetail) result.get("noHealthCheck"));
        calculateHasServiceDiscoveryPatternCoverage((NoHealthCheckAndNoServiceDiscoveryPatternDetail) result.get("noServiceDiscoveryPattern"));
        calculateGoodRestfulApiDesignCoverage((ApiDesignDetail) result.get("poorRestfulApiDesign"));
        calculateNoScatterServiceCoverage((ScatteredServiceDetail) result.get("scatteredService"));
        calculateSeparatedDatabaseCoverage((Map<String, List<String>>) result.get("sharedDatabases"));
        calculateAppropriateServiceIntimacyCoverage((Map<String, List<String>>) result.get("serviceIntimacy"));
        calculateSeparatedDependencyCoverage((SharedLibraryDetail) result.get("sharedLibraries"));
        calculateNoUnnecessarySettingsCoverage((UnnecessarySettingsDetail) result.get("unnecessarySettings"));
        calculateNoWhateverTypes((WhateverTypesDetail) result.get("whateverTypes"));
        calculateCorrectServicesCutCoverage((WrongCutDetail) result.get("wrongCut"));
    }

    public void setMicroserviceRankDetails(List<MicroserviceRankDetail> microserviceRankDetails) {
        QualityCharacteristicCalculationService.microserviceCount = microserviceRankDetails.size();
        for (MicroserviceRankDetail microserviceRankDetail : microserviceRankDetails) {
            this.microserviceWeights.put(microserviceRankDetail.getName(), microserviceRankDetail.getWeight());
        }
    }

    public void calculateNoBloatedServiceCoverage(BloatedServiceDetail bloatedServiceDetail) {
        if (!bloatedServiceDetail.isStatus()) {
            this.noBloatedServiceCoverage = 1;
        } else {
            for (String microserviceName : bloatedServiceDetail.getBloatedServices().keySet()) {
                this.noBloatedServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noBloatedServiceCoverage = 1 - this.noBloatedServiceCoverage;
        }
    }

    public void calculateNoChattyServiceCoverage(ChattyServiceDetail chattyServiceDetail) {
        if (!chattyServiceDetail.isStatus()) {
            this.noChattyServiceCoverage = 1;
        } else {
            for (String microserviceName : chattyServiceDetail.getChattyServices().keySet()) {
                this.noChattyServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noChattyServiceCoverage = 1 - this.noChattyServiceCoverage;
        }
    }

    public void calculateNoCyclicReferenceCoverage(CyclicReferenceDetail cyclicReferenceDetail) {
        if (!cyclicReferenceDetail.isStatus()) {
            this.noCyclicReferenceCoverage = 1;
        } else {
            for (String microserviceName : cyclicReferenceDetail.getCyclicReferences().keySet()) {
                this.noCyclicReferenceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noCyclicReferenceCoverage = 1 - this.noCyclicReferenceCoverage;
        }
    }

    public void calculateNoDuplicatedServiceCoverage(Set<List<DuplicatedServiceDetail>> duplicatedServiceDetail) {
        Set<String> microserviceNames = new LinkedHashSet<>();
        for (List<DuplicatedServiceDetail> duplicatedService : duplicatedServiceDetail) {
            for (DuplicatedServiceDetail detail : duplicatedService) {
                microserviceNames.add(detail.getMicroserviceName());
            }
        }
        for (String microserviceName : microserviceNames) {
            this.noDuplicatedServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
        }
        this.noDuplicatedServiceCoverage = 1 - this.noDuplicatedServiceCoverage;
    }

    public void calculateNoESBUsageCoverage(ESBUsageDetail esbUsageDetail) {
        if (!esbUsageDetail.isStatus()) {
            this.noESBCoverage = 1;
        } else {
            for (String microserviceName : esbUsageDetail.getServiceCallItems().keySet()) {
                if (esbUsageDetail.getServiceCallItems().get(microserviceName).isESBUsage()) {
                    this.noESBCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
                }
            }
            this.noESBCoverage = 1 - this.noESBCoverage;
        }
    }

    public void calculateNoGodServiceCoverage(GodServiceDetail godServiceDetail) {
        if (!godServiceDetail.isStatus()) {
            this.noGodServiceCoverage = 1;
        } else {
            for (String microserviceName : godServiceDetail.getGodServices().keySet()) {
                this.noGodServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noGodServiceCoverage = 1 - this.noGodServiceCoverage;
        }
    }

    public void calculateNoGreedyServiceCoverage(ServiceGreedyDetail serviceGreedyDetail) {
        if (!serviceGreedyDetail.isStatus()) {
            this.noGreedyService = 1;
        } else {
            for (ServiceGreedyItem serviceGreedyItem : serviceGreedyDetail.getGreedyServices()) {
                this.noGreedyService += this.microserviceWeights.getOrDefault(serviceGreedyItem.getMicroserviceName(), 0.0);
            }
            this.noGreedyService = 1 - this.noGreedyService;
        }
    }

    public void calculateNoHardCodeCoverage(HardCodeDetail hardCodeDetail) {
        if (!hardCodeDetail.isStatus()) {
            this.noHardCodeCoverage = 1;
        } else {
            for (String microserviceName : hardCodeDetail.getHardCodes().keySet()) {
                this.noHardCodeCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noHardCodeCoverage = 1 - this.noHardCodeCoverage;
        }
    }

    public void calculateNoHubCoverage(HubServiceDetail hubServiceDetail) {
        if (!hubServiceDetail.isStatus()) {
            this.noHubServiceCoverage = 1;
        } else {
            for (String microserviceName : hubServiceDetail.getHubClasses().keySet()) {
                this.noHubServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noHubServiceCoverage = 1 - this.noHubServiceCoverage;
        }
    }

    public void calculateNoLocalLoggingCoverage(LocalLoggingDetail localLoggingDetail) {
        if (!localLoggingDetail.isStatus()) {
            this.noLocalLoggingCoverage = 1;
        } else {
            for (String microserviceName : localLoggingDetail.getLogs().keySet()) {
                if (localLoggingDetail.getLogs().get(microserviceName))
                    this.noLocalLoggingCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noLocalLoggingCoverage = 1 - this.noLocalLoggingCoverage;
        }
    }

    public void calculateHasApiVersionCoverage(ApiVersionDetail apiVersionDetail) {
        if (!apiVersionDetail.isStatus()) {
            this.hasApiVersionCoverage = 1;
        } else {
            for (String microserviceName : apiVersionDetail.getNoVersion().keySet()) {
                if (!apiVersionDetail.getNoVersion().get(microserviceName).isEmpty()) {
                    this.hasApiVersionCoverage += this.microserviceWeights.get(microserviceName);
                }
            }
            this.hasApiVersionCoverage = 1 - this.hasApiVersionCoverage;
        }
    }

    public void calculateHasGatewayCoverage(NoGatewayDetail noGatewayDetail) {
        if (!noGatewayDetail.isStatus()) {
            this.hasGatewayCoverage = 1;
        } else {
            this.hasGatewayCoverage = 0;
        }
    }

    public void calculateHasHealthCheckCoverage(NoHealthCheckAndNoServiceDiscoveryPatternDetail noHealthCheckDetail) {
        if (!noHealthCheckDetail.isStatus()) {
            this.hasHealthCheckCoverage = 1;
        } else {
            for (String microserviceName : noHealthCheckDetail.getHasDiscovery().keySet()) {
                if (noHealthCheckDetail.getHasDiscovery().get(microserviceName)) {
                    this.hasHealthCheckCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
                }
            }
        }
    }

    public void calculateHasServiceDiscoveryPatternCoverage(NoHealthCheckAndNoServiceDiscoveryPatternDetail noServiceDiscoveryPatternDetail) {
        if (!noServiceDiscoveryPatternDetail.isStatus()) {
            this.hasServiceDiscoveryPatternCoverage = 1;
        } else {
            for (String microserviceName : noServiceDiscoveryPatternDetail.getHasDiscovery().keySet()) {
                if (noServiceDiscoveryPatternDetail.getHasDiscovery().get(microserviceName)) {
                    this.hasServiceDiscoveryPatternCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
                }
            }
        }
    }

    public void calculateGoodRestfulApiDesignCoverage(ApiDesignDetail apiDesignDetail) {
        if (!apiDesignDetail.isStatus()) {
            this.goodRestfulApiDesignCoverage = 1;
        } else {
            Set<String> microserviceNames = new LinkedHashSet<>();
            microserviceNames.addAll(apiDesignDetail.getNoVersion().keySet());
            microserviceNames.addAll(apiDesignDetail.getNoStandard().keySet());
            microserviceNames.addAll(apiDesignDetail.getMissingHttpMethod().keySet());
            for (String microserviceName : microserviceNames) {
                this.goodRestfulApiDesignCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.goodRestfulApiDesignCoverage = 1 - this.goodRestfulApiDesignCoverage;
        }
    }

    public void calculateNoScatterServiceCoverage(ScatteredServiceDetail scatteredServiceDetail) {
        if (!scatteredServiceDetail.isStatus()) {
            this.noScatterServiceCoverage = 1;
        } else {
            Set<String> microserviceNames = new LinkedHashSet<>();
            for (Set<String> scatteredServicesSet : scatteredServiceDetail.getScatteredServices()) {
                microserviceNames.addAll(scatteredServicesSet);
            }
            for (String microserviceName : microserviceNames) {
                this.noScatterServiceCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noScatterServiceCoverage = 1 - this.noScatterServiceCoverage;
        }
    }

    public void calculateSeparatedDatabaseCoverage(Map<String, List<String>> sharedDatabases) {
        Set<String> microserviceNames = new LinkedHashSet<>();
        for (String sharedDatabase : sharedDatabases.keySet()) {
            microserviceNames.addAll(sharedDatabases.get(sharedDatabase));
        }
        for (String microserviceName : microserviceNames) {
            this.separatedDatabaseCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
        }
        this.separatedDatabaseCoverage = 1 - this.separatedDatabaseCoverage;
    }

    public void calculateAppropriateServiceIntimacyCoverage(Map<String, List<String>> serviceIntimacy) {
        Set<String> microserviceNames = new LinkedHashSet<>();
        for (String microserviceName : serviceIntimacy.keySet()) {
            microserviceNames.add(microserviceName);
            microserviceNames.addAll(serviceIntimacy.get(microserviceName));
        }
        for (String microserviceName : microserviceNames) {
            this.appropriateServiceIntimacyCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
        }
        this.appropriateServiceIntimacyCoverage = 1 - this.appropriateServiceIntimacyCoverage;
    }

    public void calculateSeparatedDependencyCoverage(SharedLibraryDetail sharedLibraryDetail) {
        if (!sharedLibraryDetail.isStatus()) {
            this.separatedDependencyCoverage = 1;
        } else {
            Set<String> microserviceNames = new LinkedHashSet<>();
            for (String dependency : sharedLibraryDetail.getSharedLibraries().keySet()) {
                microserviceNames.addAll(sharedLibraryDetail.getSharedLibraries().get(dependency));
            }
            for (String microserviceName : microserviceNames) {
                this.separatedDependencyCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.separatedDependencyCoverage = 1 - this.separatedDependencyCoverage;
        }
    }

    public void calculateNoUnnecessarySettingsCoverage(UnnecessarySettingsDetail unnecessarySettingsDetail) {
        if (!unnecessarySettingsDetail.isStatus()) {
            this.noUnnecessarySettingsCoverage = 1;
        } else {
            for (String microserviceName : unnecessarySettingsDetail.getMicroserviceNameToConfigurations().keySet()) {
                this.noUnnecessarySettingsCoverage = this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noUnnecessarySettingsCoverage = 1 - this.noUnnecessarySettingsCoverage;
        }
    }

    public void calculateNoWhateverTypes(WhateverTypesDetail whateverTypesDetail) {
        if (!whateverTypesDetail.isStatus()) {
            this.noWhateverTypesCoverage = 1;
        } else {
            for (String microserviceName : whateverTypesDetail.getReturnTypes().keySet()) {
                this.noWhateverTypesCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.noWhateverTypesCoverage = 1 - this.noWhateverTypesCoverage;
        }
    }

    public void calculateCorrectServicesCutCoverage(WrongCutDetail wrongCutDetail) {
        if (!wrongCutDetail.isStatus()) {
            this.correctServicesCutCoverage = 1;
        } else {
            for (String microserviceName : wrongCutDetail.getWrongCutMicroservices().keySet()) {
                this.correctServicesCutCoverage += this.microserviceWeights.getOrDefault(microserviceName, 0.0);
            }
            this.correctServicesCutCoverage = 1 - this.correctServicesCutCoverage;
        }
    }
}
