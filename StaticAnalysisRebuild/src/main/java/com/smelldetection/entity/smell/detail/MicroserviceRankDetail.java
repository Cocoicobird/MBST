package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.MicroserviceRankItem;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class MicroserviceRankDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double weight;
    private List<MicroserviceRankItem> pointers;

    public MicroserviceRankDetail() {
        this.name = "";
        this.weight = 0.0;
        this.pointers = new ArrayList<>();
    }

    public void addPointers(List<String> pointers) {
        for (String pointer : pointers)
            this.pointers.add(new MicroserviceRankItem(pointer));
    }
}
