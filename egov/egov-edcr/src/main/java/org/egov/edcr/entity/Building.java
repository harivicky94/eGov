package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Building extends Measurement {

    private BigDecimal buildingHeight;

    private BigDecimal buildingTopMostHeight;

    private BigDecimal totalFloorArea;

    private Measurement exteriorWall;
   
    private Measurement shade;
    
    private List<Measurement> openStairs=new ArrayList<>();

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
    
    private List<Floor> floors=new ArrayList<>();

    private BigDecimal floorsAboveGround;

    private BigDecimal distanceFromBuildingFootPrintToRoadEnd;
    
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

    public Measurement getShade() {
        return shade;
    }

    public void setShade(Measurement shade) {
        this.shade = shade;
    }

    public List<Measurement> getOpenStairs() {
        return openStairs;
    }

    public void setOpenStairs(List<Measurement> openStairs) {
        this.openStairs = openStairs;
    }

    @Override
    public String toString() {
        String newLine="\n";
        StringBuilder str=new StringBuilder();
        str.append("Building :")
        .append(newLine)
        .append("buildingHeight:").append(this.buildingHeight).append(newLine)
        .append("totalFloorArea:").append(this.totalFloorArea).append(newLine)
        .append("Coverage:").append(this.coverage).append(newLine)
        .append("totalFloors:").append(this.totalFloors).append(newLine)
        .append("floorsAboveGround:").append(this.floorsAboveGround).append(newLine)
        .append("maxFloor").append(this.maxFloor).append(newLine)
        .append("area").append(this.area).append(newLine)
        .append("Floors Count:").append(this.floors.size()).append(newLine)
        .append("Exterior wall").append(this.exteriorWall).append(newLine)
        .append("Open Stair count:").append(this.openStairs.size()).append(newLine)
        .append("Floors:").append(this.floors).append(newLine)
        .append("open stairs").append(this.openStairs);
        return str.toString();
    }

    public BigDecimal getDistanceFromBuildingFootPrintToRoadEnd() {
        return distanceFromBuildingFootPrintToRoadEnd;
    }

    public void setDistanceFromBuildingFootPrintToRoadEnd(BigDecimal distanceFromBuildingFootPrintToRoadEnd) {
        this.distanceFromBuildingFootPrintToRoadEnd = distanceFromBuildingFootPrintToRoadEnd;
    }
    

}
