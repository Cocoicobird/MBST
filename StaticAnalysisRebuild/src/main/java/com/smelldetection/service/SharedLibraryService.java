package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.LocalLoggingDetail;
import com.smelldetection.entity.smell.detail.SharedLibraryDetail;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class SharedLibraryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public SharedLibraryDetail getSharedLibraries(Map<String, String> filePathToMicroserviceName, String systemPath, String changed) throws IOException, XmlPullParserException {
        long start = System.currentTimeMillis();
        SharedLibraryDetail sharedLibraryDetail = new SharedLibraryDetail();
        List<Pom> poms = new ArrayList<>();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<Pom> pomObject = FileUtils.getPomObject(FileUtils.getPomXml(filePath));
            for (Pom pom : pomObject) {
                pom.setMicroserviceName(microserviceName);
            }
            poms.addAll(pomObject);
        }
        int num = poms.size();
        for (int i = 0; i < num; i++) {
            for (int j = i + 1; j < num; j++) {
                Model mavenModel1 = poms.get(i).getMavenModel();
                Model mavenModel2 = poms.get(j).getMavenModel();
                for (Dependency dependency1 : mavenModel1.getDependencies()) {
                    for (Dependency dependency2 : mavenModel2.getDependencies()) {
                        if (dependency1.getGroupId().equals(dependency2.getGroupId())
                                && dependency1.getArtifactId().equals(dependency2.getArtifactId())) {
                            if (dependency1.getGroupId().startsWith("org.springframework")) {
                                continue;
                            }
                            String sharedLibrary = dependency1.getGroupId() + "." + dependency2.getArtifactId();
                            if (dependency1.getVersion() != null
                                    && dependency1.getVersion().equals(dependency2.getVersion())) {
                                sharedLibrary += "." + dependency1.getVersion();
                            }
                            String service1 = poms.get(i).getMicroserviceName();
                            String service2 = poms.get(j).getMicroserviceName();
                            sharedLibraryDetail.put(sharedLibrary, service1);
                            sharedLibraryDetail.put(sharedLibrary, service2);
                        }
                    }
                }
            }
        }
        redisTemplate.opsForValue().set(systemPath + "_sharedLibraries_" + start, sharedLibraryDetail);
        return sharedLibraryDetail;
    }

    public List<SharedLibraryDetail> getSharedLibrariesHistory(String systemPath) {
        String key = systemPath + "_localLogging_*";
        Set<String> keys = redisTemplate.keys(key);
        List<SharedLibraryDetail> sharedLibraryDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                sharedLibraryDetails.add((SharedLibraryDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return sharedLibraryDetails;
    }
}
