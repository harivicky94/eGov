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
import org.egov.edcr.entity.measurement.Yard;


import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "EDCR_PLOT")
@SequenceGenerator(name = Plot.SEQ_EDCR_PLOT, sequenceName = Plot.SEQ_EDCR_PLOT, allocationSize = 1)
public class Plot extends Measurement{

    public static final String SEQ_EDCR_PLOT = "SEQ_EDCR_PLOT";
    private static final long serialVersionUID = -4008688541272615177L;

    public static final String PLOT_BOUNDARY = "PLOT_BOUNDARY";

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "frontYard")
    private Yard frontYard;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "rearYard")
    private Yard rearYard;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "sideYard1")
    private Yard sideYard1;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "sideYard2")
    private Yard sideYard2;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "bsmtFrontYard")
    private Yard bsmtFrontYard;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "bsmtRearYard")
    private Yard bsmtRearYard;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "bsmtSideYard1")
    private Yard bsmtSideYard1;

    @OneToOne(fetch = LAZY, cascade = ALL)
    @JoinColumn(name = "bsmtSideYard2")
    private Yard bsmtSideYard2;

    public Yard getFrontYard() {
        return frontYard;
    }

    public void setFrontYard(Yard frontYard) {
        this.frontYard = frontYard;
    }

    public Yard getRearYard() {
        return rearYard;
    }

    public void setRearYard(Yard rearYard) {
        this.rearYard = rearYard;
    }

    public Yard getSideYard1() {
        return sideYard1;
    }

    public void setSideYard1(Yard sideYard1) {
        this.sideYard1 = sideYard1;
    }

    public Yard getSideYard2() {
        return sideYard2;
    }

    public void setSideYard2(Yard sideYard2) {
        this.sideYard2 = sideYard2;
    }

    public Yard getBsmtFrontYard() {
        return bsmtFrontYard;
    }

    public void setBsmtFrontYard(Yard bsmtFrontYard) {
        this.bsmtFrontYard = bsmtFrontYard;
    }

    public Yard getBsmtRearYard() {
        return bsmtRearYard;
    }

    public void setBsmtRearYard(Yard bsmtRearYard) {
        this.bsmtRearYard = bsmtRearYard;
    }

    public Yard getBsmtSideYard1() {
        return bsmtSideYard1;
    }

    public void setBsmtSideYard1(Yard bsmtSideYard1) {
        this.bsmtSideYard1 = bsmtSideYard1;
    }

    public Yard getBsmtSideYard2() {
        return bsmtSideYard2;
    }

    public void setBsmtSideYard2(Yard bsmtSideYard2) {
        this.bsmtSideYard2 = bsmtSideYard2;
    }
}
