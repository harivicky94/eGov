package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.measurement.Measurement;

public class Parking {

    private List<Measurement> parkingSlots= new ArrayList<>();
    private List<FloorUnit> floorUnits= new ArrayList<>();//check is it required here or in plan detail ?
   
    
}
