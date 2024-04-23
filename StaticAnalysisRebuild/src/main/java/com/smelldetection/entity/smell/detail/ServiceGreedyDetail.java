package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceGreedyItem;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 整个系统的服务贪婪情况
 */
@Data
public class ServiceGreedyDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    // 是否存在服务贪婪
    private boolean existed;
    private List<ServiceGreedyItem> greedyServices;

    public ServiceGreedyDetail() {
        this.greedyServices = new ArrayList<>();
    }

    public void addGreedyService(ServiceGreedyItem serviceGreedyItem) {
        this.greedyServices.add(serviceGreedyItem);
    }
}
