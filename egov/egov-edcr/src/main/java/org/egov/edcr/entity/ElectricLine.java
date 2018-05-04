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

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.math.BigDecimal;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;


@Entity
@Table(name = "EDCR_ELECTRICLINE")
@SequenceGenerator(name = ElectricLine.SEQ_EDCR_ELECTRIC_LINE, sequenceName = ElectricLine.SEQ_EDCR_ELECTRIC_LINE, allocationSize = 1)
public class ElectricLine extends Measurement {

    public static final String SEQ_EDCR_ELECTRIC_LINE = "SEQ_EDCR_ELECTRIC_LINE";
    private static final long serialVersionUID = -2132152973548122050L;

    private BigDecimal verticalDistance;

    private BigDecimal horizontalDistance;

    private BigDecimal voltage;

    @OneToOne(mappedBy = "electricLine", cascade = ALL, fetch = LAZY)
    private PlanDetail planDetail;

    public BigDecimal getVerticalDistance() {
        return verticalDistance;
    }

    public void setVerticalDistance(BigDecimal verticalDistance) {
        this.verticalDistance = verticalDistance;
    }

    public BigDecimal getHorizontalDistance() {
        return horizontalDistance;
    }

    public void setHorizontalDistance(BigDecimal horizontalDistance) {
        this.horizontalDistance = horizontalDistance;
    }

    public BigDecimal getVoltage() {
        return voltage;
    }

    public void setVoltage(BigDecimal voltage) {
        this.voltage = voltage;
    }
    
    public PlanDetail getPlanDetail() {
        return planDetail;
    }
    
    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    @Override
    public String toString() {
        return "ElectricLine :verticalDistance=" + verticalDistance + ", horizontalDistance=" + horizontalDistance + ", voltage="
                + voltage + "";
    }

}
