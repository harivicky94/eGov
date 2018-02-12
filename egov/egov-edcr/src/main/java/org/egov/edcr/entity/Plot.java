package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

public class Plot {
    
    private List<Measurement> measurements;

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    
}
