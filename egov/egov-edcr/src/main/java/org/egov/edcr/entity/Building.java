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

import org.egov.edcr.entity.measurement.Measurement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "EDCR_BUILDING")
@SequenceGenerator(name = Building.SEQ_EDCR_BUILDING, sequenceName = Building.SEQ_EDCR_BUILDING, allocationSize = 1)
public class Building extends Measurement{

    public static final String SEQ_EDCR_BUILDING = "SEQ_EDCR_BUILDING";
    private static final long serialVersionUID = 4172639386762640520L;

    private BigDecimal buildingHeight;

    private BigDecimal buildingTopMostHeight;

    private BigDecimal totalFloorArea;

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name="exteriorWall")
    private ExteriorWall exteriorWall;

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "shade")
    private Shade shade;

    @OneToMany(mappedBy = "buildingDetail", fetch = LAZY, cascade = ALL)
    private List<OpenStair> openStairs = new ArrayList<>();

    private BigDecimal far;

    private BigDecimal coverage;
    /*
     * Maximum number of floors
     */
    private BigDecimal maxFloor;
    /*
     * Total number of floors including celler
     */
    private BigDecimal totalFloors;

    @OneToMany(mappedBy = "buildingDetail", fetch = LAZY, cascade = ALL)
    private List<Floor> floors = new ArrayList<>();

    private BigDecimal floorsAboveGround;

    private BigDecimal distanceFromBuildingFootPrintToRoadEnd;

    private BigDecimal totalBuitUpArea;

    @OneToOne(mappedBy = "building", cascade = ALL, fetch = LAZY)
    private PlanDetail planDetail;

    public BigDecimal getBuildingHeight() {
        return buildingHeight;
    }

    public void setBuildingHeight(BigDecimal buildingHeight) {
        this.buildingHeight = buildingHeight;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public BigDecimal getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(BigDecimal totalFloors) {
        this.totalFloors = totalFloors;
    }

    public BigDecimal getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(BigDecimal maxFloor) {
        this.maxFloor = maxFloor;
    }

    public BigDecimal getBuildingTopMostHeight() {
        return buildingTopMostHeight;
    }

    public void setBuildingTopMostHeight(BigDecimal buildingHeightTopMost) {
        buildingTopMostHeight = buildingHeightTopMost;
    }

    public BigDecimal getTotalFloorArea() {
        return totalFloorArea;
    }

    public void setTotalFloorArea(BigDecimal totalFloorArea) {
        this.totalFloorArea = totalFloorArea;
    }

    public BigDecimal getFar() {
        return far;
    }

    public void setFar(BigDecimal far) {
        this.far = far;
    }

    public BigDecimal getCoverage() {
        return coverage;
    }

    public void setCoverage(BigDecimal coverage) {
        this.coverage = coverage;
    }

    public ExteriorWall getExteriorWall() {
        return exteriorWall;
    }

    public void setExteriorWall(ExteriorWall exteriorWall) {
        this.exteriorWall = exteriorWall;
    }

    public BigDecimal getFloorsAboveGround() {
        return floorsAboveGround;
    }

    public void setFloorsAboveGround(BigDecimal floorsAboveGround) {
        this.floorsAboveGround = floorsAboveGround;
    }

    public Shade getShade() {
        return shade;
    }

    public void setShade(Shade shade) {
        this.shade = shade;
    }

    public List<OpenStair> getOpenStairs() {
        return openStairs;
    }

    public void setOpenStairs(List<OpenStair> openStairs) {
        this.openStairs = openStairs;
    }

    public BigDecimal getTotalBuitUpArea() {
        return totalBuitUpArea;
    }

    public void setTotalBuitUpArea(BigDecimal totalBuitUpArea) {
        this.totalBuitUpArea = totalBuitUpArea;
    }

    
    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    
    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    @Override
    public String toString() {
        String newLine = "\n";
        StringBuilder str = new StringBuilder();
        str.append("Building :")
                .append(newLine)
                .append("buildingHeight:").append(this.buildingHeight).append(newLine)
                .append("totalFloorArea:").append(this.totalFloorArea).append(newLine)
                .append("far:").append(this.far).append(newLine)
                .append("Coverage:").append(this.coverage).append(newLine)
                .append("totalFloors:").append(this.totalFloors).append(newLine)
                .append("floorsAboveGround:").append(this.floorsAboveGround).append(newLine)
                .append("maxFloor:").append(this.maxFloor).append(newLine)
                .append("area:").append(this.area).append(newLine)
                .append("Floors Count:").append(this.floors.size()).append(newLine)
               .append("Exterior wall:").append(this.exteriorWall).append(newLine)
               .append("Open Stair count:").append(this.openStairs.size()).append(newLine)
                .append("Floors:").append(this.floors).append(newLine)
               .append("open stairs:").append(this.openStairs);
        return str.toString();
    }

    public BigDecimal getDistanceFromBuildingFootPrintToRoadEnd() {
        return distanceFromBuildingFootPrintToRoadEnd;
    }

    public void setDistanceFromBuildingFootPrintToRoadEnd(BigDecimal distanceFromBuildingFootPrintToRoadEnd) {
        this.distanceFromBuildingFootPrintToRoadEnd = distanceFromBuildingFootPrintToRoadEnd;
    }

}
