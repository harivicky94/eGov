package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

/*Used to determine the rules to be validated for a building plan*/
public class PlanRule {

    private String service;

    private BigDecimal plotArea;

    private String occupancy;

    private Double noOfFloors;

    private BigDecimal heightOfBuilding;

    private List<String> rules;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public BigDecimal getHeightOfBuilding() {
        return heightOfBuilding;
    }

    public void setHeightOfBuilding(BigDecimal heightOfBuilding) {
        this.heightOfBuilding = heightOfBuilding;
    }

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

    public Double getNoOfFloors() {
        return noOfFloors;
    }

    public void setNoOfFloors(Double noOfFloors) {
        this.noOfFloors = noOfFloors;
    }
}
