package com.smelldetection.service;

import com.smelldetection.base.Enum.GateWayType;
import com.smelldetection.base.context.GateWayContext;
import com.smelldetection.base.factory.FileFactory;
import com.smelldetection.base.item.RequestItem;
import lombok.NoArgsConstructor;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @description:
 * @author: xyc
 * @date: 2022-12-25 09:11
 */
@Service
@NoArgsConstructor
public class NoGateWayService {
    @Autowired
    public FileFactory fileFactory;

    public GateWayContext isExistGateWay(RequestItem request) throws IOException, XmlPullParserException {
        String path = request.getServicesPath();
        String servicesDirectory = new File(path).getAbsolutePath();
        List<String> pomFiles = fileFactory.getPomFiles(servicesDirectory);
        GateWayContext gateWayContext = new GateWayContext();
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        boolean hasDependency = false;
        boolean hasProperity = false;
        Yaml yaml = new Yaml();
        // 依赖判断
        for (String pom: pomFiles) {
            Model svc1 = mavenReader.read(new FileReader(pom));
            for (Dependency dependency1 : svc1.getDependencies()) {
                if (dependency1.getGroupId().equals("org.springframework.cloud") && dependency1.getArtifactId().equals("spring-cloud-starter-gateway")) {
                    gateWayContext.setType(GateWayType.SpringCloudGateWay);
                    hasDependency = true;

                } else if (dependency1.getGroupId().equals("org.springframework.cloud") && dependency1.getArtifactId().equals("spring-cloud-starter-netflix-zuul")) {
                    gateWayContext.setType(GateWayType.Zuul);
                    hasDependency = true;
                }
                if (hasDependency) {
                    break;
                }
            }
                if(hasDependency){
                    break;
                }
        }
        // 配置文件判断
        List<String> applicationYamlOrPropertities = fileFactory.getApplicationYamlOrPropertities(servicesDirectory);
        Map gateway = null;
        Map zuul = null;
        for (String app : applicationYamlOrPropertities) {
            if (app.endsWith("yaml") || app.endsWith("yml")) {
                Map map = yaml.load(new FileInputStream(app));
                Map m1 = (Map) map.get("spring");
                Map m2 = (Map) m1.get("cloud");
                Map m3 = null;
                if(m2 != null){
                    m3 = (Map)m2.get("gateway");
                }
                if(m3 != null || map.get("zuul") != null){
                    hasProperity = true;
                    break;
                }
            } else {
                InputStream in = new BufferedInputStream(new FileInputStream(app));
                Properties p = new Properties();
                p.load(in);
                for(String key: p.stringPropertyNames()){
                    if(key.contains("spring.cloud.gateway") || key.contains("zuul.routes")){
                        hasProperity = true;
                        break;
                    }
                }

            }

        }
        if(hasDependency && hasProperity){
            gateWayContext.setHasGateWay(true);
            gateWayContext.setStatus(false);
            return  gateWayContext;
        }
        else {
            gateWayContext.setType(null);
            return  gateWayContext;
        }
    }

}
