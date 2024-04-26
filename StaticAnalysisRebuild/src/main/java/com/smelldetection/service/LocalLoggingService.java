package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.LocalLoggingDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.utils.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class LocalLoggingService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public LocalLoggingDetail getLocalLoggingService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, DocumentException {
        long start = System.currentTimeMillis();
        LocalLoggingDetail localLoggingDetail = new LocalLoggingDetail();
        Map<String, Configuration> filePathToConfiguration;
        if (redisTemplate.opsForValue().get(systemPath + "_filePathToConfiguration") == null || "true".equals(changed)) {
            filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_filePathToConfiguration", filePathToConfiguration);
        } else {
            filePathToConfiguration = (Map<String, Configuration>) redisTemplate.opsForValue().get(systemPath + "_filePathToConfiguration");
        }
        SAXReader saxReader = new SAXReader();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> logs = FileUtils.getLogs(filePath);
            boolean logstash = false;
            for (String log : logs) {
                System.out.println(log);
                Document document = saxReader.read(new File(log));
                Element rootElement = document.getRootElement();
                for (Element element : rootElement.elements()) {
                    if ("appender".equals(element.getName()) && "net.logstash.logback.appender.LogstashTcpSocketAppender".equals(element.attributeValue("class"))) {
                        List<Element> elements = element.elements();
                        for (Element e : elements) {
                            if ("destination".equals(e.getName())) {
                                logstash = true;
                                break;
                            }
                        }
                        if (logstash)
                            break;
                    }
                }
            }
            boolean status = false;
            for (String key : filePathToConfiguration.get(filePath).getItems().keySet()) {
                if (key.contains("log") || !logstash) {
                    status = true;
                    break;
                }
            }
            localLoggingDetail.put(microserviceName, status);
        }
        for (String microserviceName : localLoggingDetail.getLogs().keySet()) {
            if (localLoggingDetail.getLogs().get(microserviceName)) {
                localLoggingDetail.setStatus(true);
                break;
            }
        }
        redisTemplate.opsForValue().set(systemPath + "_localLogging_" + start, localLoggingDetail);
        return localLoggingDetail;
    }

    public List<LocalLoggingDetail> getLocalLoggingHistory(String systemPath) {
        String key = systemPath + "_localLogging_*";
        Set<String> keys = redisTemplate.keys(key);
        List<LocalLoggingDetail> localLoggingDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                localLoggingDetails.add((LocalLoggingDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return localLoggingDetails;
    }
}
