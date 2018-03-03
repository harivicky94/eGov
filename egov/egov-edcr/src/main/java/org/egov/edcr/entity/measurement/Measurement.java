package org.egov.edcr.entity.measurement;

import java.math.BigDecimal;

import org.kabeja.dxf.DXFLWPolyline;

public class Measurement {

    protected Boolean presentInDxf = false;

    protected BigDecimal minimumDistance;

    protected BigDecimal length;

    protected BigDecimal width;

    protected BigDecimal height;

    protected BigDecimal mean;

    protected BigDecimal area;

    protected DXFLWPolyline polyLine;

    public void setMinimumDistance(BigDecimal minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public Boolean getPresentInDxf() {
        return presentInDxf;
    }

    public void setPresentInDxf(Boolean present) {
        presentInDxf = present;
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

    public DXFLWPolyline getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(DXFLWPolyline polyLine) {
        this.polyLine = polyLine;
    }

    @Override
    public String toString() {
        return "Measurement : presentInDxf=" + presentInDxf +"\n polyLine Count="  + "]";
    }

}
