package com.smelldetection.controller;

import com.smelldetection.entity.smell.detail.*;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.entity.system.component.Pom;
import com.smelldetection.service.*;
import com.smelldetection.utils.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private RedisTemplate<String, Object> redisTemplate;

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

    @Autowired
    private TooManyStandardsService tooManyStandardsService;

    @Autowired
    private CyclicReferenceService cyclicReferenceService;

    @Autowired
    private GreedyService greedyService;

    @Autowired
    private WrongCutService wrongCutService;

    @Autowired
    private ESBUsageService esbUsageService;

    @Autowired
    private ScatteredService scatteredService;

    @Autowired
    private GodService godService;

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
        // 存储所有微服务模块路径
        redisTemplate.opsForValue().set("microservices", services);
        for (String service : services) {
            List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(service);
            // 存储微服务模块路径及其配置文件路径集合
            redisTemplate.opsForValue().set(service, applicationYamlOrProperties);
            redisTemplate.opsForValue().set(service, applicationYamlOrProperties);
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
        }
        apiVersionService.getNoApiVersion(filePathToMicroserviceName);
        return "static";
    }

    @GetMapping("/configuration")
    public String configuration(HttpServletRequest request) throws IOException {
        List<String> microservices = (List<String>) redisTemplate.opsForValue().get("microservices");
        List<String> applicationYamlOrProperties = new ArrayList<>();
        for (String microservice : microservices) {
            List<String> app = (List<String>) redisTemplate.opsForValue().get(microservice);
            assert app != null;
            applicationYamlOrProperties.addAll(app);
        }
        // List<String> applicationYamlOrProperties = FileUtils.getApplicationYamlOrProperties(request.getParameter("path"));
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
        SharedDatabasesAndServiceIntimacyDetail sharedDatabases = sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(configurations);
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
        SharedLibraryDetail sharedLibraries = sharedLibraryService.getSharedLibraries(poms);
        System.out.println(sharedLibraries);
        return "dependency\n" + sharedLibraries.toString();
    }

    @GetMapping("/api")
    public ApiVersionDetail api(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        ApiVersionDetail apiVersion = apiVersionService.getNoApiVersion(filePathToMicroserviceName);
        return apiVersion;
    }

    @GetMapping("/sharedLibraries")
    public SharedLibraryDetail sharedLibraries(HttpServletRequest request) throws IOException, XmlPullParserException {
        List<String> services = FileUtils.getServices(request.getParameter("path"));
        List<Pom> poms = new ArrayList<>();
        System.out.println(services);
        for (String service : services) {
            Pom pomXml = new Pom();
            MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
            Model model = mavenXpp3Reader.read(new FileInputStream(service + "pom.xml"));
            pomXml.setMavenModel(model);
            poms.add(pomXml);
        }
        return sharedLibraryService.getSharedLibraries(poms);
    }

    @GetMapping("/noGateway")
    public NoGatewayDetail gateway(HttpServletRequest request) throws IOException, XmlPullParserException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        NoGatewayDetail noGatewayDetail = noGatewayService.getGateway(filePathToMicroserviceName);
        return noGatewayDetail;
    }

    @GetMapping("/scatteredService")
    public ScatteredServiceDetail scatteredService(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        ScatteredServiceDetail scatteredServiceDetail = scatteredService.getScatteredFunctionalityServices(filePathToMicroserviceName);
        return scatteredServiceDetail;
    }

    @GetMapping("/hardCode")
    public List<HardCodeDetail> hardCode(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        List<HardCodeDetail> hardCode = hardCodeService.getHardCode(filePathToMicroserviceName);
        for (HardCodeDetail hardCodeDetail : hardCode) {
            System.out.println(hardCodeDetail);
        }
        return hardCode;
    }

    @GetMapping("/tooManyStandards")
    public String tooManyStandards(HttpServletRequest request) throws IOException {
        TooManyStandardsDetail tooManyStandardsDetail = tooManyStandardsService.TooManyStandards(request.getParameter("path"));
        System.out.println(tooManyStandardsDetail);
        return "tooManyStandards";
    }
}
