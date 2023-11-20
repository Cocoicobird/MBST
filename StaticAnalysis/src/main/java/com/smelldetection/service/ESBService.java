package com.smelldetection.service;

import com.smelldetection.base.context.ESBServiceContext;
import com.smelldetection.base.item.RequestItem;
import com.smelldetection.base.utils.ESBParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: xyc
 * @date: 2023-02-24 20:50
 */
@Service
public class ESBService {
    @Autowired
    public ESBParserUtils esbParserUtils;

    /**
     * 根据服务调用情况来确定是否为 ESBUsage
     * @param request
     * @return
     */
    public ESBServiceContext getESBServices(RequestItem request) {
        String path = request.getServicesPath();
        ESBServiceContext esbServiceContext = new ESBServiceContext();
        esbServiceContext.setResult(esbParserUtils.ESBUsageAnalysis(path));
        for (String service : esbServiceContext.getResult().keySet()) {
            if (esbServiceContext.getResult().get(service).getIsESBUsage()) {
                esbServiceContext.setStatus(true);
                break;
            }
        }

        return esbServiceContext;

    }
}
