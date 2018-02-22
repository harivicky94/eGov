package org.egov.edcr.entity;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Measurement;

public class ElectricLine extends Measurement {
    private BigDecimal verticalDistance;
    private BigDecimal horizontalDistance;
    private BigDecimal voltage;

    public BigDecimal getVerticalDistance() {
        return verticalDistance;
    }

    public void setVerticalDistance(BigDecimal verticalDistance) {
        this.verticalDistance = verticalDistance;
    }

    public BigDecimal getHorizontalDistance() {
        return horizontalDistance;
    }

    public void setHorizontalDistance(BigDecimal horizontalDistance) {
        this.horizontalDistance = horizontalDistance;
    }

    public BigDecimal getVoltage() {
        return voltage;
    }

    public void setVoltage(BigDecimal voltage) {
        this.voltage = voltage;
    }

}
