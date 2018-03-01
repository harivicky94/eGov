package org.egov.edcr.entity;

import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.Yard;

public class Plot extends Measurement {

    public static final String PLOT_BOUNDARY = "PLOT_BOUNDARY";
    private Yard frontYard;
    private Yard rearYard;
    private Yard sideYard1;
    private Yard sideYard2;

    private Yard bsmtFrontYard;
    private Yard bsmtRearYard;
    private Yard bsmtSideYard1;
    private Yard bsmtSideYard2;

    public Yard getFrontYard() {
        return frontYard;
    }

    public void setFrontYard(Yard frontYard) {
        this.frontYard = frontYard;
    }

    public Yard getRearYard() {
        return rearYard;
    }

    public void setRearYard(Yard rearYard) {
        this.rearYard = rearYard;
    }

    public Yard getSideYard1() {
        return sideYard1;
    }

    public void setSideYard1(Yard sideYard1) {
        this.sideYard1 = sideYard1;
    }

    public Yard getSideYard2() {
        return sideYard2;
    }

    public void setSideYard2(Yard sideYard2) {
        this.sideYard2 = sideYard2;
    }

    public Yard getBsmtFrontYard() {
        return bsmtFrontYard;
    }

    public void setBsmtFrontYard(Yard bsmtFrontYard) {
        this.bsmtFrontYard = bsmtFrontYard;
    }

    public Yard getBsmtRearYard() {
        return bsmtRearYard;
    }

    public void setBsmtRearYard(Yard bsmtRearYard) {
        this.bsmtRearYard = bsmtRearYard;
    }

    public Yard getBsmtSideYard1() {
        return bsmtSideYard1;
    }

    public void setBsmtSideYard1(Yard bsmtSideYard1) {
        this.bsmtSideYard1 = bsmtSideYard1;
    }

    public Yard getBsmtSideYard2() {
        return bsmtSideYard2;
    }

    public void setBsmtSideYard2(Yard bsmtSideYard2) {
        this.bsmtSideYard2 = bsmtSideYard2;
    }
}
