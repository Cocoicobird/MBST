package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ServiceCutItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 错误划分
 */
@Data
public class WrongCutDetail {
    private Map<String, ServiceCutItem> wrongCutMicroservices;

    public WrongCutDetail() {
        this.wrongCutMicroservices = new HashMap<>();
    }
}
