package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.HardCodeItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 * @description 某一微服务的硬编码信息
 */
@Data
public class HardCodeDetail {
    private Map<String, List<HardCodeItem>> hardCodes;

    public HardCodeDetail() {
        this.hardCodes = new HashMap<>();
    }

    public void add(String microserviceName, HardCodeItem hardCodeItem) {
        if (!this.hardCodes.containsKey(microserviceName)) {
            this.hardCodes.put(microserviceName, new ArrayList<>());
        }
        this.hardCodes.get(microserviceName).add(hardCodeItem);
    }

    public void addAll(String microserviceName, List<HardCodeItem> hardCodeItems) {
        if (!this.hardCodes.containsKey(microserviceName)) {
            this.hardCodes.put(microserviceName, new ArrayList<>());
        }
        this.hardCodes.get(microserviceName).addAll(hardCodeItems);
    }
}
