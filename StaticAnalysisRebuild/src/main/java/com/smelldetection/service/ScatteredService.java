package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ApiDesignDetail;
import com.smelldetection.entity.smell.detail.ScatteredServiceDetail;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 松散服务
 */
@Service
public class ScatteredService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ScatteredServiceDetail getScatteredFunctionalityServices(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) {
        long start = System.currentTimeMillis();
        ScatteredServiceDetail scatteredServiceDetail = new ScatteredServiceDetail();
        Map<String, Map<String,Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
        if (microserviceCallResults != null) {
            ServiceCallParserUtils.checkCallResult(microserviceCallResults);
            int threshold = 3;
            for (String microserviceName : microserviceCallResults.keySet()) {
                // 当前微服务的调用情况
                Map<String, Integer> serviceCallItem = microserviceCallResults.get(microserviceName);
                for (String calledMicroserviceName : serviceCallItem.keySet()) {
                    // microserviceName 是 A
                    // serviceCallItem 里有 B C D
                    // 假设初始 scatteredServiceDetail 里为空
                    // 那么先存 A B
                    if (serviceCallItem.get(calledMicroserviceName) > threshold) { // 调用次数超过阈值，这两个微服务是分散的
                        boolean isExist = false;
                        // 遍历松散服务集合的列表
                        for (Set<String> scatteredService : scatteredServiceDetail.getScatteredServices()) {
                            if (scatteredService.contains(microserviceName) && !scatteredService.contains(calledMicroserviceName)) {
                                scatteredService.add(calledMicroserviceName);
                                isExist = true;
                                break;
                            } else if (!scatteredService.contains(microserviceName) && scatteredService.contains(calledMicroserviceName)) {
                                scatteredService.add(microserviceName);
                                isExist = true;
                                break;
                            } else if (scatteredService.contains(microserviceName) && scatteredService.contains(calledMicroserviceName)) {
                                isExist = true;
                                break;
                            }
                        }
                        // two services do not exist in result list
                        if (!isExist) {
                            Set<String> scatteredSet = new HashSet<>();
                            scatteredSet.add(microserviceName);
                            scatteredSet.add(calledMicroserviceName);
                            scatteredServiceDetail.getScatteredServices().add(scatteredSet);
                        }

                    }
                }
            }
        }
        // Remove duplicates, gather functionality scattered services
        // 去重
        ScatteredServiceDetail ssd = new ScatteredServiceDetail();
        for (Set<String> set: scatteredServiceDetail.getScatteredServices()) {
            boolean isExist = false;
            if (ssd.getScatteredServices().size() == 0) {
                ssd.getScatteredServices().add(new HashSet<>(set));
                continue;
            }
            for (Set<String> set1 : ssd.getScatteredServices()) {
                for (String microservice : set) {
                    if (set1.contains(microservice)) {
                        set1.addAll(set);
                        isExist = true;
                        break;
                    }
                }
                if (isExist)
                    break;
            }
            if (!isExist)
                ssd.getScatteredServices().add(new HashSet<>(set));

        }
        if (!ssd.getScatteredServices().isEmpty())
            ssd.setStatus(true);
        redisTemplate.opsForValue().set(systemPath + "_scatteredFunctionalityService_" + start, ssd);
        return ssd;
    }

    public List<ScatteredServiceDetail> getScatteredFunctionalityServiceHistory(String systemPath) {
        String key = systemPath + "_scatteredFunctionalityService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ScatteredServiceDetail> scatteredServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                scatteredServiceDetails.add((ScatteredServiceDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return scatteredServiceDetails;
    }
}
