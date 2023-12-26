package com.smelldetection.utils;

import com.smelldetection.entity.item.ProgrammingLanguageItem;
import kong.unirest.Unirest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 微服务开发语言分析
 */
public class ProgrammingLanguageUtils {

    private static final String languageAnalysisServerIp = "172.16.17.37";
    private static final String languageAnalysisServerPort = "3000";
    private static final List<String> programmingLanguages = Arrays.asList("Java", "JavaScript", "C#", "Python", "TypeScript", "Go",
            "PHP", "Shell", "Ruby");

    /**
     * 获取该目录下所使用的开发语言及其占比
     * @param directory 项目目录
     */
    private static Map<String, Double> getLanguages(String directory) {
        Map responseBody = Unirest.get("http://" + languageAnalysisServerIp + ":" + languageAnalysisServerPort + "/languists/langanalysis")
                .queryString("path", directory)
                .asObject(Map.class)
                .getBody();
        return responseBody;
    }

    /**
     * 获取该目录下所使用的开发语言及其对应文件
     * @param directory 项目目录
     */
    private static Map<String, List<String>> getFileList(String directory) {
        Map responseBody = Unirest.get("http://" + languageAnalysisServerIp + ":" + languageAnalysisServerPort + "/languists/langfilelist")
                .queryString("path", directory)
                .asObject(Map.class)
                .getBody();
        return responseBody;
    }

    /**
     * 获取某一微服务的编程语言使用情况
     * @param directory 微服务目录
     */
    public static Map<String, ProgrammingLanguageItem> analyseProgrammingLanguage(String directory) {
        Map<String, ProgrammingLanguageItem> programmingLanguageItemMap = new HashMap<>();
        if (!"".equals(directory)) {
            Map<String, Double> languageAndPercent = getLanguages(directory);
            Map<String, List<String>> languageAndFileList = getFileList(directory);
            if (languageAndPercent != null) {
                for (String language : languageAndPercent.keySet()) {
                    if (programmingLanguages.contains(language)) {
                        ProgrammingLanguageItem programmingLanguageItem = new ProgrammingLanguageItem();
                        programmingLanguageItem.setLanguageName(language);
                        programmingLanguageItem.setBytes(languageAndPercent.get(language));
                        programmingLanguageItem.setFileList(languageAndFileList.get(language));
                        programmingLanguageItemMap.put(language, programmingLanguageItem);
                    }
                }
            }
        }
        return programmingLanguageItemMap;
    }

}
