package org.egov.edcr.entity;

import org.egov.edcr.entity.measurement.Measurement;

import java.math.BigDecimal;
import java.util.List;

public class Floor {

    private String name;

    private BigDecimal area;

    private List<Measurement> rooms;

    private BigDecimal floorHeight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public List<Measurement> getRooms() {
        return rooms;
    }

    public void setRooms(List<Measurement> rooms) {
        this.rooms = rooms;
    }

    public BigDecimal getFloorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(BigDecimal floorHeight) {
        this.floorHeight = floorHeight;
    }
}
