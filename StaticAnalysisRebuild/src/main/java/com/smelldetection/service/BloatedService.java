package com.smelldetection.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.smelldetection.entity.item.BloatedServiceItem;
import com.smelldetection.entity.smell.detail.BloatedServiceDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 有一个大的接口和许多参数，其中执行低内聚的异构操作。
 * 如果最大 api 参数量 >= 6 &&（传出数量占比（调用的服务数量/总的服务数量） >= 20% || ServiceImpl 类中调用服务内部的类的方法的总次数 > 10 || codeSize > 5000）
 * 则判定存在 BloatedService
 */
@Service
public class BloatedService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public BloatedServiceDetail getBloatedService(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException {
        // 微服务调用结果，每个微服务调用了哪些微服务以及次数
        long start = System.currentTimeMillis();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateformat.format(start);
        BloatedServiceDetail bloatedServiceDetail = new BloatedServiceDetail();
        bloatedServiceDetail.setTime(time);
        Map<String, Map<String, Integer>> microserviceCallResults;
        if (redisTemplate.opsForValue().get(systemPath + "_" + "microserviceCallResults") == null || "true".equals(changed)) {
            microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_" + "microserviceCallResults", microserviceCallResults);
        } else {
            microserviceCallResults = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(systemPath + "_microserviceCallResults");
        }
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            // 该微服务模块所有方法声明
            Map<String, MethodDeclaration> methodDeclarations = new HashMap<>();
            // 该微服务模块的总代码行数
            int codeSize = 0;
            // 该微服务模块中控制器层中所有业务层调用情况
            Map<String, Map<String, Integer>> serviceMethodCallOfControllers;
            if (redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_serviceMethodCallOfControllers") == null || "true".equals(changed)) {
                serviceMethodCallOfControllers = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
                redisTemplate.opsForValue().set(systemPath + "_" + microserviceName + "_serviceMethodCallOfControllers", serviceMethodCallOfControllers);
            } else {
                serviceMethodCallOfControllers = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_serviceMethodCallOfControllers");
            }
            for (String javaFile : javaFiles) {
                methodDeclarations.putAll(JavaParserUtils.getMethodDeclaration(javaFile));
                codeSize = codeSize + FileUtils.getJavaFileLinesOfCode(new File(javaFile));
                if (JavaParserUtils.isControllerClass(new File(javaFile))) {
                    CompilationUnit compilationUnit = StaticJavaParser.parse(new File(javaFile));
                    // <对象名:<方法名:次数>>
                    Map<String, Map<String, Integer>> serviceMethodCallOfOneController = JavaParserUtils.getServiceMethodCallOfController(compilationUnit);
                    for (String serviceObject : serviceMethodCallOfOneController.keySet()) {
                        Map<String, Integer> result = serviceMethodCallOfOneController.get(serviceObject);
                        // 该对象未统计过
                        if (!serviceMethodCallOfControllers.containsKey(serviceObject)) {
                            serviceMethodCallOfControllers.put(serviceObject, new HashMap<>());
                        }
                        for (String methodName : result.keySet()) {
                            // 该对象的方法未统计过
                            if (!serviceMethodCallOfControllers.get(serviceObject).containsKey(methodName)) {
                                serviceMethodCallOfControllers.get(serviceObject).put(methodName, 0);
                            }
                            int count = serviceMethodCallOfControllers.get(serviceObject).get(methodName);
                            serviceMethodCallOfControllers.get(serviceObject).put(methodName, count + result.get(methodName));
                        }
                    }
                }
            }
            // Controller 调用 Service 方法总次数
            int totalServiceMethodCallNumber = 0;
            for (String serviceObject : serviceMethodCallOfControllers.keySet()) {
                for (String methodName : serviceMethodCallOfControllers.get(serviceObject).keySet()) {
                    totalServiceMethodCallNumber = totalServiceMethodCallNumber + serviceMethodCallOfControllers.get(serviceObject).get(methodName);
                }
            }
            // 获取最大参数数量
            int maxParameterNumber = 0;
            for (String methodName : methodDeclarations.keySet()) {
                MethodDeclaration methodDeclaration = methodDeclarations.get(methodName);
                maxParameterNumber = Math.max(maxParameterNumber, methodDeclaration.getParameters().size());
            }
            assert microserviceCallResults != null;
            Map<String, Integer> microserviceCall = microserviceCallResults.get(microserviceName);
            double percent = (double) microserviceCall.size() / filePathToMicroserviceName.size();
            boolean status = maxParameterNumber >= 6 && (percent >= 0.2 || totalServiceMethodCallNumber > 10 || codeSize > 5000);
            if (status) {
                BloatedServiceItem bloatedServiceItem = new BloatedServiceItem();
                bloatedServiceItem.setMaxParameterNumber(maxParameterNumber);
                bloatedServiceItem.setMicroserviceCallPercent(percent);
                bloatedServiceItem.setTotalServiceImplMethodCallCount(totalServiceMethodCallNumber);
                bloatedServiceItem.setCodeSize(codeSize);
                bloatedServiceDetail.put(microserviceName, bloatedServiceItem);
            }
        }
        if (!bloatedServiceDetail.getBloatedServices().isEmpty())
            bloatedServiceDetail.setStatus(true);
        redisTemplate.opsForValue().set(systemPath + "_bloatedService_" + start, bloatedServiceDetail);
        return bloatedServiceDetail;
    }

    public List<BloatedServiceDetail> getBloatedServiceHistory(String systemPath) {
        String key = systemPath + "_bloatedService_*";
        Set<String> keys = redisTemplate.keys(key);
        List<BloatedServiceDetail> bloatedServiceDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                bloatedServiceDetails.add((BloatedServiceDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return bloatedServiceDetails;
    }
}
