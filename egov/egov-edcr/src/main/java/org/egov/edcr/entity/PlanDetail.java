package org.egov.edcr.entity;

import org.egov.edcr.entity.utility.Utility;

import java.util.HashMap;
import java.util.List;

/*All the details extracted from the plan are referred in this object*/
public class PlanDetail {
   

    private List<Utility> utilities;
    private PlanInformation planInformation;
    private Plot landDetail;
    private Building buildingDetail;
    private HashMap<String, HashMap<String,Object>> reportOutput;

    
    
    public List<Utility> getUtilities() {
        return utilities;
    }
    public void setUtilities(List<Utility> utilities) {
        this.utilities = utilities;
    }
    public HashMap<String, HashMap<String, Object>> getReportOutput() {
        return reportOutput;
    }
    public void setReportOutput(HashMap<String, HashMap<String, Object>> reportOutput) {
        this.reportOutput = reportOutput;
    }
    public PlanInformation getPlanInformation() {
        return planInformation;
    }
    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }
    public Plot getLandDetail() {
        return landDetail;
    }
    public void setLandDetail(Plot landDetail) {
        this.landDetail = landDetail;
    }
    public Building getBuildingDetail() {
        return buildingDetail;
    }
    public void setBuildingDetail(Building buildingDetail) {
        this.buildingDetail = buildingDetail;
    }
    
    
   
}
