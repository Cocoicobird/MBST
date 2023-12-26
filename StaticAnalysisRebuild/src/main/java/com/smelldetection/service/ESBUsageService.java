package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCallItem;
import com.smelldetection.entity.smell.detail.ESBUsageDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class ESBUsageService {

    public ESBUsageDetail getESBUsageServices(Map<String, String> filePathToMicroserviceName) {
        ESBUsageDetail esbUsageDetail = new ESBUsageDetail();
        Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
        if (microserviceCallResults != null) {
            Map<String, ServiceCallItem> microserviceCallResultsAfterESBUsageAnalysis = ServiceCallParserUtils.isESBUsageExist(microserviceCallResults);
            esbUsageDetail.setServiceCallItems(microserviceCallResultsAfterESBUsageAnalysis);
            for (String microserviceName : microserviceCallResultsAfterESBUsageAnalysis.keySet()) {
                if (microserviceCallResultsAfterESBUsageAnalysis.get(microserviceName).isESBUsage()) {
                    esbUsageDetail.setStatus(true);
                    break;
                }
            }
        }
        return esbUsageDetail;
    }

}
