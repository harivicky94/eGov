package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.BuildingArea;
import org.egov.edcr.entity.measurement.ShortestDistanceToRoad;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Building {

    private BigDecimal buildingHeight;
    private List<Floor> floors;
    private BuildingArea buildingArea;

    private WasteDisposal wasteDisposal;
    private ShortestDistanceToRoad shortestDistanceToRoad;
    
    
    
    public ShortestDistanceToRoad getShortestDistanceToRoad() {
        return shortestDistanceToRoad;
    }

    public void setShortestDistanceToRoad(ShortestDistanceToRoad shortestDistanceToRoad) {
        this.shortestDistanceToRoad = shortestDistanceToRoad;
    }

    public WasteDisposal getWasteDisposal() {
        return wasteDisposal;
    }

    public void setWasteDisposal(WasteDisposal wasteDisposal) {
        this.wasteDisposal = wasteDisposal;
    }

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
