package org.egov.edcr.entity.utility;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Measurement;

public class Utility extends Measurement {

    private String name;

    private BigDecimal area;

    private BigDecimal distanceFromBuilding;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public BigDecimal getArea() {
        return area;
    }

    @Override
    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getDistanceFromBuilding() {
        return distanceFromBuilding;
    }

    public void setDistanceFromBuilding(BigDecimal distanceFromBuilding) {
        this.distanceFromBuilding = distanceFromBuilding;
    }

}
