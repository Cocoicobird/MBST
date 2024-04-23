package com.smelldetection.controller;

import com.smelldetection.entity.smell.detail.*;
import com.smelldetection.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Controller("/history")
public class HistoryController {

    @Autowired
    private BloatedService bloatedService;

    @Autowired
    private ChattyService chattyService;

    @Autowired
    private CyclicReferenceService cyclicReferenceService;

    @Autowired
    private DuplicatedServicesService duplicatedServicesService;

    @Autowired
    private ESBUsageService esbUsageService;

    @Autowired
    private GodService godService;

    @Autowired
    private GreedyService greedyService;

    @Autowired
    private HardCodeService hardCodeService;

    @Autowired
    private HubService hubService;

    @Autowired
    private LocalLoggingService localLoggingService;

    @Autowired
    private NoApiVersionService noApiVersionService;

    @Autowired
    private NoGatewayService noGatewayService;

    @Autowired
    private NoHealthCheckAndNoServiceDiscoveryPatternService noHealthCheckAndNoServiceDiscoveryPatternService;

    @Autowired
    private PoorRestfulApiDesignService poorRestfulApiDesignService;

    @Autowired
    private ScatteredService scatteredService;

    @Autowired
    private SharedDatabaseAndServiceIntimacyService sharedDatabaseAndServiceIntimacyService;

    @Autowired
    private SharedLibraryService sharedLibraryService;

    @Autowired
    private UnnecessarySettingsService unnecessarySettingsService;

    @Autowired
    private WhateverTypesService whateverTypesService;

    @Autowired
    private WrongCutService wrongCutService;

    @GetMapping("/bloatedService")
    public List<BloatedServiceDetail> bloatedServiceHistory(HttpServletRequest request) {
        return bloatedService.getBloatedServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/chattyService")
    public List<ChattyServiceDetail> chattyServiceHistory(HttpServletRequest request) {
        return chattyService.getChattyServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/cyclicReference")
    public List<CyclicReferenceDetail> cyclicReferenceHistory(HttpServletRequest request) {
        return cyclicReferenceService.getCyclicReferenceHistory(request.getParameter("path"));
    }

    @GetMapping("/duplicatedService")
    public List<Map<String, Set<List<DuplicatedServiceDetail>>>> duplicatedServiceHistory(HttpServletRequest request) {
        return duplicatedServicesService.getDuplicatedServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/esbUsage")
    public List<ESBUsageDetail> esbUsageHistory(HttpServletRequest request) {
        return esbUsageService.getESBUsageHistory(request.getParameter("path"));
    }

    @GetMapping("/godService")
    public List<GodServiceDetail> godServiceHistory(HttpServletRequest request) {
        return godService.getGodServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/greedyService")
    public List<ServiceGreedyDetail> greedyServiceHistory(HttpServletRequest request) {
        return greedyService.getGreedyServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/hardCode")
    public List<HardCodeDetail> hardCodeHistory(HttpServletRequest request) {
        return hardCodeService.getHardCodeHistory(request.getParameter("path"));
    }

    @GetMapping("/hubService")
    public List<HubServiceDetail> hubServiceHistory(HttpServletRequest request) {
        return hubService.getHubServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/localLogging")
    public List<LocalLoggingDetail> localLoggingHistory(HttpServletRequest request) {
        return localLoggingService.getLocalLoggingHistory(request.getParameter("path"));
    }

    @GetMapping("/noApiVersion")
    public List<ApiVersionDetail> noApiVersionHistory(HttpServletRequest request) {
        return noApiVersionService.getNoApiVersionHistory(request.getParameter("path"));
    }

    @GetMapping("/noGateway")
    public List<NoGatewayDetail> noGatewayHistory(HttpServletRequest request) {
        return noGatewayService.getNoGatewayHistory(request.getParameter("path"));
    }

    @GetMapping("/noHealthCheckAndNoServiceDiscoveryPattern")
    public List<NoHealthCheckAndNoServiceDiscoveryPatternDetail> noHealthCheckAndNoServiceDiscoveryPatternHistory(HttpServletRequest request) {
        return noHealthCheckAndNoServiceDiscoveryPatternService.getNoHealthCheckAndNoServiceDiscoveryPatternHistory(request.getParameter("path"));
    }

    @GetMapping("/poorRestfulApiDesign")
    public List<ApiDesignDetail> poorRestfulApiDesignHistory(HttpServletRequest request) {
        return poorRestfulApiDesignService.getPoorRestfulApiDesignHistory(request.getParameter("path"));
    }

    @GetMapping("/scatteredService")
    public List<ScatteredServiceDetail> scatteredServiceHistory(HttpServletRequest request) {
        return scatteredService.getScatteredFunctionalityServiceHistory(request.getParameter("path"));
    }

    @GetMapping("/sharedDatabaseAndServiceIntimacy")
    public List<SharedDatabasesAndServiceIntimacyDetail> sharedDatabasesAndServiceIntimacyHistory(HttpServletRequest request) {
        return sharedDatabaseAndServiceIntimacyService.getSharedDatabasesAndServiceIntimacyHistory(request.getParameter("path"));
    }

    @GetMapping("/sharedLibraries")
    public List<SharedLibraryDetail> sharedLibrariesHistory(HttpServletRequest request) {
        return sharedLibraryService.getSharedLibrariesHistory(request.getParameter("path"));
    }

    @GetMapping("/unnecessarySettings")
    public List<UnnecessarySettingsDetail> unnecessarySettingsHistory(HttpServletRequest request) {
        return unnecessarySettingsService.getUnnecessarySettingsHistory(request.getParameter("path"));
    }

    @GetMapping("/whateverTypes")
    public List<WhateverTypesDetail> whateverTypesHistory(HttpServletRequest request) {
        return whateverTypesService.getWhateverTypesHistory(request.getParameter("path"));
    }

    @GetMapping("/wrongCut")
    public List<WrongCutDetail> wrongCutHistory(HttpServletRequest request) {
        return wrongCutService.getWrongCutHistory(request.getParameter("path"));
    }
}
