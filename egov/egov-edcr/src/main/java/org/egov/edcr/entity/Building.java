package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Building extends Measurement {

    private BigDecimal buildingHeight;
    /*
     * Maximum number of floors
     */
    private BigDecimal maxFloor; 
    /*
     * Total number of floors including celler
     */
    private BigDecimal totalFloors;
    private List<Floor> floors;

    private WasteDisposal wasteDisposal;

    public WasteDisposal getWasteDisposal() {
        return wasteDisposal;
    }

    public void setWasteDisposal(WasteDisposal wasteDisposal) {
        this.wasteDisposal = wasteDisposal;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public BigDecimal getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(BigDecimal totalFloors) {
        this.totalFloors = totalFloors;
    }

    public BigDecimal getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(BigDecimal maxFloor) {
        this.maxFloor = maxFloor;
    }

}
