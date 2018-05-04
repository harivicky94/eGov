package org.egov.edcr.entity.utility;

import org.egov.edcr.entity.measurement.Measurement;
import org.kabeja.dxf.DXFCircle;

public class WellUtility extends Measurement {

    protected DXFCircle circle;

    public DXFCircle getCircle() {
        return circle;
    }

    public void setCircle(DXFCircle circle) {
        this.circle = circle;
    }
    
}
