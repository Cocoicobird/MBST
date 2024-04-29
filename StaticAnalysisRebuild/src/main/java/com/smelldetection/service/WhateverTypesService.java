package com.smelldetection.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.serialization.JavaParserJsonSerializer;
import com.smelldetection.entity.smell.detail.UnnecessarySettingsDetail;
import com.smelldetection.entity.smell.detail.WhateverTypesDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import javax.json.stream.JsonGenerator;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class WhateverTypesService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public WhateverTypesDetail getWhateverTypes(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        WhateverTypesDetail whateverTypesDetail = new WhateverTypesDetail();
        whateverTypesDetail.setTime(time);
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            Map<String, MethodDeclaration> returnTypes = new HashMap<>();
            for (String javaFile : javaFiles) {
                Map<String, MethodDeclaration> returnType = JavaParserUtils.getMethodDeclaration(javaFile);
                returnTypes.putAll(returnType);
            }
            // redisTemplate.opsForValue().set(microserviceName + "MethodDeclaration", returnTypes);
            // System.out.println(microserviceName + "---------------------");
            for (String method : returnTypes.keySet()) {
                MethodDeclaration methodDeclaration = returnTypes.get(method);
                if (methodDeclaration.getTypeParameters().size() > 0) {
                    whateverTypesDetail.put(microserviceName, method);
                }
            }
        }
        if (!whateverTypesDetail.getReturnTypes().isEmpty()) {
            whateverTypesDetail.setStatus(true);
        }
        redisTemplate.opsForValue().set(systemPath + "_whateverTypes_" + start, whateverTypesDetail);
        return whateverTypesDetail;
    }

    public List<WhateverTypesDetail> getWhateverTypesHistory(String systemPath) {
        String key = systemPath + "_whateverTypes_*";
        Set<String> keys = redisTemplate.keys(key);
        List<WhateverTypesDetail> whateverTypesDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                whateverTypesDetails.add((WhateverTypesDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return whateverTypesDetails;
    }
}
