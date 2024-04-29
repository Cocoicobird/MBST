package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.entity.smell.detail.LocalLoggingDetail;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class NoApiVersionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public ApiVersionDetail getNoApiVersion(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        ApiVersionDetail apiVersionDetail = new ApiVersionDetail();
        apiVersionDetail.setTime(time);
        Map<String, Map<String, String>> urls = new HashMap<>();
        for (String directory : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(directory);
            apiVersionDetail.getNoVersion().put(microserviceName, new HashMap<>());
            apiVersionDetail.getMissingUrl().put(microserviceName, new HashMap<>());
            List<String> javaFiles = FileUtils.getJavaFiles(directory);
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                JavaParserUtils.resolveApiFromJavaFile(file, apiVersionDetail, microserviceName, urls);
            }
        }
        redisTemplate.opsForValue().set(systemPath + "_urls", urls);
        for(String microserviceName: apiVersionDetail.getNoVersion().keySet()) {
            if(!apiVersionDetail.getNoVersion().get(microserviceName).isEmpty()){
                apiVersionDetail.setStatus(true);
                break;
            }
        }
        // redisTemplate.opsForValue().set(systemPath + "_urls", urls);
        redisTemplate.opsForValue().set(systemPath + "_noApiVersion_" + start, apiVersionDetail);
        return apiVersionDetail;
    }

    public List<ApiVersionDetail> getNoApiVersionHistory(String systemPath) {
        String key = systemPath + "_noApiVersion_*";
        Set<String> keys = redisTemplate.keys(key);
        List<ApiVersionDetail> apiVersionDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                apiVersionDetails.add((ApiVersionDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return apiVersionDetails;
    }
}
