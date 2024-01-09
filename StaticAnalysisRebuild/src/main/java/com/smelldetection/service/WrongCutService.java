package com.smelldetection.service;

import com.smelldetection.entity.item.ServiceCutItem;
import com.smelldetection.entity.smell.detail.WrongCutDetail;
import com.smelldetection.utils.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Service
public class WrongCutService {

    /**
     * 获取系统错误划分的服务
     * @param filePathToMicroserviceName 微服务模块路径与微服务名的映射
     */
    public WrongCutDetail getWrongCut(Map<String, String> filePathToMicroserviceName) throws IOException, DocumentException, XmlPullParserException {
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
        WrongCutDetail wrongCutDetail = new WrongCutDetail();
        for (ServiceCutItem serviceCut : systemServiceCuts) {
            if(Math.abs(serviceCut.getEntityCount() - systemAverageEntityCount) >= 3 * std
                    && (filePathToMicroserviceName.size() != 1) && std != 0) {
                if (serviceCut.getEntityCount() > systemAverageEntityCount && serviceCut.getEntityCount() <= 2)
                    continue;
                wrongCutDetail.getWrongCutMicroservices().put(serviceCut.getMicroserviceName(), serviceCut);
            }
        }
        return wrongCutDetail;
    }
}
