package com.smelldetection.service;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.smelldetection.entity.smell.detail.CyclicReferenceDetail;
import com.smelldetection.entity.smell.detail.DuplicatedServiceDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.NlpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 多个服务具有相同或相似功能
 * 实现思路：控制层和业务层方法针对方法名、参数、返回值类型进行相似度判断
 */
@Service
public class DuplicatedServicesService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Map<String, Set<List<DuplicatedServiceDetail>>> getDuplicatedService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        long start = System.currentTimeMillis();
        // Map<String, Map<String, MethodDeclaration>> filePathToControllerMethod = new HashMap<>();
        Map<String, Map<String, MethodDeclaration>> filePathToServiceImplMethod = new HashMap<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            // filePathToControllerMethod.put(microserviceName, new HashMap<>());
            filePathToServiceImplMethod.put(microserviceName, new HashMap<>());
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            // 主要收集控制层和业务层进行判断
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
//                if (JavaParserUtils.isControllerClass(file)) {
//                    Map<String, MethodDeclaration> methodDeclarations = JavaParserUtils.getMethodDeclaration(javaFile);
//                    for (String methodName : methodDeclarations.keySet()) {
//                        filePathToControllerMethod.get(microserviceName).put(javaFile + "-" + methodName, methodDeclarations.get(methodName));
//                    }
//                }
                if ("ServiceImpl".equals(JavaParserUtils.isServiceImplementationClass(file))) {
                    Map<String, MethodDeclaration> methodDeclarations = JavaParserUtils.getMethodDeclaration(javaFile);
                    for (String methodName : methodDeclarations.keySet()) {
                        filePathToServiceImplMethod.get(microserviceName).put(javaFile + "-" + methodName, methodDeclarations.get(methodName));
                    }
                }
            }
        }
        List<String> microserviceNames = new ArrayList<>(filePathToMicroserviceName.values());
        Set<List<DuplicatedServiceDetail>> serviceImpls = new LinkedHashSet<>();
        // List<List<DuplicatedServiceDetail>> controllers = new ArrayList<>();
        for (int i = 0; i < microserviceNames.size(); i++) {
            // 微服务模块 1 中的所有业务层方法声明
            String microserviceName1 = microserviceNames.get(i);
            Map<String, MethodDeclaration> serviceImplMethodDeclarations1 = filePathToServiceImplMethod.get(microserviceName1);
            for (String methodName1 : serviceImplMethodDeclarations1.keySet()) {
                MethodDeclaration methodDeclaration1 = serviceImplMethodDeclarations1.get(methodName1);
                DuplicatedServiceDetail duplicatedServiceDetail1 = new DuplicatedServiceDetail();
                duplicatedServiceDetail1.setMicroserviceName(microserviceName1);
                duplicatedServiceDetail1.setMethodDeclaration(methodDeclaration1.getDeclarationAsString(false, false, true));
                List<DuplicatedServiceDetail> duplicatedServiceDetails = new ArrayList<>();
                duplicatedServiceDetails.add(duplicatedServiceDetail1);
                for (int j = i + 1; j < microserviceNames.size(); j++) {
                    String microserviceName2 = microserviceNames.get(j);
                    // 微服务模块 2 中的所有业务层方法声明
                    Map<String, MethodDeclaration> serviceImplMethodDeclarations2 = filePathToServiceImplMethod.get(microserviceName2);
                    for (String methodName2 : serviceImplMethodDeclarations2.keySet()) {
                        MethodDeclaration methodDeclaration2 = serviceImplMethodDeclarations2.get(methodName2);
                        // System.out.println(convert(methodDeclaration1.getNameAsString()) + " " + convert(methodDeclaration2.getNameAsString()));
                        double value1 = NlpUtils.getSimilarity(convert(methodDeclaration1.getNameAsString()), convert(methodDeclaration2.getNameAsString()));
                        double value2 = NlpUtils.getSimilarity(methodDeclaration1.getTypeAsString(), methodDeclaration2.getTypeAsString());
                        double value3 = NlpUtils.getSimilarity(methodDeclaration1.getParameters().toString(), methodDeclaration2.getParameters().toString());
                        if (value1 >= 0.85 && value2 >= 0.85 && value3 >= 0.85) {
                            DuplicatedServiceDetail duplicatedServiceDetail2 = new DuplicatedServiceDetail();
                            duplicatedServiceDetail2.setMicroserviceName(microserviceName2);
                            duplicatedServiceDetail2.setMethodDeclaration(methodDeclaration2.getDeclarationAsString(false, false, true));
                            duplicatedServiceDetails.add(duplicatedServiceDetail2);
                        }
                    }
                }
                if (duplicatedServiceDetails.size() > 1) {
                    serviceImpls.add(duplicatedServiceDetails);
                }
            }
            /*
            // 微服务模块 1 中的控制层方法声明
            Map<String, MethodDeclaration> controllerMethodDeclarations1 = filePathToControllerMethod.get(microserviceName1);
            for (int j = i + 1; j < microserviceNames.size(); j++) {
                String microserviceName2 = microserviceNames.get(j);
                // 微服务模块 2 中的控制层方法声明
                Map<String, MethodDeclaration> controllerMethodDeclarations2 = filePathToControllerMethod.get(microserviceName2);
                List<DuplicatedServiceDetail> duplicatedServiceDetails = new ArrayList<>();
                for (String methodName1 : controllerMethodDeclarations1.keySet()) {
                    MethodDeclaration methodDeclaration1 = controllerMethodDeclarations1.get(methodName1);
                    DuplicatedServiceDetail duplicatedServiceDetail1 = new DuplicatedServiceDetail();
                    duplicatedServiceDetail1.setMicroserviceName(microserviceName1);
                    duplicatedServiceDetail1.setMethodDeclaration(methodDeclaration1.getDeclarationAsString(false, false, true));
                    duplicatedServiceDetails.add(duplicatedServiceDetail1);
                    for (String methodName2 : controllerMethodDeclarations2.keySet()) {
                        MethodDeclaration methodDeclaration2 = controllerMethodDeclarations2.get(methodName2);
                        // System.out.println(convert(methodDeclaration1.getNameAsString()) + " " + convert(methodDeclaration2.getNameAsString()));
                        double value1 = NlpUtils.getSimilarity(convert(methodDeclaration1.getNameAsString()), convert(methodDeclaration2.getNameAsString()));
                        double value2 = NlpUtils.getSimilarity(methodDeclaration1.getTypeAsString(), methodDeclaration2.getTypeAsString());
                        double value3 = NlpUtils.getSimilarity(methodDeclaration1.getParameters().toString(), methodDeclaration2.getParameters().toString());
                        if (value1 >= 0.85 && value2 >= 0.85 && value3 >= 0.85) {
                            DuplicatedServiceDetail duplicatedServiceDetail2 = new DuplicatedServiceDetail();
                            duplicatedServiceDetail2.setMicroserviceName(microserviceName2);
                            duplicatedServiceDetail2.setMethodDeclaration(methodDeclaration2.getDeclarationAsString(false, false, true));
                            duplicatedServiceDetails.add(duplicatedServiceDetail2);
                        }
                    }
                    if (duplicatedServiceDetails.size() > 1) {
                        controllers.add(duplicatedServiceDetails);
                    }
                }
            }
             */
        }
        List<List<DuplicatedServiceDetail>> temp = new ArrayList<>(serviceImpls);
        serviceImpls.clear();
        for (int i = 0; i < temp.size(); i++) {
            List<DuplicatedServiceDetail> duplicatedServiceDetails = new ArrayList<>(temp.get(i));
            if (!check(serviceImpls, duplicatedServiceDetails))
                serviceImpls.add(duplicatedServiceDetails);
        }
        Map<String, Set<List<DuplicatedServiceDetail>>> results = new HashMap<>();
        results.put("serviceImpl", serviceImpls);
        // results.put("controllers", controllers);
        redisTemplate.opsForValue().set(systemPath + "_duplicatedService_" + start, results);
        return results;
    }

    private boolean check(Set<List<DuplicatedServiceDetail>> serviceImpls, List<DuplicatedServiceDetail> duplicatedServiceDetails) {
        for (List<DuplicatedServiceDetail> serviceDetails : serviceImpls)
            if (serviceDetails.containsAll(duplicatedServiceDetails))
                return true;
        return false;
    }

    private String convert(String methodName) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < methodName.length(); i++) {
            if (Character.isUpperCase(methodName.charAt(i))) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(Character.toLowerCase(methodName.charAt(i)));
        }
        return stringBuilder.toString();
    }

    public List<Map<String, Set<List<DuplicatedServiceDetail>>>> getDuplicatedServiceHistory(String systemPath) {
        String key = systemPath + "_duplicatedService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<Map<String, Set<List<DuplicatedServiceDetail>>>> duplicatedServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                duplicatedServiceDetails.add((Map<String, Set<List<DuplicatedServiceDetail>>>) redisTemplate.opsForValue().get(k));
            }
        }
        return duplicatedServiceDetails;
    }
}
