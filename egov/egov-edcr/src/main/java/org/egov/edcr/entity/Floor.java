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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

@Entity
@Table(name = "EDCR_FLOOR")
@SequenceGenerator(name = Floor.SEQ_EDCR_FLOOR, sequenceName = Floor.SEQ_EDCR_FLOOR, allocationSize = 1)
public class Floor extends Measurement{

    public static final String SEQ_EDCR_FLOOR = "SEQ_EDCR_FLOOR";
    private static final long serialVersionUID = -7097079080368627308L;

    private String name;

    @OneToMany(mappedBy = "floorData", cascade = ALL, fetch = LAZY)
    private List<Room> habitableRooms = new ArrayList<>();

    
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "exterior")
    private Exterior exterior;

    
    @OneToMany(mappedBy = "floor", fetch = LAZY, cascade = ALL)
    private List<OpenSpace> openSpaces = new ArrayList<>();

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "building")
    private Building buildingDetail;

    public List<Room> getHabitableRooms() {
        return habitableRooms;
    }

    public void setHabitableRooms(List<Room> habitableRooms) {
        this.habitableRooms = habitableRooms;
    }

    public Exterior getExterior() {
        return exterior;
    }

    public void setExterior(Exterior exterior) {
        this.exterior = exterior;
    }

    public List<OpenSpace> getOpenSpaces() {
        return openSpaces;
    }

    public void setOpenSpaces(List<OpenSpace> openSpaces) {
        this.openSpaces = openSpaces;
    }

    @Override
    public String toString() {
        return "Floor [habitableRooms Count" + habitableRooms.size() + "\n exterior=" + exterior + "\n openSpaces Count=" + openSpaces.size() + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Building getBuildingDetail() {
        return buildingDetail;
    }

    public void setBuildingDetail(Building buildingDetail) {
        this.buildingDetail = buildingDetail;
    }
}
