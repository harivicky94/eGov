package org.egov.edcr.entity.utility;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Yard;

public class SetBack {
    private Yard frontYard;
    private Yard rearYard;
    private Yard sideYard1;
    private Yard sideYard2;
    private Integer level;
    private BigDecimal height;
    
    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public BigDecimal getHeight() {
        return height;
    }
    public void setHeight(BigDecimal height) {
        this.height = height;
    }
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
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SetBack other = (SetBack) obj;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        return true;
    }
    
    
}
