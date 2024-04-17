package com.smelldetection.service;

import com.smelldetection.entity.item.HardCodeItem;
import com.smelldetection.entity.smell.detail.HardCodeDetail;
import com.smelldetection.utils.FileUtils;
import com.smelldetection.utils.HardCodeUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 硬编码
 */
@Service
public class HardCodeService {

    public HardCodeDetail getHardCode(Map<String, String> filePathToMicroserviceName) throws IOException {
        HardCodeDetail hardCodeDetail = new HardCodeDetail();
        for (String filePath : filePathToMicroserviceName.keySet()) {
            String microserviceName = filePathToMicroserviceName.get(filePath);
            List<String> javaFiles = FileUtils.getJavaFiles(filePath);
            List<HardCodeItem> hardCodeItems = new ArrayList<>();
            HardCodeUtils.resolveHardCodeFromJavaFiles(javaFiles, hardCodeItems);
            hardCodeDetail.addAll(microserviceName, hardCodeItems);
        }
        return hardCodeDetail;
    }

}
