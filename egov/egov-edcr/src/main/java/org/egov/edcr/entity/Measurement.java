package org.egov.edcr.entity;

import java.math.BigDecimal;

public class Measurement {
//TODO: DEFINE MEASUREMENTTYPE OBJECT OUTSIDE AND EXTEND MEASUREMENT.
    private MeasurementType measurementType;

    private BigDecimal minimumDistance;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal mean;
    private BigDecimal area;
    //DXFpolyline
    
    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    public BigDecimal getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(BigDecimal minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getMean() {
        return mean;
    }

    public void setMean(BigDecimal mean) {
        this.mean = mean;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

}
