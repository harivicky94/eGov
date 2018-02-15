package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;

public class Measurement {

    private Boolean presentInDxf=false;
    private BigDecimal minimumDistance;

    private BigDecimal length;

    private BigDecimal width;

    private BigDecimal height;

    private BigDecimal mean;

    private BigDecimal area;
    //DXFpolyline
    
    
    public void setMinimumDistance(BigDecimal minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public Boolean getPresentInDxf() {
        return presentInDxf;
    }

    public void setPresentInDxf(Boolean present) {
        this.presentInDxf = present;
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

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getMinimumDistance() {
        return minimumDistance;
    }

}
