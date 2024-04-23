package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class ChattyServiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer maxCallNumber;
    private Integer entityNumber;
}
