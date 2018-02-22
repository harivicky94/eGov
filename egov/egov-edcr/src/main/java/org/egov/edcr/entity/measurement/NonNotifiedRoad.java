package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;

public class NonNotifiedRoad extends Measurement {
    private BigDecimal shortestDistanceToRoad;

    public BigDecimal getShortestDistanceToRoad() {
        return shortestDistanceToRoad;
    }

    public void setShortestDistanceToRoad(BigDecimal shortestDistanceToRoad) {
        this.shortestDistanceToRoad = shortestDistanceToRoad;
    }
}