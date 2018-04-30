package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.EnumSet;

public class VirtualBuilding {
    private BigDecimal buildingHeight;
    private EnumSet<OccupancyType> occupancies= EnumSet.noneOf(OccupancyType.class);
    private BigDecimal totalBuitUpArea;
    private BigDecimal totalFloorArea;
    private OccupancyType mostRestrictive;
    
    
    
    public BigDecimal getTotalBuitUpArea() {
        return totalBuitUpArea;
    }

    public void setTotalBuitUpArea(BigDecimal totalBuitUpArea) {
        this.totalBuitUpArea = totalBuitUpArea;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public EnumSet<OccupancyType> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(EnumSet<OccupancyType> occupancies) {
        this.occupancies = occupancies;
    }

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public OccupancyType getMostRestrictive() {
        return mostRestrictive;
    }

    public void setMostRestrictive(OccupancyType mostRestrictive) {
        this.mostRestrictive = mostRestrictive;
    }
    
    

}
