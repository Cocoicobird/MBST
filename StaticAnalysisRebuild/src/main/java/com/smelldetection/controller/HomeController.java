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
import org.dom4j.DocumentException;
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

    @Autowired
    private WhateverTypesService whateverTypesService;

    @Autowired
    private BloatedService bloatedService;

    @Autowired
    private ChattyService chattyService;

    @Autowired
    private PoorRestfulApiDesignService poorRestfulApiDesignService;

    @Autowired
    private UnnecessarySettingsService unnecessarySettingsService;

    @Autowired
    private HubService hubService;

    @Autowired
    private DuplicatedServicesService duplicatedServicesService;

    @Autowired
    private NoHealthCheckAndNoServiceDiscoveryPatternService noHealthCheckAndNoServiceDiscoveryPatternService;

    @Autowired
    private LocalLoggingService localLoggingService;

    @GetMapping("/static")
    public Map<String, Object> staticAnalysis(HttpServletRequest request) throws IOException, XmlPullParserException, DocumentException {
        /**
         * 1.获取系统各微服务模块的路径
         * 2.针对每个模块进行静态解析
         *   2.1.解析配置文件
         *   2.2.解析 pom 文件
         *   2.3.解析 .java 文件
         */
        String systemPath = request.getParameter("path");
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
        Map<String, Object> result = new HashMap<>();
        result.put("bloatedService", bloatedService.getBloatedService(filePathToMicroserviceName));
        result.put("chattyService", chattyService.getChattyService(filePathToMicroserviceName));
        result.put("cyclicReference", cyclicReferenceService.getCyclicReference(filePathToMicroserviceName));
        result.put("duplicatedService", duplicatedServicesService.getDuplicatedService(filePathToMicroserviceName));
        result.put("ESBUsage", esbUsageService.getESBUsageServices(filePathToMicroserviceName));
        result.put("godService", godService.getGodService(filePathToMicroserviceName));
        result.put("hardCode", hardCodeService.getHardCode(filePathToMicroserviceName));
        result.put("hubService", hubService.getHubClass(filePathToMicroserviceName));
        result.put("noApiVersion", apiVersionService.getNoApiVersion(filePathToMicroserviceName));
        result.put("noGateway", noGatewayService.getGateway(filePathToMicroserviceName));
        result.put("noHealthCheckAndNoServiceDiscoveryPattern", noHealthCheckAndNoServiceDiscoveryPatternService.getNoHealthCheckAndNoServiceDiscoveryPattern(filePathToMicroserviceName, systemPath));
        result.put("scatteredService", scatteredService.getScatteredFunctionalityServices(filePathToMicroserviceName));
        result.put("GreedyService", greedyService.getGreedyService(filePathToMicroserviceName));
        Map<String, Configuration> configuration = FileUtils.getConfiguration(filePathToMicroserviceName);
        result.put("sharedDatabasesAndServiceIntimacy", sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(configuration));
        result.put("sharedLibraries", sharedLibraries(request));
        result.put("unnecessarySettings", unnecessarySettingsService.getUnnecessarySettings(filePathToMicroserviceName, systemPath));
        result.put("whateverTypes", whateverTypesService.getWhateverTypes(filePathToMicroserviceName));
        result.put("wrongCut", wrongCutService.getWrongCut(filePathToMicroserviceName));
        return result;
    }

    @GetMapping("/sharedDatabasesAndServiceIntimacy")
    public SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacy(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        Map<String, Configuration> configuration = FileUtils.getConfiguration(filePathToMicroserviceName);
        SharedDatabasesAndServiceIntimacyDetail sharedDatabases = sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(configuration);
        return sharedDatabases;
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
    public HardCodeDetail hardCode(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return hardCodeService.getHardCode(filePathToMicroserviceName);
    }

    @GetMapping("/tooManyStandards")
    public String tooManyStandards(HttpServletRequest request) throws IOException {
        TooManyStandardsDetail tooManyStandardsDetail = tooManyStandardsService.TooManyStandards(request.getParameter("path"));
        System.out.println(tooManyStandardsDetail);
        return "tooManyStandards";
    }

    @GetMapping("/whateverTypes")
    public WhateverTypesDetail whateverTypes(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return whateverTypesService.getWhateverTypes(filePathToMicroserviceName);
    }

    @GetMapping("/bloatedService")
    public BloatedServiceDetail bloatedService(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return bloatedService.getBloatedService(filePathToMicroserviceName);
    }

    @GetMapping("/chattyService")
    public ChattyServiceDetail chattyService(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return chattyService.getChattyService(filePathToMicroserviceName);
    }

    @GetMapping("/poorRestfulApiDesign")
    public ApiDesignDetail poorRestfulApiDesign(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return poorRestfulApiDesignService.getPoorRestfulApiDesign(filePathToMicroserviceName);
    }

    @GetMapping("/unnecessarySettings")
    public UnnecessarySettingsDetail unnecessarySettings(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return unnecessarySettingsService.getUnnecessarySettings(filePathToMicroserviceName, request.getParameter("path"));
    }

    @GetMapping("/hubService")
    public HubServiceDetail hubService(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return hubService.getHubClass(filePathToMicroserviceName);
    }

    @GetMapping("/duplicatedService")
    public Map<String, Set<List<DuplicatedServiceDetail>>> duplicatedService(HttpServletRequest request) throws IOException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return duplicatedServicesService.getDuplicatedService(filePathToMicroserviceName);
    }

    @GetMapping("/noHealthCheckAndNoServiceDiscoveryPattern")
    public NoHealthCheckAndNoServiceDiscoveryPatternDetail noHealthCheckAndNoServiceDiscoveryPattern(HttpServletRequest request) throws IOException, XmlPullParserException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return noHealthCheckAndNoServiceDiscoveryPatternService.getNoHealthCheckAndNoServiceDiscoveryPattern(filePathToMicroserviceName, request.getParameter("path"));
    }

    @GetMapping("/localLogging")
    public LocalLoggingDetail localLogging(HttpServletRequest request) throws IOException, DocumentException {
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(request.getParameter("path"));
        return localLoggingService.getLocalLoggingService(filePathToMicroserviceName);
    }
}
