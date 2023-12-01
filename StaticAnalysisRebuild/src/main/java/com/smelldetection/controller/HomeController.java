package com.smelldetection.controller;

import com.smelldetection.entity.smell.detail.HardCodeDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.service.*;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private SharedLibraryService sharedLibraryService;

    @Autowired
    private SharedDatabaseAndServiceIntimacyService sharedDatabaseService;

    @Autowired
    private NoApiVersionService apiVersionService;

    @Autowired
    private NoGatewayService noGatewayService;

    @Autowired
    private HardCodeService hardCodeService;

    @GetMapping("/static")
    public String staticAnalysis(HttpServletRequest request) throws IOException, XmlPullParserException {
        /**
         * 1.获取系统各微服务模块的路径
         * 2.针对每个模块进行静态解析
         *   2.1.解析配置文件
         *   2.2.解析 pom 文件
         *   2.3.解析 .java 文件
         */
        Map<String, String> filePathToMicroserviceName = new HashMap<>();
        List<String> services = FileUtils.getServices(request.getParameter("path"));
        for (String service : services) {
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(service);
            String microserviceName = "";
            List<Configuration> configurations = new ArrayList<>();
            for (String applicationYamlOrProperty : applicationYamlOrProperties) {
                Configuration configuration = new Configuration();
                if (applicationYamlOrProperty.endsWith("yaml") || applicationYamlOrProperty.endsWith("yml")) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yml = yaml.load(new FileInputStream(applicationYamlOrProperty));
                    FileUtils.resolveYaml(new Stack<>(), configuration.getItems(), yml);
                } else {
                    FileUtils.resolveProperties(applicationYamlOrProperty, configuration.getItems());
                }
                microserviceName = configuration.getItems().getOrDefault("spring.application.name", "");
                configurations.add(configuration);
                configuration.getItems().forEach((key, value) -> {
                    System.out.println(key + "=" + value);
                });
            }
            filePathToMicroserviceName.put(service, microserviceName);
            List<String> pomXml = FileUtils.getPomXml(request.getParameter("path"));
            List<Pom> poms = new ArrayList<>();
            for (String p : pomXml) {
                Pom pom = new Pom();
                MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
                Model model = mavenXpp3Reader.read(new FileInputStream(p));
                for (Dependency dependency : model.getDependencies()) {
                    System.out.println(dependency);
                }
                pom.setMavenModel(model);
                poms.add(pom);
            }
            apiVersionService.getNoApiVersion(service);
        }
        return "static";
    }

    @GetMapping("/configuration")
    public String configuration(HttpServletRequest request) throws IOException {
        List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(request.getParameter("path"));
        List<Configuration> configurations = new ArrayList<>();
        for (String application : applicationYamlOrProperties) {
            Configuration configuration = new Configuration();
            if (application.endsWith("yaml") || application.endsWith("yml")) {
                Yaml yaml = new Yaml();
                Map<String, Object> yml = yaml.load(new FileInputStream(application));
                FileUtils.resolveYaml(new Stack<>(), configuration.getItems(), yml);
            } else {
                FileUtils.resolveProperties(application, configuration.getItems());
            }
            configurations.add(configuration);
            configuration.getItems().forEach((key, value) -> {
                System.out.println(key + "=" + value);
            });
        }
        Set<String> sharedDatabases = sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(configurations);
        for (String sharedDatabase : sharedDatabases) {
            System.out.println(sharedDatabase);
        }
        return "configuration";
    }

    @GetMapping("/dependency")
    public String dependency(HttpServletRequest request) throws IOException, XmlPullParserException {
        List<String> pomXml = FileUtils.getPomXml(request.getParameter("path"));
        List<Pom> poms = new ArrayList<>();
        for (String p : pomXml) {
            Pom pom = new Pom();
            MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
            Model model = mavenXpp3Reader.read(new FileInputStream(p));
            for (Dependency dependency : model.getDependencies()) {
                System.out.println(dependency);
            }
            pom.setMavenModel(model);
            poms.add(pom);
        }
        Set<String> sharedLibraries = sharedLibraryService.getSharedLibraries(poms);
        for (String sharedLibrary : sharedLibraries) {
            System.out.println(sharedLibrary);
        }
        return "dependency";
    }

    @GetMapping("/api")
    public String api(HttpServletRequest request) throws IOException {
        List<String> services = FileUtils.getServices(request.getParameter("path"));
        for (String service : services) {
            System.out.println("[service] " + service);
            apiVersionService.getNoApiVersion(service);
        }
        return "api";
    }

    @GetMapping("/gateway")
    public String gateway(HttpServletRequest request) throws IOException, XmlPullParserException {
        List<String> services = FileUtils.getServices(request.getParameter("path"));
        for (String service : services) {
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(service);
            List<Configuration> configurations = new ArrayList<>();
            for (String application : applicationYamlOrProperties) {
                Configuration configuration = new Configuration();
                if (application.endsWith("yaml") || application.endsWith("yml")) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yml = yaml.load(new FileInputStream(application));
                    FileUtils.resolveYaml(new Stack<>(), configuration.getItems(), yml);
                } else {
                    FileUtils.resolveProperties(application, configuration.getItems());
                }
                configurations.add(configuration);
                configuration.getItems().forEach((key, value) -> {
                    System.out.println(key + "=" + value);
                });
            }
            List<String> pomXml = FileUtils.getPomXml(service);
            List<Pom> poms = new ArrayList<>();
            for (String p : pomXml) {
                Pom pom = new Pom();
                MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
                Model model = mavenXpp3Reader.read(new FileInputStream(p));
                for (Dependency dependency : model.getDependencies()) {
                    System.out.println(dependency);
                }
                pom.setMavenModel(model);
                poms.add(pom);
            }
            noGatewayService.getGateway(configurations, poms);
        }
        return "gateway";
    }

    @GetMapping("/hardCode")
    public String hardCode(HttpServletRequest request) throws IOException {
        List<String> services = FileUtils.getServices(request.getParameter("path"));
        List<HardCodeDetail> hardCode = hardCodeService.getHardCode(services);
        for (HardCodeDetail hardCodeDetail : hardCode) {
            System.out.println(hardCodeDetail);
        }
        return "hardCode";
    }
}
