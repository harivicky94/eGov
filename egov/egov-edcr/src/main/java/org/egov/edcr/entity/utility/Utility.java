package org.egov.edcr.entity.utility;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Utility extends Measurement {

    private List<WasteDisposal> wasteDisposalUnits = new ArrayList<>();
    
    private List<WellUtility> wells= new ArrayList<>();
    private List<RoadOutput> wellDistance=new ArrayList<>();
    private List<RainWaterHarvesting> rainWaterHarvest= new ArrayList<>();
    private List<Solar> solar= new ArrayList<>();
    private BigDecimal raintWaterHarvestingTankCapacity;

    public BigDecimal getRaintWaterHarvestingTankCapacity() {
        return raintWaterHarvestingTankCapacity;
    }
    public void setRaintWaterHarvestingTankCapacity(BigDecimal raintWaterHarvestingTankCapacity) {
        this.raintWaterHarvestingTankCapacity = raintWaterHarvestingTankCapacity;
    }
    public void addRainWaterHarvest(RainWaterHarvesting rwh) {
        rainWaterHarvest.add(rwh);

    }
    public void addSolar(Solar solarsystem) {
        solar.add(solarsystem);

    }
    public List<RainWaterHarvesting> getRainWaterHarvest() {
        return rainWaterHarvest;
    }

    public void setRainWaterHarvest(List<RainWaterHarvesting> rainWaterHarvest) {
        this.rainWaterHarvest = rainWaterHarvest;
    }

    public List<Solar> getSolar() {
        return solar;
    }

    public void setSolar(List<Solar> solar) {
        this.solar = solar;
    }

    public List<WasteDisposal> getWasteDisposalUnits() {
        return wasteDisposalUnits;
    }

    public void setWasteDisposalUnits(List<WasteDisposal> wasteDisposalUnits) {
        this.wasteDisposalUnits = wasteDisposalUnits;
    }

    public void addWasteDisposal(WasteDisposal wasteDisposal) {
        wasteDisposalUnits.add(wasteDisposal);

    }

    public List<WellUtility> getWells() {
        return wells;
    }

    public void setWells(List<WellUtility> wells) {
        this.wells = wells;
    }

    public List<RoadOutput> getWellDistance() {
        return wellDistance;
    }

    public void setWellDistance(List<RoadOutput> wellDistance) {
        this.wellDistance = wellDistance;
    }
    public void addWells(WellUtility wellUtility) {
        wells.add(wellUtility);

    }
}
