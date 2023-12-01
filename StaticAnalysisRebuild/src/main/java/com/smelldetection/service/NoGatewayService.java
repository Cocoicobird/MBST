package com.smelldetection.service;

import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import org.apache.maven.model.Dependency;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class NoGatewayService {

    public void getGateway(List<Configuration> configurations, List<Pom> poms) {
        System.out.println(configurations.size() + " " + poms.size());
        boolean hasDependency = false;
        boolean hasConfiguration = false;
        for (Pom pom : poms) {
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
        System.out.println("hasDependency " + hasDependency + " hasConfiguration " + hasConfiguration);
    }
}
