package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCutItem;
import com.smelldetection.entity.smell.detail.ESBUsageDetail;
import com.smelldetection.entity.smell.detail.GodServiceDetail;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class GodService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public GodServiceDetail getGodService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, DocumentException, XmlPullParserException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        // ServiceCutItem 包含微服务名称以及该微服务模块的实体数量
        List<ServiceCutItem> systemServiceCuts;
        if (redisTemplate.opsForValue().get(systemPath + "_systemServiceCuts") == null || "true".equals(changed)) {
            systemServiceCuts = FileUtils.getSystemServiceCuts(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_systemServiceCuts", systemServiceCuts);
        } else {
            systemServiceCuts = (List<ServiceCutItem>) redisTemplate.opsForValue().get(systemPath + "_systemServiceCuts");
        }
        double systemTotalEntityCount = 0;
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            systemTotalEntityCount += serviceCut.getEntityCount();
        }
        double systemAverageEntityCount = systemTotalEntityCount / systemServiceCuts.size();
        double sumOfSquares = 0;
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            sumOfSquares += Math.pow(serviceCut.getEntityCount() - systemAverageEntityCount, 2);
        }
        double std = Math.sqrt(sumOfSquares / systemServiceCuts.size());
        GodServiceDetail godServiceDetail = new GodServiceDetail();
        godServiceDetail.setTime(time);
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            if(Math.abs(serviceCut.getEntityCount() - systemAverageEntityCount) >= 3 * std
                    && (systemServiceCuts.size() != 1) && std != 0) {
                if (serviceCut.getEntityCount() > systemAverageEntityCount && serviceCut.getEntityCount() <= 2)
                    continue;
                godServiceDetail.getGodServices().put(serviceCut.getMicroserviceName(), serviceCut);
            }
        }
        if (!godServiceDetail.getGodServices().isEmpty()) {
            godServiceDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_godService_" + start, godServiceDetail);
        return godServiceDetail;
    }

    public List<GodServiceDetail> getGodServiceHistory(String systemPath) {
        String key = systemPath + "_godService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<GodServiceDetail> godServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                godServiceDetails.add((GodServiceDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return godServiceDetails;
    }
}
