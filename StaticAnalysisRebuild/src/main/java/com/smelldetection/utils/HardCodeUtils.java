package com.smelldetection.utils;

import com.smelldetection.entity.smell.detail.HardCodeDetail;
import com.smelldetection.entity.item.HardCodeItem;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cocoicobird
 * @version 1.0
 */
public class HardCodeUtils {

    private static final String REGEX_IP_PORT = "((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(:\\d{0,5})";
    private static final String REGEX_IP = "((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)";
    private static final String REGEX_LOCALHOST = "localhost:\\d+";

    /**
     * 检测 microservice 模块下 .java 文件中的硬编码
     * @param javaFiles .java 文件路径列表
     * @param hardCodeItems 存储硬编码信息
     */
    public static void resolveHardCodeFromJavaFiles(List<String> javaFiles,
                                                    List<HardCodeItem> hardCodeItems) throws IOException {
        for (String javaFile : javaFiles) {
            HardCodeItem hardCodeItem = inspectJavaFile(javaFile);
            if (!hardCodeItem.getHardCodeAndPositions().isEmpty()) {
                hardCodeItems.add(hardCodeItem);
            }
        }
    }

    private static HardCodeItem inspectJavaFile(String  javaFile) throws IOException {
        HardCodeItem hardCodeItem = new HardCodeItem();
        hardCodeItem.setFileName(javaFile);
        hardCodeItem.setFilePath(javaFile);
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(javaFile));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        int row = 0;
        String line;
        /**
         * left 用于标记多行注解
         */
        boolean left = false;
        while ((line = bufferedReader.readLine()) != null) {
            row++;
            if (line.trim().startsWith("//"))
                continue;
            if (line.trim().startsWith("/*")) {
                left = true;
                continue;
            }
            if (left && line.trim().startsWith("*/")) {
                left = false;
                continue;
            }
            Matcher ipPortMatcher = Pattern.compile(REGEX_IP_PORT).matcher(line);
            Matcher ipMatcher = Pattern.compile(REGEX_IP).matcher(line);
            Matcher localhostMatcher = Pattern.compile(REGEX_LOCALHOST).matcher(line);
            if (ipPortMatcher.find()) {
                hardCodeItem.getHardCodeAndPositions().put(row, ipPortMatcher.group());
            } else if (ipMatcher.find()) {
                hardCodeItem.getHardCodeAndPositions().put(row, ipMatcher.group());
            } else if (localhostMatcher.find()) {
                hardCodeItem.getHardCodeAndPositions().put(row, localhostMatcher.group());
            }
        }
        bufferedReader.close();
        inputStreamReader.close();
        return hardCodeItem;
    }
}
