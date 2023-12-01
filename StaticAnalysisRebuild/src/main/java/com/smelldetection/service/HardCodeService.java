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
 */
@Service
public class HardCodeService {

    public List<HardCodeDetail> getHardCode(List<String> services) throws IOException {
        List<HardCodeDetail> hardCodeDetails = new ArrayList<>();
        for (String service : services) {
            String microserviceName = "";
            List<String> javaFiles = FileUtils.getJavaFiles(service);
            List<HardCodeItem> hardCodeItems = new ArrayList<>();
            HardCodeUtils.resolveHardCodeFromJavaFiles(javaFiles, hardCodeItems);
            HardCodeDetail hardCodeDetail = new HardCodeDetail();
            hardCodeDetail.setMicroserviceName(microserviceName);
            hardCodeDetail.setHardCodes(hardCodeItems);
            hardCodeDetails.add(hardCodeDetail);
        }
        return hardCodeDetails;
    }

}
