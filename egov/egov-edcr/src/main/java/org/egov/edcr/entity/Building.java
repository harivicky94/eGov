package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

public class Building {

    private BigDecimal buildingHeight;
    private List<Floor> floors;

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
