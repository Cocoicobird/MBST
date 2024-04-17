package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.NoHealthCheckAndNoServiceDiscoveryPatternDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 微服务系统是否引入健康检查机制
 */
@Service
public class NoHealthCheckAndNoServiceDiscoveryPatternService {

    /**
     * 解析健康检查情况，同时微服务中的健康检查机制与服务发现注册关联
     * 实现思路：针对 Eureka 需要一个服务端依赖，其余服务引入客户端依赖即可；Consul、Zookeeper、Nacos 等一般只需要引入客户端，服务端单独部署
     * @param filePathToMicroserviceName 微服务模块路径与名称的映射
     * @param systemPath 整个微服务系统的路径
     * 相关依赖比如 Consul、ZooKeeper、etcd、Eureka、Linkerd
     */
    public NoHealthCheckAndNoServiceDiscoveryPatternDetail getNoHealthCheckAndNoServiceDiscoveryPattern(Map<String, String> filePathToMicroserviceName, String systemPath) throws IOException, XmlPullParserException {
        List<Pom> parentPom = FileUtils.getPomObject(FileUtils.getParentPomXml(filePathToMicroserviceName, systemPath));
        boolean nacos = false, consul = false;
        for (Pom pom : parentPom) {
            for (Dependency dependency : pom.getMavenModel().getDependencies()) {
                if ("com.alibaba.cloud".equals(dependency.getGroupId()) && "spring-cloud-starter-alibaba-nacos-discovery".equals(dependency.getArtifactId())) {
                    nacos = true;
                }
                if ("org.springframework.cloud".equals(dependency.getGroupId())
                        && "spring-cloud-starter-consul-discovery".equals(dependency.getArtifactId())) {
                    consul = true;
                }
            }
        }
        Map<String, String> eurekaStatus = hasEureka(filePathToMicroserviceName);
        Map<String, Boolean> consulStatus = hasConsul(filePathToMicroserviceName);
        Map<String, Configuration> filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
        NoHealthCheckAndNoServiceDiscoveryPatternDetail result = new NoHealthCheckAndNoServiceDiscoveryPatternDetail();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            Configuration configuration = filePathToConfiguration.get(filePath);
            boolean status = false;
            if ((consul || consulStatus.get(microserviceName)) && configuration.getItems().containsKey("spring.cloud.consul.host")) {
                status = true;
            }
            if ("client".equals(eurekaStatus.get(microserviceName)) && configuration.getItems().containsKey("eureka.client.service-url.defaultZone")) {
                status = true;
            } else if ("server".equals(eurekaStatus.get(microserviceName)) && configuration.getItems().containsKey("eureka.client.fetch-registry")) {
                status = true;
            }
            if (nacos && configuration.getItems().containsKey("spring.cloud.nacos.discovery.server-addr")) {
                status = true;
            }
            result.put(microserviceName, status);
        }
        return result;
    }

    /**
     * 每个微服务模块是否含有 actuator
     * @param filePathToMicroserviceName 微服务模块路径与微服务名称的映射
     */
    private Map<String, Boolean> hasActuator(Map<String, String> filePathToMicroserviceName) throws IOException, XmlPullParserException {
        Map<String, Boolean> actuatorStatus = new HashMap<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            actuatorStatus.put(filePathToMicroserviceName.get(filePath), false);
            // 一般单个模块只有一个 pom 文件
            List<Pom> pomList = FileUtils.getPomObject(FileUtils.getPomXml(filePath));
            for (Pom pom : pomList) {
                pom.setMicroserviceName(filePathToMicroserviceName.get(filePath));
                List<Dependency> dependencies = pom.getMavenModel().getDependencies();
                for (Dependency dependency : dependencies) {
                    if ("org.springframework.boot".equals(dependency.getGroupId())
                            && "spring-boot-starter-actuator".equals(dependency.getArtifactId())) {
                        actuatorStatus.put(filePathToMicroserviceName.get(filePath), true);
                    }
                }
            }
        }
        return actuatorStatus;
    }

    /**
     * 是否引入 Eureka
     * @param filePathToMicroserviceName 微服务模块路径与微服务名称的映射
     */
    private Map<String, String> hasEureka(Map<String, String> filePathToMicroserviceName) throws IOException, XmlPullParserException {
        Map<String, String> eurekaStatus = new HashMap<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            eurekaStatus.put(filePathToMicroserviceName.get(filePath), "");
            List<Pom> pomList = FileUtils.getPomObject(FileUtils.getPomXml(filePath));
            for (Pom pom : pomList) {
                pom.setMicroserviceName(filePathToMicroserviceName.get(filePath));
                List<Dependency> dependencies = pom.getMavenModel().getDependencies();
                for (Dependency dependency : dependencies) {
                    if ("org.springframework.cloud".equals(dependency.getGroupId())
                            && "spring-cloud-starter-netflix-eureka-server".equals(dependency.getArtifactId())) {
                        eurekaStatus.put(filePathToMicroserviceName.get(filePath), "server");
                    } else if ("org.springframework.cloud".equals(dependency.getGroupId())
                            && "spring-cloud-starter-netflix-eureka-client".equals(dependency.getArtifactId())) {
                        eurekaStatus.put(filePathToMicroserviceName.get(filePath), "client");
                    }
                }
            }
        }
        return eurekaStatus;
    }

    /**
     * 判断是否引入 Consul 服务发现组件
     * @param filePathToMicroserviceName 微服务模块路径与微服务名称的映射
     */
    private Map<String, Boolean> hasConsul(Map<String, String> filePathToMicroserviceName) throws IOException, XmlPullParserException {
        Map<String, Boolean> consulStatus = new HashMap<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            consulStatus.put(filePathToMicroserviceName.get(filePath), false);
            List<Pom> pomList = FileUtils.getPomObject(FileUtils.getPomXml(filePath));
            for (Pom pom : pomList) {
                pom.setMicroserviceName(filePathToMicroserviceName.get(filePath));
                List<Dependency> dependencies = pom.getMavenModel().getDependencies();
                for (Dependency dependency : dependencies) {
                    if ("org.springframework.cloud".equals(dependency.getGroupId())
                            && "spring-cloud-starter-consul-discovery".equals(dependency.getArtifactId())) {
                        consulStatus.put(filePathToMicroserviceName.get(filePath), true);
                    }
                }
            }
        }
        return consulStatus;
    }
}
