package com.smelldetection.controller;

import com.smelldetection.entity.smell.detail.MicroserviceRankDetail;
import com.smelldetection.entity.smell.detail.*;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.service.*;
import com.smelldetection.utils.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private MicroserviceRankService microserviceRankService;

    @GetMapping("/static")
    public Map<String, Object> staticAnalysis(HttpServletRequest request) throws IOException, XmlPullParserException, DocumentException {
        /**
         * 1.获取系统各微服务模块的路径
         * 2.针对每个模块进行静态解析
         *   2.1.解析配置文件
         *   2.2.解析 pom 文件
         *   2.3.解析 .java 文件
         */
        long start = System.currentTimeMillis();
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
        Map<String, Configuration> configuration = FileUtils.getConfiguration(filePathToMicroserviceName);
        redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        redisTemplate.opsForValue().set(systemPath + "_filePathToConfiguration", configuration);
        Map<String, Object> result = new HashMap<>();
        result.put("bloatedService", bloatedService.getBloatedService(filePathToMicroserviceName, systemPath, changed));
        result.put("chattyService", chattyService.getChattyService(filePathToMicroserviceName, systemPath, changed));
        result.put("cyclicReference", cyclicReferenceService.getCyclicReference(filePathToMicroserviceName, systemPath, changed));
        result.put("duplicatedService", duplicatedServicesService.getDuplicatedService(filePathToMicroserviceName, systemPath, changed));
        result.put("ESBUsage", esbUsageService.getESBUsageServices(filePathToMicroserviceName, systemPath, changed));
        result.put("godService", godService.getGodService(filePathToMicroserviceName, systemPath, changed));
        result.put("greedyService", greedyService.getGreedyService(filePathToMicroserviceName, systemPath, changed));
        result.put("hardCode", hardCodeService.getHardCode(filePathToMicroserviceName, systemPath, changed));
        result.put("hubService", hubService.getHubClass(filePathToMicroserviceName, systemPath, changed));
        result.put("localLogging", localLoggingService.getLocalLoggingService(filePathToMicroserviceName, systemPath, changed));
        result.put("noApiVersion", apiVersionService.getNoApiVersion(filePathToMicroserviceName, systemPath, changed));
        result.put("noGateway", noGatewayService.getGateway(filePathToMicroserviceName, systemPath, changed));
        NoHealthCheckAndNoServiceDiscoveryPatternDetail noHealthCheckAndNoServiceDiscoveryPattern = noHealthCheckAndNoServiceDiscoveryPatternService.getNoHealthCheckAndNoServiceDiscoveryPattern(filePathToMicroserviceName, systemPath, changed);
        result.put("noHealthCheck", noHealthCheckAndNoServiceDiscoveryPattern);
        result.put("noServiceDiscoveryPattern", noHealthCheckAndNoServiceDiscoveryPattern);
        result.put("poorRestfulApiDesign", poorRestfulApiDesignService.getPoorRestfulApiDesign(filePathToMicroserviceName, systemPath, changed));
        result.put("scatteredService", scatteredService.getScatteredFunctionalityServices(filePathToMicroserviceName, systemPath, changed));
        SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacy = sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(configuration, systemPath, changed);
        result.put("sharedDatabases", sharedDatabasesAndServiceIntimacy.getSharedDatabases());
        result.put("serviceIntimacy", sharedDatabasesAndServiceIntimacy.getServiceIntimacy());
        result.put("sharedLibraries", sharedLibraryService.getSharedLibraries(filePathToMicroserviceName, systemPath, changed));
        result.put("unnecessarySettings", unnecessarySettingsService.getUnnecessarySettings(filePathToMicroserviceName, systemPath, changed));
        result.put("whateverTypes", whateverTypesService.getWhateverTypes(filePathToMicroserviceName, systemPath, changed));
        result.put("wrongCut", wrongCutService.getWrongCut(filePathToMicroserviceName, systemPath, changed));
        redisTemplate.opsForValue().set(systemPath + "_static_" + start, result);
        return result;
    }

    @GetMapping("/sharedDatabasesAndServiceIntimacy")
    public SharedDatabasesAndServiceIntimacyDetail sharedDatabasesAndServiceIntimacy(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
        Map<String, Configuration> filePathToConfiguration;
        if (redisTemplate.opsForValue().get(systemPath + "_filePathToConfiguration") == null || "true".equals(changed)) {
            filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemPath + "_filePathToConfiguration", filePathToConfiguration);
        } else {
            filePathToConfiguration = (Map<String, Configuration>) redisTemplate.opsForValue().get(systemPath + "_filePathToConfiguration");
        }
        SharedDatabasesAndServiceIntimacyDetail sharedDatabases = sharedDatabaseService.getSharedDatabasesAndServiceIntimacy(filePathToConfiguration, systemPath, changed);
        return sharedDatabases;
    }

    @GetMapping("/noApiVersion")
    public ApiVersionDetail noApiVersion(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        ApiVersionDetail apiVersion = apiVersionService.getNoApiVersion(filePathToMicroserviceName, systemPath, changed);
        return apiVersion;
    }

    @GetMapping("/sharedLibraries")
    public SharedLibraryDetail sharedLibraries(HttpServletRequest request) throws IOException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return sharedLibraryService.getSharedLibraries(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/noGateway")
    public NoGatewayDetail gateway(HttpServletRequest request) throws IOException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        NoGatewayDetail noGatewayDetail = noGatewayService.getGateway(filePathToMicroserviceName, systemPath, changed);
        return noGatewayDetail;
    }

    @GetMapping("/scatteredService")
    public ScatteredServiceDetail scatteredService(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        ScatteredServiceDetail scatteredServiceDetail = scatteredService.getScatteredFunctionalityServices(filePathToMicroserviceName, systemPath, changed);
        return scatteredServiceDetail;
    }

    @GetMapping("/hardCode")
    public HardCodeDetail hardCode(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return hardCodeService.getHardCode(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/tooManyStandards")
    public String tooManyStandards(HttpServletRequest request) throws IOException {
        TooManyStandardsDetail tooManyStandardsDetail = tooManyStandardsService.TooManyStandards(request.getParameter("path"));
        System.out.println(tooManyStandardsDetail);
        return "tooManyStandards";
    }

    @GetMapping("/whateverTypes")
    public WhateverTypesDetail whateverTypes(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return whateverTypesService.getWhateverTypes(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/bloatedService")
    public BloatedServiceDetail bloatedService(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return bloatedService.getBloatedService(filePathToMicroserviceName, systemPath, changed);
    }


    @GetMapping("/chattyService")
    public ChattyServiceDetail chattyService(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return chattyService.getChattyService(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/poorRestfulApiDesign")
    public ApiDesignDetail poorRestfulApiDesign(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return poorRestfulApiDesignService.getPoorRestfulApiDesign(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/unnecessarySettings")
    public UnnecessarySettingsDetail unnecessarySettings(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return unnecessarySettingsService.getUnnecessarySettings(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/hubService")
    public HubServiceDetail hubService(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return hubService.getHubClass(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/duplicatedService")
    public Map<String, Set<List<DuplicatedServiceDetail>>> duplicatedService(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return duplicatedServicesService.getDuplicatedService(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/noHealthCheckAndNoServiceDiscoveryPattern")
    public NoHealthCheckAndNoServiceDiscoveryPatternDetail noHealthCheckAndNoServiceDiscoveryPattern(HttpServletRequest request) throws IOException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return noHealthCheckAndNoServiceDiscoveryPatternService.getNoHealthCheckAndNoServiceDiscoveryPattern(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/localLogging")
    public LocalLoggingDetail localLogging(HttpServletRequest request) throws IOException, DocumentException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return localLoggingService.getLocalLoggingService(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/cyclicReference")
    public CyclicReferenceDetail cyclicReference(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return cyclicReferenceService.getCyclicReference(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/esbUsage")
    public ESBUsageDetail esbUsage(HttpServletRequest request) throws IOException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return esbUsageService.getESBUsageServices(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/godService")
    public GodServiceDetail godService(HttpServletRequest request) throws IOException, DocumentException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return godService.getGodService(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/greedyService")
    public ServiceGreedyDetail greedyService(HttpServletRequest request) throws IOException, DocumentException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return greedyService.getGreedyService(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/wrongCut")
    public WrongCutDetail wrongCut(HttpServletRequest request) throws IOException, DocumentException, XmlPullParserException {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return wrongCutService.getWrongCut(filePathToMicroserviceName, systemPath, changed);
    }

    @GetMapping("/microserviceRank")
    public List<MicroserviceRankDetail> microserviceRank(HttpServletRequest request) throws Exception {
        String systemPath = request.getParameter("path");
        String changed = request.getParameter("changed");
        Map<String, String> filePathToMicroserviceName = (Map<String, String>) redisTemplate.opsForValue().get(systemPath);
        if (filePathToMicroserviceName == null || "true".equals(changed)) {
            filePathToMicroserviceName = FileUtils.getFilePathToMicroserviceName(systemPath);
            redisTemplate.opsForValue().set(systemPath + "_filePathToMicroserviceName", filePathToMicroserviceName);
        }
        return microserviceRankService.getMicroserviceRank(filePathToMicroserviceName, systemPath, changed);
    }
}
