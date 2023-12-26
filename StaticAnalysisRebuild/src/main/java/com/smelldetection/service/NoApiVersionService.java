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

    public void getNoApiVersion(String directory) throws IOException {
        List<String> services = FileUtils.getServices(directory);
        ApiVersionDetail apiVersionDetail = new ApiVersionDetail();
        for (String service : services) {
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(service);
            String microserviceName = "";
            for (String application : applicationYamlOrProperties) {
                Map<String, String> configuration = new HashMap<>();
                if (application.endsWith("yaml") || application.endsWith("yml")) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yml = yaml.load(new FileInputStream(application));
                    FileUtils.resolveYaml(new Stack<>(), configuration, yml);
                } else {
                    FileUtils.resolveProperties(application, configuration);
                }
                microserviceName = configuration.getOrDefault("spring.application.name", "");
                System.out.println(microserviceName);
                apiVersionDetail.getNoVersion().put(microserviceName, new HashMap<>());
                apiVersionDetail.getMissingUrl().put(microserviceName, new HashMap<>());
            }
            List<String> javaFiles = FileUtils.getJavaFiles(service);
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
        System.out.println(apiVersionDetail);
        // apiVersionDetail.setStatus(status);
        // TODO return apiVersionDetail;
    }

    private void getNoApiVersion(Map<String, String> directoryToMicroservice) throws IOException {
        ApiVersionDetail apiVersionDetail = new ApiVersionDetail();
        for (String directory : directoryToMicroservice.keySet()) {
            String microserviceName = directoryToMicroservice.get(directory);
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
        System.out.println(status + "\n" + apiVersionDetail);
    }
}
