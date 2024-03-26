package com.smelldetection.entity.smell.summary;

import com.smelldetection.entity.smell.detail.*;
import lombok.Data;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 微服务异味静态分析结果
 */
@Data
public class StaticAnalysisSummary {
    private ApiVersionDetail apiVersionDetail;
    private CyclicReferenceDetail cyclicReferenceDetail;
    private ESBUsageDetail esbUsageDetail;
    private NoGatewayDetail noGatewayDetail;
    private GodServiceDetail godServiceDetail;
    private HardCodeDetail hardCodeDetail;
    private ScatteredServiceDetail scatteredServiceDetail;
    private ServiceGreedyDetail serviceGreedyDetail;
    private SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacyDetail;
    private SharedLibraryDetail sharedLibraryDetail;
    private TooManyStandardsDetail tooManyStandardsDetail;
    private WrongCutDetail wrongCutDetail;
}
