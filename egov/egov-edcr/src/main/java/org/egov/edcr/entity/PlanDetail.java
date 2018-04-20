package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.utility.Utility;

/*All the details extracted from the plan are referred in this object*/
public class PlanDetail {

    private Utility utility = new Utility();
    private PlanInformation planInformation;
    private Plot plot;
    private List<Block>  blocks=new ArrayList<>();
    private VirtualBuilding virtualBuilding;
    private Building building;

    public ReportOutput reportOutput = new ReportOutput();
    private Boolean edcrPassed = false;
    private ElectricLine electricLine;
    private List<NonNotifiedRoad> nonNotifiedRoads = new ArrayList<>();
    private List<NotifiedRoad> notifiedRoads = new ArrayList<>();

    private List<CulDeSacRoad> culdeSacRoads = new ArrayList<>();
    private List<Lane> laneRoads = new ArrayList<>();
    
    private HashMap<String, String> errors = new HashMap<>();
    private HashMap<String, String> noObjectionCertificates= new HashMap<>();

    private HashMap<String, String> generalInformation = new HashMap<>();
    private Basement basement;
    private List<Measurement> parkingSlots= new ArrayList<>();
    private List<FloorUnit> floorUnits= new ArrayList<>();
    

 
    public List<Block> getBlocks() {
        return blocks;
    }
    public Block getBlockByName(String blockName)
    {
        for(Block block: getBlocks())
        {
            if(block.getName().equalsIgnoreCase(blockName))
                return block;
        }
       return null;     
    }
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public HashMap<String, String> getNoObjectionCertificates() {
        return noObjectionCertificates;
    }

    public void setNoObjectionCertificates(HashMap<String, String> noObjectionCertificates) {
        this.noObjectionCertificates = noObjectionCertificates;
    }

    public List<CulDeSacRoad> getCuldeSacRoads() {
        return culdeSacRoads;
    }

    public void setCuldeSacRoads(List<CulDeSacRoad> culdeSacRoads) {
        this.culdeSacRoads = culdeSacRoads;
    }

    public List<Lane> getLaneRoads() {
        return laneRoads;
    }

    public void setLaneRoads(List<Lane> laneRoads) {
        this.laneRoads = laneRoads;
    }

    public List<FloorUnit> getFloorUnits() {
        return floorUnits;
    }

    public void setFloorUnits(List<FloorUnit> floorUnits) {
        this.floorUnits = floorUnits;
    }

    public ElectricLine getElectricLine() {
        return electricLine;
    }

    public void setElectricLine(ElectricLine electricLine) {
        this.electricLine = electricLine;
    }

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
            getErrors().putAll(errors);
    }
    public void addNocs(Map<String, String> nocs) {
        if (noObjectionCertificates != null)
            getNoObjectionCertificates().putAll(nocs);
    }
    public void addNoc(String key, String value) {

        if (noObjectionCertificates != null)
            getNoObjectionCertificates().put(key, value);
    }

    public void addError(String key, String value) {

        if (errors != null)
            getErrors().put(key, value);
    }

    public HashMap<String, String> getErrors() {
        return errors;
    }

    public void setErrors(HashMap<String, String> errors) {
        this.errors = errors;
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

    public Plot getPlot() {
        return plot;
    }

    public void setPlot(Plot plot) {
        this.plot = plot;
    }

    public VirtualBuilding getVirtualBuilding() {
        return virtualBuilding;
    }
    public void setVirtualBuilding(VirtualBuilding virtualBuilding) {
        this.virtualBuilding = virtualBuilding;
    }
    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }


    public Utility getUtility() {
        return utility;
    }

    public void setUtility(Utility utility) {
        this.utility = utility;
    }

    public Basement getBasement() {
        return basement;
    }

    public void setBasement(Basement basement) {
        this.basement = basement;
    }

    public List<Measurement> getParkingSlots() {
        return parkingSlots;
    }

    public void setParkingSlots(List<Measurement> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }
    
}
