/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.edcr.entity;

import org.egov.edcr.entity.measurement.*;
import org.egov.edcr.entity.utility.Utility;
import org.egov.infra.persistence.entity.AbstractAuditable;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.SEQUENCE;

/*All the details extracted from the plan are referred in this object*/
@Entity
@Table(name = "EDCR_PLAN_DETAIL")
@SequenceGenerator(name = PlanDetail.SEQ_EDCR_PLAN_DETAIL, sequenceName = PlanDetail.SEQ_EDCR_PLAN_DETAIL, allocationSize = 1)
public class PlanDetail extends AbstractAuditable{

    public static final String SEQ_EDCR_PLAN_DETAIL = "SEQ_EDCR_PLAN_DETAIL";
    private static final long serialVersionUID = -4680122090053837162L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLAN_DETAIL, strategy = SEQUENCE)
    private Long id;

    @Transient
    private Utility utility = new Utility();

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "planInformation")
    private PlanInformation planInformation;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "plot")
    private Plot plot;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "building")
    private Building building;

    @Transient
    public ReportOutput reportOutput = new ReportOutput();

    @Transient
    private Boolean edcrPassed = false;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "electricLine")
    private ElectricLine electricLine;

    @OneToMany(mappedBy = "nonNotifiedRoadPlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<NonNotifiedRoad> nonNotifiedRoads = new ArrayList<>();

    @OneToMany(mappedBy = "notifiedRoadPlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<NotifiedRoad> notifiedRoads = new ArrayList<>();

    @OneToMany(mappedBy = "culDeSacPlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<CulDeSacRoad> culdeSacRoads = new ArrayList<>();

    @OneToMany(mappedBy = "lanePlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<Lane> laneRoads = new ArrayList<>();

    @Transient
    private Map<String, String> errors = new HashMap<>();

    @Transient
    private Map<String, String> noObjectionCertificates = new HashMap<>();

    @Transient
    private Map<String, String> generalInformation = new HashMap<>();

    @OneToOne(fetch = LAZY, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "basement")
    private Basement basement;
    
    @OneToMany(mappedBy = "psPlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<ParkingSlot> parkingSlots = new ArrayList<>();

    @OneToMany(mappedBy = "fuPlanDetail", orphanRemoval = true, fetch = LAZY, cascade = ALL)
    private List<FloorUnit> floorUnits = new ArrayList<>();

    public Map<String, String> getNoObjectionCertificates() {
        return noObjectionCertificates;
    }

    public void setNoObjectionCertificates(Map<String, String> noObjectionCertificates) {
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

    public Map<String, String> getGeneralInformation() {
        return generalInformation;
    }

    public void setGeneralInformation(Map<String, String> generalInformation) {
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

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
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

    public List<ParkingSlot> getParkingSlots() {
        return parkingSlots;
    }

    public void setParkingSlots(List<ParkingSlot> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
