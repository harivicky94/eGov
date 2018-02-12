package org.egov.edcr.entity;

import org.egov.edcr.entity.measurement.FrontYard;
import org.egov.edcr.entity.measurement.PlotArea;
import org.egov.edcr.entity.measurement.RearYard;
import org.egov.edcr.entity.measurement.SideYard1;
import org.egov.edcr.entity.measurement.SideYard2;

public class Plot {
    private FrontYard frontYard;
    private RearYard rearYard;
    private PlotArea plotArea;
    private SideYard1 sideYard1;
    private SideYard2 sideYard2;

    public FrontYard getFrontYard() {
        return frontYard;
    }

    public void setFrontYard(FrontYard frontYard) {
        this.frontYard = frontYard;
    }

    public RearYard getRearYard() {
        return rearYard;
    }

    public void setRearYard(RearYard rearYard) {
        this.rearYard = rearYard;
    }

    public PlotArea getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(PlotArea plotArea) {
        this.plotArea = plotArea;
    }

    public SideYard1 getSideYard1() {
        return sideYard1;
    }

    public void setSideYard1(SideYard1 sideYard1) {
        this.sideYard1 = sideYard1;
    }

    public SideYard2 getSideYard2() {
        return sideYard2;
    }

    public void setSideYard2(SideYard2 sideYard2) {
        this.sideYard2 = sideYard2;
    }

}
