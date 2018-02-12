package org.egov.edcr.entity;

import java.math.BigDecimal;

public class Utility {

    private String name;
  //TODO: ADD DIFFERENT UTILITIES BY INDIVIDUAL CLASS NAME. REMOVE UTILITYTYPE.
    
    private UtilityType utilityType;
    private BigDecimal area;
    private BigDecimal distanceFromBuilding;

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

    public BigDecimal getDistanceFromBuilding() {
        return distanceFromBuilding;
    }

    public void setDistanceFromBuilding(BigDecimal distanceFromBuilding) {
        this.distanceFromBuilding = distanceFromBuilding;
    }

    public UtilityType getUtilityType() {
        return utilityType;
    }

    public void setUtilityType(UtilityType utilityType) {
        this.utilityType = utilityType;
    }

}
