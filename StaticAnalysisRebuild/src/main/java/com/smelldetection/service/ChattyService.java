package com.smelldetection.service;

import com.smelldetection.entity.item.ChattyServiceItem;
import com.smelldetection.entity.smell.detail.BloatedServiceDetail;
import com.smelldetection.entity.smell.detail.ChattyServiceDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 频繁调用其它服务
 * 根据当前服务请求其他服务的最大请求次数判定
 * 请求其他服务的最大请求次数 > 5 && 当前服务的实体类数量或 dto 类数量 >= 1，频繁调用是为了获取数据
 */
@Service
public class ChattyService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ChattyServiceDetail getChattyService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        ChattyServiceDetail chattyServiceDetail = new ChattyServiceDetail();
        // 微服务调用结果，每个微服务调用了哪些微服务以及次数
        Map<String, Map<String, Integer>> microserviceCallResults;
        if (redisTemplate.opsForValue().get(systemPath + "_" + "microserviceCallResults") == null || "true".equals(changed)) {
            microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_" + "microserviceCallResults", microserviceCallResults);
        } else {
            microserviceCallResults = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(systemPath + "_microserviceCallResults");
        }
        for (String filePath: filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            System.out.println(microserviceName);
            int maxCallNumber = 0;
            assert microserviceCallResults != null;
            Map<String, Integer> microserviceCallResult = microserviceCallResults.get(microserviceName);
            if (microserviceCallResult.size() == 0)
                maxCallNumber = -1;
            for (String calledMicroserviceName : microserviceCallResult.keySet()) {
                maxCallNumber = Math.max(maxCallNumber, microserviceCallResult.get(calledMicroserviceName));
            }
            // entity 相关目录下的 .java 文件，一般是实体类，有些项目中也会将 DTO 置于该目录
            List<String> entityClasses;
            if (redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_entityClasses") == null || "true".equals(changed)) {
                entityClasses = FileUtils.getJavaFilesUnderEntity(filePath);
                redisTemplate.opsForValue().set(systemPath + "_" + microserviceName + "_entityClasses", entityClasses);
            } else {
                entityClasses = (List<String>) redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_entityClasses");
            }
            boolean status = maxCallNumber > 5 && entityClasses.size() >= 1;
            if (status) {
                ChattyServiceItem chattyServiceItem = new ChattyServiceItem();
                chattyServiceItem.setMaxCallNumber(maxCallNumber);
                chattyServiceItem.setEntityNumber(entityClasses.size());
                chattyServiceDetail.put(microserviceName, chattyServiceItem);
            }
        }
        if (!chattyServiceDetail.getChattyServices().isEmpty()) {
            chattyServiceDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_chattyService_" + start, chattyServiceDetail);
        return chattyServiceDetail;
    }

    public List<ChattyServiceDetail> getChattyServiceHistory(String systemPath) {
        String key = systemPath + "_chattyService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ChattyServiceDetail> chattyServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                chattyServiceDetails.add((ChattyServiceDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return chattyServiceDetails;
    }
}
