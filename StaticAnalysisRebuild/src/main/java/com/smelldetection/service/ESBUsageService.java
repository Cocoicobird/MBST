package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCallItem;
import com.smelldetection.entity.smell.detail.DuplicatedServiceDetail;
import com.smelldetection.entity.smell.detail.ESBUsageDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class ESBUsageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ESBUsageDetail getESBUsageServices(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        ESBUsageDetail esbUsageDetail = new ESBUsageDetail();
        esbUsageDetail.setTime(time);
        Map<String, Map<String, Integer>> microserviceCallResults;
        if (redisTemplate.opsForValue().get(systemPath + "_" + "microserviceCallResults") == null || "true".equals(changed)) {
            microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_" + "microserviceCallResults", microserviceCallResults);
        } else {
            microserviceCallResults = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(systemPath + "_microserviceCallResults");
        }
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
        redisTemplate.opsForValue().set(systemPath + "_esbUsage_" + start, esbUsageDetail);
        return esbUsageDetail;
    }

    public List<ESBUsageDetail> getESBUsageHistory(String systemPath) {
        String key = systemPath + "_esbUsage_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ESBUsageDetail> esbUsageDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                esbUsageDetails.add((ESBUsageDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return esbUsageDetails;
    }
}
