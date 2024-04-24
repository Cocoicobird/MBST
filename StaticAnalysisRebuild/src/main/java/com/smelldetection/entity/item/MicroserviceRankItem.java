package com.smelldetection.entity.item;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class MicroserviceRankItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;

    public MicroserviceRankItem() {
        this.name = "";
    }

    public MicroserviceRankItem(String name) {
        this.name = name;
    }
}
