package org.egov.edcr.entity.measurement;

public class Yard extends Measurement {
    public static final String NAME = "FRONT_YARD";

    @Override
    public String toString() {
        return "Yard : presentInDxf=" + presentInDxf + ", minimumDistance=" + minimumDistance + ", mean=" + mean + ", area=" + area
                + "";
    }

}
