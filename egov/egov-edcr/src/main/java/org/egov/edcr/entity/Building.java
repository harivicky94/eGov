package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.BuildingArea;

public class Building {

    private BigDecimal buildingHeight;
    private List<Floor> floors;
    private BuildingArea buildingArea;
    
    
    public BuildingArea getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(BuildingArea buildingArea) {
        this.buildingArea = buildingArea;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

}
