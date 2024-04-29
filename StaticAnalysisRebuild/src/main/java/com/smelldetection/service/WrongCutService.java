package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCutItem;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
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
public class WrongCutService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取系统错误划分的服务
     * @param filePathToMicroserviceName 微服务模块路径与微服务名的映射
     */
    public WrongCutDetail getWrongCut(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, DocumentException, XmlPullParserException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
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
        WrongCutDetail wrongCutDetail = new WrongCutDetail();
        wrongCutDetail.setTime(time);
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            if (Math.abs(serviceCut.getEntityCount() - systemAverageEntityCount) >= 3 * std
                    && (filePathToMicroserviceName.size() != 1) && std != 0) {
                if (serviceCut.getEntityCount() > systemAverageEntityCount && serviceCut.getEntityCount() <= 2)
                    continue;
                wrongCutDetail.getWrongCutMicroservices().put(serviceCut.getMicroserviceName(), serviceCut);
            }
        }
        if (!wrongCutDetail.getWrongCutMicroservices().isEmpty()) {
            wrongCutDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_wrongCut_" + start, wrongCutDetail);
        return wrongCutDetail;
    }

    public List<WrongCutDetail> getWrongCutHistory(String systemPath) {
        String key = systemPath + "_wrongCut_*";
        Set<String> keys = redisTemplate.keys(key);
        List<WrongCutDetail> wrongCutDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                wrongCutDetails.add((WrongCutDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return wrongCutDetails;
    }
}
