package com.smelldetection.service;

import com.smelldetection.entity.item.HardCodeItem;
import com.smelldetection.entity.smell.detail.HardCodeDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.HardCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 硬编码
 */
@Service
public class HardCodeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public HardCodeDetail getHardCode(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        HardCodeDetail hardCodeDetail = new HardCodeDetail();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            List<HardCodeItem> hardCodeItems = new ArrayList<>();
            HardCodeUtils.resolveHardCodeFromJavaFiles(javaFiles, hardCodeItems);
            if (!hardCodeItems.isEmpty()) {
                hardCodeDetail.addAll(microserviceName, hardCodeItems);
            }
        }
        if (!hardCodeDetail.getHardCodes().isEmpty()) {
            hardCodeDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_hardCode_" + start, hardCodeDetail);
        return hardCodeDetail;
    }

    public List<HardCodeDetail> getHardCodeHistory(String systemPath) {
        String key = systemPath + "_hardCode_*";
        Set<String> keys = redisTemplate.keys(key);
        List<HardCodeDetail> hardCodeDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                hardCodeDetails.add((HardCodeDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return hardCodeDetails;
    }
}
