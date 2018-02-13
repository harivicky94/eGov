package org.egov.edcr.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.utility.Utility;

/*All the details extracted from the plan are referred in this object*/
public class PlanDetail {

	private List<Utility> utilities;
	private PlanInformation planInformation;
	private Plot landDetail;
	private Building buildingDetail;
	public ReportOutput reportOutput= new ReportOutput();
	private Boolean edcrPassed=false;

	private List<NonNotifiedRoad> nonNotifiedRoads;
	private List<NotifiedRoad> notifiedRoads;

	private HashMap<String, String> errors = new HashMap<String, String>();
	private HashMap<String, String> generalInformation = new HashMap<String, String>();

	
	
	

    public Boolean getEdcrPassed() {
        return edcrPassed;
    }

    public void setEdcrPassed(Boolean edcrPassed) {
        this.edcrPassed = edcrPassed;
    }

    public List<NonNotifiedRoad> getNonNotifiedRoads() {
        return nonNotifiedRoads;
    }

    public void setNonNotifiedRoads(List<NonNotifiedRoad> nonNotifiedRoads) {
        this.nonNotifiedRoads = nonNotifiedRoads;
    }

    public List<NotifiedRoad> getNotifiedRoads() {
        return notifiedRoads;
    }

    public void setNotifiedRoads(List<NotifiedRoad> notifiedRoads) {
        this.notifiedRoads = notifiedRoads;
    }

    public HashMap<String, String> getGeneralInformation() {
		return generalInformation;
	}

	public void setGeneralInformation(HashMap<String, String> generalInformation) {
		this.generalInformation = generalInformation;
	}

	public void addGeneralInformation(Map<String, String> generalInformation) {
		if (generalInformation != null)
			getGeneralInformation().entrySet().add((Entry<String, String>) generalInformation);
	}

	public void addErrors(Map<String, String> errors) {
		if (errors != null)
			getErrors().entrySet().add((Entry<String, String>) errors);
	}

	public HashMap<String, String> getErrors() {
		return errors;
	}

	public void setErrors(HashMap<String, String> errors) {
		this.errors = errors;
	}

	public List<Utility> getUtilities() {
		return utilities;
	}

	public void setUtilities(List<Utility> utilities) {
		this.utilities = utilities;
	}

	
	public ReportOutput getReportOutput() {
        return reportOutput;
    }

    public void setReportOutput(ReportOutput reportOutput) {
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
