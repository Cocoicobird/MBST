package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.ApiVersionDetail;
import com.smelldetection.entity.smell.detail.NoGatewayDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 判断该微服务系统是否引入网关
 */
@Service
public class NoGatewayService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public NoGatewayDetail getGateway(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, XmlPullParserException {
        long start = System.currentTimeMillis();
        NoGatewayDetail noGatewayDetail = new NoGatewayDetail();
        boolean hasDependency = false;
        boolean hasConfiguration = false;
        Map<String, Configuration> configurations = FileUtils.getConfiguration(filePathToMicroserviceName);
        for (String filePath : filePathToMicroserviceName.keySet()) {
            // 一般一个微服务模块中只有一个 pom 文件
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<Pom> pomObject;
            if (redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_pom") == null || "true".equals(changed)) {
                List<String> pomXml = FileUtils.getPomXml(filePath);
                pomObject = FileUtils.getPomObject(pomXml);
                redisTemplate.opsForValue().set(systemPath + "_" + microserviceName + "_pom", pomObject);
            } else {
                pomObject = (List<Pom>) redisTemplate.opsForValue().get(systemPath + "_" + microserviceName + "_pom");
            }
            for (Pom pom :pomObject) {
                List<Dependency> dependencies = pom.getMavenModel().getDependencies();
                for (Dependency dependency : dependencies) {
                    if (dependency.getGroupId().equals("org.springframework.cloud")
                            && dependency.getArtifactId().equals("spring-cloud-starter-gateway")) {
                        hasDependency = true;
                    } else if (dependency.getGroupId().equals("org.springframework.cloud")
                            && dependency.getArtifactId().equals("spring-cloud-starter-netflix-zuul")) {
                        hasDependency = true;
                    }
                    if (hasDependency) {
                        break;
                    }
                }
            }
            // 当前微服务的配置项
            Configuration configuration = configurations.get(filePath);
            for (String key : configuration.getItems().keySet()) {
                if ((key.startsWith("spring.cloud.gateway") && configuration.getItems().get(key) != null)
                        || (key.startsWith("zuul.routes") && configuration.getItems().get(key) != null)) {
                    hasConfiguration = true;
                }
                if (hasConfiguration) {
                    break;
                }
            }
        }
        noGatewayDetail.setStatus(!(hasDependency && hasConfiguration));
        redisTemplate.opsForValue().set(systemPath + "_noGateway_" + start, noGatewayDetail);
        return noGatewayDetail;
    }

    public List<NoGatewayDetail> getNoGatewayHistory(String systemPath) {
        String key = systemPath + "_noGateway_*";
        Set<String> keys = redisTemplate.keys(key);
        List<NoGatewayDetail> noGatewayDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                noGatewayDetails.add((NoGatewayDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return noGatewayDetails;
    }
}
