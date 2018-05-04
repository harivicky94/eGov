package org.egov.edcr.entity.utility;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Utility extends Measurement {

    private List<WasteDisposal> wasteDisposalUnits = new ArrayList<>();
    
    private List<WellUtility> wells= new ArrayList<>();
    private List<RoadOutput> wellDistance=new ArrayList<>();

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
