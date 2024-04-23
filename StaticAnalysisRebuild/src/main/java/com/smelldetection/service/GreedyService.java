package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceGreedyItem;
import com.smelldetection.entity.smell.detail.HardCodeDetail;
import com.smelldetection.entity.smell.detail.ServiceGreedyDetail;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
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
 * @description 服务贪婪
 */
@Service
public class GreedyService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WrongCutService wrongCutService;

    public ServiceGreedyDetail getGreedyService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, DocumentException, XmlPullParserException {
        long start = System.currentTimeMillis();
        ServiceGreedyDetail serviceGreedyDetail = new ServiceGreedyDetail();
        WrongCutDetail wrongCutDetail = wrongCutService.getWrongCut(filePathToMicroserviceName, systemPath, changed);
        for (String directory : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(directory);
            List<String> staticFiles = FileUtils.getStaticFiles(directory);
            if (staticFiles != null && !staticFiles.isEmpty() && staticFiles.size() <= 2) {
                serviceGreedyDetail.addGreedyService(new ServiceGreedyItem(microserviceName, staticFiles));
            } else if (staticFiles != null && staticFiles.isEmpty()
                    && wrongCutDetail.getWrongCutMicroservices().containsKey(microserviceName)
                    && wrongCutDetail.getWrongCutMicroservices().get(microserviceName).getEntityCount() < 1) {
                serviceGreedyDetail.addGreedyService(new ServiceGreedyItem(microserviceName, staticFiles));
            }
        }
        if (!serviceGreedyDetail.getGreedyServices().isEmpty()) {
            serviceGreedyDetail.setExisted(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_greedyService_" + start, serviceGreedyDetail);
        return serviceGreedyDetail;
    }

    public List<ServiceGreedyDetail> getGreedyServiceHistory(String systemPath) {
        String key = systemPath + "_greedyService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ServiceGreedyDetail> serviceGreedyDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                serviceGreedyDetails.add((ServiceGreedyDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return serviceGreedyDetails;
    }
}
