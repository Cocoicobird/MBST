package com.smelldetection.quality.detail;

import com.smelldetection.entity.smell.detail.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
@Component
public class StaticAnalysisDetail {
    public double qualityScore;
    public BloatedServiceDetail bloatedServiceDetail;
    public ChattyServiceDetail chattyServiceDetail;
    public CyclicReferenceDetail cyclicReferenceDetail;
    public Set<List<DuplicatedServiceDetail>> duplicatedServiceDetail;
    public ESBUsageDetail esbUsageDetail;
    public GodServiceDetail godServiceDetail;
    public ServiceGreedyDetail serviceGreedyDetail;
    public HardCodeDetail hardCodeDetail;
    public HubServiceDetail hubServiceDetail;
    public LocalLoggingDetail localLoggingDetail;
    public ApiVersionDetail noApiVersionDetail;
    public NoGatewayDetail noGatewayDetail;
    public NoHealthCheckAndNoServiceDiscoveryPatternDetail noHealthCheckDetail;
    public NoHealthCheckAndNoServiceDiscoveryPatternDetail noServiceDiscoveryPatternDetail;
    public ApiDesignDetail poorRestfulApiDesignDetail;
    public ScatteredServiceDetail scatteredServiceDetail;
    public Set<String> sharedDatabasesDetail;
    public Set<String> serviceIntimacyDetail;
}
