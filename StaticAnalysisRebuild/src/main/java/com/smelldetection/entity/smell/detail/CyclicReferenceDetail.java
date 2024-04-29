package com.smelldetection.entity.smell.detail;

import com.smelldetection.entity.item.CyclicReferenceItem;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cocoicobird
 * @version 1.0
 */
@Data
public class CyclicReferenceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String time;
    public Map<String, CyclicReferenceItem> cyclicReferences;
    public CyclicReferenceDetail(){
        this.cyclicReferences = new HashMap<>();
    }
    public void addCyclicReference(String microserviceName, String superClass, String subClass){
        if (cyclicReferences.get(subClass) == null){
            cyclicReferences.put(superClass, new CyclicReferenceItem(microserviceName, superClass));
        }
        cyclicReferences.get(superClass).addSubClass(subClass);
    }
}
