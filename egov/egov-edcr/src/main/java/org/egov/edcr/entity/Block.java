package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Block extends Measurement {
    
    private Building building=new Building();
    private String name;
    private String number;


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

 

}
