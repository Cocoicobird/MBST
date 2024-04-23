package com.smelldetection.service;

import com.smelldetection.entity.smell.detail.SharedDatabasesAndServiceIntimacyDetail;
import com.smelldetection.entity.smell.detail.UnnecessarySettingsDetail;
import com.smelldetection.entity.system.component.Configuration;
import com.smelldetection.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 不必要的配置，如配置为默认项可以不进行配置
 * 预加载了 SpringBoot 的一些通用配置
 */
@Service
public class UnnecessarySettingsService {

    private static final Map<String, String> defaultConfiguration = new HashMap<>();

    static {
        try (BufferedReader br = Files.newBufferedReader(Paths.get("StaticAnalysisRebuild", "src", "main", "resources", "configuration.csv"))) {
            // CSV文件的分隔符
            String DELIMITER = ",";
            // 按行读取
            String line;
            while ((line = br.readLine()) != null) {
                // 分割
                String[] columns = line.split(DELIMITER);
                // 打印行
                if (columns.length == 4) {
                    defaultConfiguration.put(columns[1], columns[3]);
                }
                System.out.println("Configuration["+ String.join(", ", columns) +"]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public UnnecessarySettingsDetail getUnnecessarySettings(Map<String, String> filePathToMicroserviceName, String systemDirectory, String changed) throws IOException {
        long start = System.currentTimeMillis();
        Map<String, Configuration> filePathToConfiguration;
        if (redisTemplate.opsForValue().get(systemDirectory + "_filePathToConfiguration") != null || "true".equals(changed)) {
            filePathToConfiguration = (Map<String, Configuration>) redisTemplate.opsForValue().get(systemDirectory + "Configuration");
        } else {
            filePathToConfiguration = FileUtils.getConfiguration(filePathToMicroserviceName);
            redisTemplate.opsForValue().set(systemDirectory + "_filePathToConfiguration", filePathToConfiguration);
        }
        UnnecessarySettingsDetail unnecessarySettingsDetail = new UnnecessarySettingsDetail();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            Configuration configuration = filePathToConfiguration.get(filePath);
            System.out.println(microserviceName);
            for (String key : configuration.getItems().keySet()) {
                String value = configuration.get(key);
                if (defaultConfiguration.containsKey(key) && defaultConfiguration.get(key).equals(value)) {
                    unnecessarySettingsDetail.put(microserviceName, key + "=" + value);
                }
            }
        }
        redisTemplate.opsForValue().set(systemDirectory + "_unnecessarySettings_" + start, unnecessarySettingsDetail);
        return unnecessarySettingsDetail;
    }

    public List<UnnecessarySettingsDetail> getUnnecessarySettingsHistory(String systemPath) {
        String key = systemPath + "_unnecessarySettings_*";
        Set<String> keys = redisTemplate.keys(key);
        List<UnnecessarySettingsDetail> unnecessarySettingsDetails = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                unnecessarySettingsDetails.add((UnnecessarySettingsDetail) redisTemplate.opsForValue().get(k));
            }
        }
        return unnecessarySettingsDetails;
    }
}
