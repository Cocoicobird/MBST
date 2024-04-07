package com.smelldetection.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.serialization.JavaParserJsonSerializer;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class WhateverTypesService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<WhateverTypesDetail> getWhateverTypes(Map<String, String> filePathToMicroserviceName) throws IOException {
        List<WhateverTypesDetail> whateverTypesDetails = new ArrayList<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            WhateverTypesDetail whateverTypesDetail = new WhateverTypesDetail();
            String microserviceName = filePathToMicroserviceName.get(filePath);
            whateverTypesDetail.setMicroserviceName(microserviceName);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            Map<String, MethodDeclaration> returnTypes = new HashMap<>();
            for (String javaFile : javaFiles) {
                Map<String, MethodDeclaration> returnType = JavaParserUtils.getMethodDeclaration(javaFile);
                System.out.println(JavaParserUtils.getPackageName(new File(javaFile)));
                System.out.println(JavaParserUtils.getImports(new File(javaFile)));
                returnTypes.putAll(returnType);
            }
            // redisTemplate.opsForValue().set(microserviceName + "MethodDeclaration", returnTypes);
            // System.out.println(microserviceName + "---------------------");
            for (String method : returnTypes.keySet()) {
                MethodDeclaration methodDeclaration = returnTypes.get(method);
                if (methodDeclaration.getTypeParameters().size() > 0) {
                    whateverTypesDetail.put(method, methodDeclaration.getType().asString());
                }
            }
            if (!whateverTypesDetail.isEmpty()) {
                whateverTypesDetails.add(whateverTypesDetail);
            }
        }
        return whateverTypesDetails;
    }
}
