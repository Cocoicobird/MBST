package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.ChattyServiceItem;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ChattyServiceDetail {
    private boolean status;
    private String microserviceName;
    private Map<String, ChattyServiceItem> chattyServices;

    public ChattyServiceDetail() {
        this.chattyServices = new HashMap<>();
    }

    public void put(String microserviceName, ChattyServiceItem chattyServiceItem) {
        this.chattyServices.put(microserviceName, chattyServiceItem);
    }
}
