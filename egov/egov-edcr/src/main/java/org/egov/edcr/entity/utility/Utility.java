package org.egov.edcr.entity.utility;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.WasteDisposal;

public class Utility extends Measurement {

    private List<WasteDisposal> wasteDisposalUnits = new ArrayList<>();
    
    
    

    public List<WasteDisposal> getWasteDisposalUnits() {
        return wasteDisposalUnits;
    }

    public void setWasteDisposalUnits(List<WasteDisposal> wasteDisposalUnits) {
        this.wasteDisposalUnits = wasteDisposalUnits;
    }

    public void addWasteDisposal(WasteDisposal wasteDisposal) {
        wasteDisposalUnits.add(wasteDisposal);

    }

}
