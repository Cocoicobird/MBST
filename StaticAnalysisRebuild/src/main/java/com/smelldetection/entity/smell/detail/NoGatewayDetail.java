package com.smelldetection.entity.smell.detail;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class NoGatewayDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status; // true 表示无网关
    private String time;
}
