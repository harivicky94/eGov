package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class FloorUnit extends Measurement{

    private List<Measurement> deductions= new ArrayList<>();
    private BigDecimal totalUnitDeduction;
    
    
    public BigDecimal getTotalUnitDeduction() {
        return totalUnitDeduction;
    }

    public void setTotalUnitDeduction(BigDecimal totalDeduction) {
        this.totalUnitDeduction = totalDeduction;
    }

    public List<Measurement> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<Measurement> deductions) {
        this.deductions = deductions;
    }
    
    
}
