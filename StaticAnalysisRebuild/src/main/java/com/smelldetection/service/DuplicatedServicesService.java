package com.smelldetection.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.hankcs.hanlp.HanLP;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.NlpUtils;
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

    public void getDuplicatedService(Map<String, String> filePathToMicroserviceName) throws IOException {
        Map<String, Map<String, MethodDeclaration>> filePathToControllerMethod = new HashMap<>();
        Map<String, Map<String, MethodDeclaration>> filePathToServiceImplMethod = new HashMap<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            filePathToControllerMethod.put(microserviceName, new HashMap<>());
            filePathToServiceImplMethod.put(microserviceName, new HashMap<>());
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            // 主要收集控制层和业务层进行判断
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                if (JavaParserUtils.isControllerClass(file)) {
                    Map<String, MethodDeclaration> methodDeclarations = JavaParserUtils.getMethodDeclaration(javaFile);
                    for (String methodName : methodDeclarations.keySet()) {
                        filePathToControllerMethod.get(microserviceName).put(javaFile + "-" + methodName, methodDeclarations.get(methodName));
                    }
                }
                if ("ServiceImpl".equals(JavaParserUtils.isServiceImplementationClass(file))) {
                    Map<String, MethodDeclaration> methodDeclarations = JavaParserUtils.getMethodDeclaration(javaFile);
                    for (String methodName : methodDeclarations.keySet()) {
                        filePathToServiceImplMethod.get(microserviceName).put(javaFile + "-" + methodName, methodDeclarations.get(methodName));
                    }
                }
            }
        }
        List<String> microserviceNames = new ArrayList<>(filePathToMicroserviceName.values());
        for (int i = 0; i < microserviceNames.size(); i++) {
            // 微服务模块 1 中的业务层方法声明
            String microserviceName1 = microserviceNames.get(i);
            Map<String, MethodDeclaration> serviceImplMethodDeclarations1 = filePathToServiceImplMethod.get(microserviceName1);
            for (int j = i + 1; j < microserviceNames.size(); j++) {
                String microserviceName2 = microserviceNames.get(j);
                // 微服务模块 2 中的业务层方法声明
                Map<String, MethodDeclaration> serviceImplmethodDeclarations2 = filePathToServiceImplMethod.get(microserviceName2);
                for (String methodName1 : serviceImplMethodDeclarations1.keySet()) {
                    MethodDeclaration methodDeclaration1 = serviceImplMethodDeclarations1.get(methodName1);
                    for (String methodName2 : serviceImplmethodDeclarations2.keySet()) {
                        MethodDeclaration methodDeclaration2 = serviceImplmethodDeclarations2.get(methodName2);
                        System.out.println(convert(methodDeclaration1.getNameAsString()) + " " + convert(methodDeclaration2.getNameAsString()));
                        System.out.println(NlpUtils.getSimilarity(convert(methodDeclaration1.getNameAsString()), convert(methodDeclaration2.getNameAsString())));
                        System.out.println(NlpUtils.getSimilarity(methodDeclaration1.getTypeAsString(), methodDeclaration2.getTypeAsString()));
                        System.out.println(NlpUtils.getSimilarity(methodDeclaration1.getParameters().toString(), methodDeclaration2.getParameters().toString()));
                    }
                }
            }
            // 微服务模块 1 中的控制层方法声明
            Map<String, MethodDeclaration> controllerMethodDeclarations1 = filePathToControllerMethod.get(microserviceName1);
            for (int j = i + 1; j < microserviceNames.size(); j++) {
                String microserviceName2 = microserviceNames.get(j);
                // 微服务模块 2 中的控制层方法声明
                Map<String, MethodDeclaration> controllerMethodDeclarations2 = filePathToControllerMethod.get(microserviceName2);
                for (String methodName1 : controllerMethodDeclarations1.keySet()) {
                    MethodDeclaration methodDeclaration1 = controllerMethodDeclarations1.get(methodName1);
                    for (String methodName2 : controllerMethodDeclarations2.keySet()) {
                        MethodDeclaration methodDeclaration2 = controllerMethodDeclarations2.get(methodName2);
                        System.out.println(convert(methodDeclaration1.getNameAsString()) + " " + convert(methodDeclaration2.getNameAsString()));
                        System.out.println(NlpUtils.getSimilarity(convert(methodDeclaration1.getNameAsString()), convert(methodDeclaration2.getNameAsString())));
                        System.out.println(NlpUtils.getSimilarity(methodDeclaration1.getTypeAsString(), methodDeclaration2.getTypeAsString()));
                        System.out.println(NlpUtils.getSimilarity(methodDeclaration1.getParameters().toString(), methodDeclaration2.getParameters().toString()));
                    }
                }
            }
        }
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
}
