package com.smelldetection.service;

import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.ServiceCallParserUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 频繁调用其它服务
 * 根据当前服务请求其他服务的最大请求次数判定
 * 请求其他服务的最大请求次数 > 5 && 当前服务的实体类数量或 dto 类数量 >= 1，频繁调用是为了获取数据
 */
@Service
public class ChattyService {
    public void getChattyService(Map<String, String> filePathToMicroserviceName) throws IOException {
        // 微服务调用结果，每个微服务调用了哪些微服务以及次数
        Map<String, Map<String, Integer>> microserviceCallResults = ServiceCallParserUtils.getMicroserviceCallResults(filePathToMicroserviceName);
        for (String filePath: filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            int maxCallNumber = 0;
            assert microserviceCallResults != null;
            Map<String, Integer> microserviceCallResult = microserviceCallResults.get(microserviceName);
            if (microserviceCallResult.size() == 0)
                maxCallNumber = -1;
            for (String calledMicroserviceName : microserviceCallResult.keySet()) {
                maxCallNumber = Math.max(maxCallNumber, microserviceCallResult.get(calledMicroserviceName));
            }
            // entity 相关目录下的 .java 文件，一般是实体类，有些项目中也会将 DTO 置于该目录
            List<String> entityClasses = FileUtils.getJavaFilesUnderEntity(filePath);
            boolean status = maxCallNumber > 5 && entityClasses.size() >= 1;
            System.out.println(microserviceCallResult);
            System.out.println(entityClasses);
            System.out.println(microserviceName + " " + maxCallNumber + " " + entityClasses.size() + " " + status);
        }
    }
}
