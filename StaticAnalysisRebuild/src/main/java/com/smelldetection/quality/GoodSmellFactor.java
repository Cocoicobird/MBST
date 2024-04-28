package com.smelldetection.quality;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 异味影响因子
 */
public class GoodSmellFactor {
    public static final int noBloatedService = 3; // BloatedService
    public static final int noChattyService = 2; // ChattyService
    public static final int noCircleReference = 2; // CyclicReference
    public static final int noDuplicatedServices = 2; // DuplicatedServices
    public static final int noESB = 4; // ESBUsage
    public static final int noGodComponent = 6; // GodService or MegaService
    public static final int noServiceGreedy = 4; // GreedyService
    public static final int noHardcode = 3; // HardCodes
    public static final int noHub = 2; // HubService
    public static final int noLocalLogging = 1; // LocalLogging
    public static final int hasApiVersion = 2; // NoApiVersion
    public static final int hasApiGateway = 2; // NoGateway
    public static final int hasHealthCheck = 1; // NoHealthCheck
    public static final int hasServiceDiscoveryPattern = 6; // NoServiceDiscoveryPattern
    public static final int goodRestfulApiDesign = 1; // PoorRestfulApiDesign
    public static final int noScatteredFunctionality = 2; // ScatteredFunctionality
    public static final int separatedDatabase = 4; // SharedDatabases
    public static final int appropriateServiceIntimacy = 4; // ServiceIntimacy
    public static final int separatedDependency = 5; // SharedLibraries
    public static final int noUnnecessarySettings = 1; // UnnecessarySettings
    public static final int noWhateverTypes = 4; // WhateverTypes
    public static final int correctServicesCut = 5; // WrongCut
    // public static final int noCircleDependencies = 7; // CyclicDependency
}
