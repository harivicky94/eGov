package org.egov.edcr.entity;

import java.math.BigDecimal;

public class PlanInformation {

    private BigDecimal plotArea;
    private String ownerName;
    private String architectName;
    private String occupancy;

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

}
