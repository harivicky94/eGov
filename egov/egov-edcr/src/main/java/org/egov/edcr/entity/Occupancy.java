package org.egov.edcr.entity;

import java.math.BigDecimal;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFLWPolyline;

public class Occupancy extends Measurement {
    private OccupancyType type;
    private BigDecimal deduction;
    private BigDecimal noOfSeats;
    protected DXFLWPolyline deductionPolyLine;
    
    
    public DXFLWPolyline getDeductionPolyLine() {
        return deductionPolyLine;
    }
    public void setDeductionPolyLine(DXFLWPolyline deductionPolyLine) {
        this.deductionPolyLine = deductionPolyLine;
    }
    public OccupancyType getType() {
        return type;
    }
    public void setType(OccupancyType type) {
        this.type = type;
    }
   
    public BigDecimal getDeduction() {
        return deduction;
    }
    public void setDeduction(BigDecimal deduction) {
        this.deduction = deduction;
    }
    public BigDecimal getNoOfSeats() {
        return noOfSeats;
    }
    public void setNoOfSeats(BigDecimal noOfSeats) {
        this.noOfSeats = noOfSeats;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        Occupancy other = (Occupancy) obj;
        if (type != other.type)
            return false;
        return true;
    }
   
    

}
