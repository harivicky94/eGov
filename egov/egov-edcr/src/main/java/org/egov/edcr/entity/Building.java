package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Building extends Measurement {

    private BigDecimal buildingHeight;

    private BigDecimal buildingTopMostHeight;

    private BigDecimal totalFloorArea;

    private Measurement exteriorWall;

    private BigDecimal far;

    private BigDecimal coverage;
    /*
     * Maximum number of floors
     */
    private BigDecimal maxFloor;
    /*
     * Total number of floors including celler
     */
    private BigDecimal totalFloors;
    private List<Floor> floors;

    private BigDecimal floorsAboveGround;

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

    public BigDecimal getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(BigDecimal totalFloors) {
        this.totalFloors = totalFloors;
    }

    public BigDecimal getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(BigDecimal maxFloor) {
        this.maxFloor = maxFloor;
    }

    public BigDecimal getBuildingTopMostHeight() {
        return buildingTopMostHeight;
    }

    public void setBuildingTopMostHeight(BigDecimal buildingHeightTopMost) {
        buildingTopMostHeight = buildingHeightTopMost;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public BigDecimal getFar() {
        return far;
    }

    public void setFar(BigDecimal far) {
        this.far = far;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public Measurement getExteriorWall() {
        return exteriorWall;
    }

    public void setExteriorWall(Measurement exteriorWall) {
        this.exteriorWall = exteriorWall;
    }

    public BigDecimal getFloorsAboveGround() {
        return floorsAboveGround;
    }

    public void setFloorsAboveGround(BigDecimal floorsAboveGround) {
        this.floorsAboveGround = floorsAboveGround;
    }

}
