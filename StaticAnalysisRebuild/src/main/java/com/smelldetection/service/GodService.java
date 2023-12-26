package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCutItem;
import com.smelldetection.entity.smell.detail.GodServiceDetail;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class GodService {

    public GodServiceDetail getGodService(Map<String, String> filePathToMicroserviceName) throws IOException {
        List<ServiceCutItem> systemServiceCuts = FileUtils.getSystemServiceCuts(filePathToMicroserviceName);
        double systemTotalEntityCount = 0;
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            systemTotalEntityCount += serviceCut.getEntityCount();
        }
        double systemAverageEntityCount = systemTotalEntityCount / systemServiceCuts.size();
        double sumOfSquares = 0;
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            sumOfSquares += Math.pow(serviceCut.getEntityCount() - systemAverageEntityCount, 2);
        }
        double std = Math.sqrt(sumOfSquares / systemServiceCuts.size());
        GodServiceDetail godServiceDetail = new GodServiceDetail();
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            if(Math.abs(serviceCut.getEntityCount() - systemAverageEntityCount) >= 3 * std
                    && (systemServiceCuts.size() != 1) && std != 0) {
                if (serviceCut.getEntityCount() > systemAverageEntityCount && serviceCut.getEntityCount() <= 2)
                    continue;
                godServiceDetail.getGodServices().put(serviceCut.getMicroserviceName(), serviceCut);
            }
        }
        return godServiceDetail;
    }
}
