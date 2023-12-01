package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.SharedLibraryDetail;
import com.smelldetection.entity.system.component.Pom;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class SharedLibraryService {

    public Set<String> getSharedLibraries(List<Pom> poms) {
        Map<String, List<SharedLibraryDetail>> sharedLibraryDetails = new HashMap<>();
        int num = poms.size();
        for (int i = 0; i < num; i++) {
            for (int j = i + 1; j < num; j++) {
                Model mavenModel1 = poms.get(i).getMavenModel();
                Model mavenModel2 = poms.get(j).getMavenModel();
                for (Dependency dependency1 : mavenModel1.getDependencies()) {
                    for (Dependency dependency2 : mavenModel2.getDependencies()) {
                        if (dependency1.getGroupId().equals(dependency2.getGroupId())
                                && dependency1.getArtifactId().equals(dependency2.getArtifactId())) {
                            if (dependency1.getGroupId().startsWith("org.springframework.boot")) {
                                continue;
                            }
                            String sharedLibrary = dependency1.getGroupId() + "." + dependency2.getArtifactId();
                            if (dependency1.getVersion() != null
                                    &&dependency1.getVersion().equals(dependency2.getVersion())) {
                                sharedLibrary += "." + dependency1.getVersion();
                            }
                            String service1 = mavenModel1.getGroupId() + mavenModel2.getArtifactId();
                            String service2 = mavenModel2.getGroupId() + mavenModel2.getArtifactId();
                            if (!sharedLibraryDetails.containsKey(sharedLibrary)) {
                                sharedLibraryDetails.put(sharedLibrary, new ArrayList<>());
                            }
                            sharedLibraryDetails.get(sharedLibrary).add(new SharedLibraryDetail(service1, service2));
                        }
                    }
                }
            }
        }
        // TODO
        return new LinkedHashSet<>();
    }
}
