package org.egov.edcr.entity;

import org.egov.edcr.entity.measurement.Measurement;

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
