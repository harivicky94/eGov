package org.egov.edcr.entity.utility;

import org.egov.edcr.entity.measurement.Yard;

public class SetBack {
    private Yard frontYard;
    private Yard rearYard;
    private Yard sideYard1;
    private Yard sideYard2;
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
    
}
