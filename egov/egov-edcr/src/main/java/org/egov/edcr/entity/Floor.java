package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Floor {

    private String name;

    private BigDecimal area;

    private List<Measurement> rooms;

    private BigDecimal floorHeight;

    //Building_exterior_wall
    private BigDecimal buildingExteriorWall;

    //FAR_deduct
    private BigDecimal farDeduct;

    private BigDecimal coverageDeduct;

    private BigDecimal buildingFootPrint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public List<Measurement> getRooms() {
        return rooms;
    }

    public void setRooms(List<Measurement> rooms) {
        this.rooms = rooms;
    }

    public BigDecimal getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(BigDecimal floorHeight) {
        this.floorHeight = floorHeight;
    }

    public BigDecimal getBuildingExteriorWall() {
        return buildingExteriorWall;
    }

    public BigDecimal getFarDeduct() {
        return farDeduct;
    }

    public void setFarDeduct(BigDecimal farDeduct) {
        this.farDeduct = farDeduct;
    }

    public BigDecimal getCoverageDeduct() {
        return coverageDeduct;
    }

    public void setCoverageDeduct(BigDecimal coverageDeduct) {
        this.coverageDeduct = coverageDeduct;
    }

    public BigDecimal getBuildingFootPrint() {
        return buildingFootPrint;
    }

    public void setBuildingFootPrint(BigDecimal buildingFootPrint) {
        this.buildingFootPrint = buildingFootPrint;
    }
}
