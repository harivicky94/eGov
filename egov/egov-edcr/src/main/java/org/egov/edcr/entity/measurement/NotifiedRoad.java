package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;

public class NotifiedRoad extends Measurement {

    private BigDecimal shortestDistanceToRoad;
    private BigDecimal distanceFromCenterToPlot;

    public BigDecimal getDistanceFromCenterToPlot() {
        return distanceFromCenterToPlot;
    }

    public void setDistanceFromCenterToPlot(BigDecimal distanceFromCenterToPlot) {
        this.distanceFromCenterToPlot = distanceFromCenterToPlot;
    }

    public BigDecimal getShortestDistanceToRoad() {
        return shortestDistanceToRoad;
    }

    public void setShortestDistanceToRoad(BigDecimal shortestDistanceToRoad) {
        this.shortestDistanceToRoad = shortestDistanceToRoad;
    }
}