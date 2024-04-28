package com.smelldetection.entity.smell.summary;

import com.smelldetection.entity.smell.detail.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 微服务异味静态分析结果
 */
@Data
public class StaticAnalysisSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private BloatedServiceDetail bloatedServiceDetail;
    private ChattyServiceDetail chattyServiceDetail;
    private CyclicReferenceDetail cyclicReferenceDetail;
    private Set<List<DuplicatedServiceDetail>> duplicatedServiceDetail;
    private ESBUsageDetail esbUsageDetail;
    private GodServiceDetail godServiceDetail;
    private ServiceGreedyDetail greedyServiceDetail;
    private HardCodeDetail hardCodeDetail;
    private HubServiceDetail hubServiceDetail;
    private LocalLoggingDetail localLoggingDetail;
    private ApiVersionDetail noApiVersionDetail;
    private NoGatewayDetail noGatewayDetail;
    private NoHealthCheckAndNoServiceDiscoveryPatternDetail NoHealthCheckDetail;
    private NoHealthCheckAndNoServiceDiscoveryPatternDetail NoServiceDiscoveryPatternDetail;
    private ApiDesignDetail poorRestfulApiDesign;
    private ScatteredServiceDetail scatteredServiceDetail;
    private SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacyDetail;
    private SharedLibraryDetail sharedLibraryDetail;
    private UnnecessarySettingsDetail unnecessarySettingsDetail;
    private WhateverTypesDetail whateverTypesDetail;
    private WrongCutDetail wrongCutDetail;
}
