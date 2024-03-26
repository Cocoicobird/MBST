package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.utils.JavaParserUtils;
import com.smelldetection.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class NoApiVersionService {

    public ApiVersionDetail getNoApiVersion(Map<String, String> filePathToMicroserviceName) throws IOException {
        ApiVersionDetail apiVersionDetail = new ApiVersionDetail();
        for (String directory : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(directory);
            apiVersionDetail.getNoVersion().put(microserviceName, new HashMap<>());
            apiVersionDetail.getMissingUrl().put(microserviceName, new HashMap<>());
            List<String> javaFiles = FileUtils.getJavaFiles(directory);
            for (String javaFile : javaFiles) {
                File file = new File(javaFile);
                JavaParserUtils.resolveApiFromJavaFile(file, apiVersionDetail, microserviceName);
            }
        }
        boolean status = false;
        for(String microserviceName: apiVersionDetail.getNoVersion().keySet()) {
            if(!apiVersionDetail.getNoVersion().get(microserviceName).isEmpty()){
                status = true;
                break;
            }
        }
        return apiVersionDetail;
    }
}
