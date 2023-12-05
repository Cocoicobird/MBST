package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class GodServiceDetail {
    List<String> godServices;

    public GodServiceDetail() {
        this.godServices = new ArrayList<>();
    }
}
