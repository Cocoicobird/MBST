package com.smelldetection.service;

import com.smelldetection.entity.item.DependCount;
import com.smelldetection.entity.smell.detail.HubServiceDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class HubService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public HubServiceDetail getHubClass(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        Map<String, DependCount> importsAndOutputs = new HashMap<>();
        Set<String> classNames = new LinkedHashSet<>();
        // 收集本系统自身所有的类声明
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                JavaParserUtils.initDependCount(file, classNames, importsAndOutputs, microserviceName);
            }
        }
        // 解析 import
        for (String filePath : filePathToMicroserviceName.keySet()) {
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                JavaParserUtils.resolveImportAndOutput(file, importsAndOutputs, classNames);
            }
        }
        HubServiceDetail hubServiceDetail = new HubServiceDetail();
        for (String className : importsAndOutputs.keySet()) {
            DependCount dependCount = importsAndOutputs.get(className);
            int importCount = dependCount.getImportCount();
            int outputCount = dependCount.getOutputCount();
            if (importCount >= 10 && outputCount >= 10 && Math.max(importCount, outputCount) * 0.9 <= Math.min(importCount, outputCount)) {
                hubServiceDetail.put(dependCount.getMicroserviceName(), dependCount);
            }
        }
        if (!hubServiceDetail.getHubClasses().isEmpty()) {
            hubServiceDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_hubService_" + start, hubServiceDetail);
        return hubServiceDetail;
    }

    public List<HubServiceDetail> getHubServiceHistory(String systemPath) {
        String key = systemPath + "_hubService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<HubServiceDetail> hubServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                hubServiceDetails.add((HubServiceDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return hubServiceDetails;
    }
}
