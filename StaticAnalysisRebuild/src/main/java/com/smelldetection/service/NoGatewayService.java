package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.NoGatewayDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 判断该微服务系统是否引入网关
 */
@Service
public class NoGatewayService {

    public NoGatewayDetail getGateway(Map<String, String> filePathToMicroserviceName) throws IOException, XmlPullParserException {
        NoGatewayDetail noGatewayDetail = new NoGatewayDetail();
        boolean hasDependency = false;
        boolean hasConfiguration = false;
        for (String filePath : filePathToMicroserviceName.keySet()) {
            // 一般一个微服务模块中只有一个 pom 文件
            List<String> pomXml = FileUtils.getPomXml(filePath);
            List<Pom> pomObject = FileUtils.getPomObject(pomXml);
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
            List<Configuration> configurations = FileUtils.getConfiguration(filePathToMicroserviceName);
            for (Configuration configuration : configurations) {
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
        }
        noGatewayDetail.setStatus(!(hasDependency && hasConfiguration));
        return noGatewayDetail;
    }
}
