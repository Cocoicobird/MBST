package com.smelldetection.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.smelldetection.entity.item.ServiceCallItem;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void getBloatedService(Map<String, String> filePathToMicroserviceName) throws IOException {
        // 微服务调用结果，每个微服务调用了哪些微服务以及次数
        Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
        for (String filePath : filePathToMicroserviceName.keySet()) {
            System.out.println(filePath);
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            // 该微服务模块所有方法声明
            Map<String, MethodDeclaration> methodDeclarations = new HashMap<>();
            // 该微服务模块的总代码行数
            int codeSize = 0;
            // 该微服务模块中控制器层中所有业务层调用情况
            Map<String, Map<String, Integer>> serviceMethodCallOfControllers = new HashMap<>();
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
            System.out.println(maxParameterNumber + " " + microserviceCall.size() + "/" + filePathToMicroserviceName.size() + " " + totalServiceMethodCallNumber + " " + codeSize);
            boolean status = maxParameterNumber >= 6 && (percent >= 0.2 || totalServiceMethodCallNumber > 10 || codeSize > 5000);
            System.out.println(status);
        }
    }
}
