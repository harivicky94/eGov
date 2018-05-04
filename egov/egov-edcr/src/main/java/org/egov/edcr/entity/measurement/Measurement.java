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
package org.egov.edcr.entity.measurement;


import org.egov.infra.persistence.entity.AbstractAuditable;
import org.kabeja.dxf.DXFLWPolyline;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.InheritanceType;

@Entity
@Table(name = "EDCR_MEASUREMENT")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = Measurement.SEQ_EDCR_MEASUREMENT, sequenceName = Measurement.SEQ_EDCR_MEASUREMENT, allocationSize = 1)
public class Measurement extends AbstractAuditable implements java.io.Serializable{

    public static final String SEQ_EDCR_MEASUREMENT = "SEQ_EDCR_MEASUREMENT";
    private static final long serialVersionUID = 2453711727333550777L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_MEASUREMENT, strategy = GenerationType.SEQUENCE)
    private Long id;

    @Transient
    protected Boolean presentInDxf = false;

    protected BigDecimal minimumDistance;

    protected BigDecimal length;

    protected BigDecimal width;

    protected BigDecimal height;

    protected BigDecimal mean;

    protected BigDecimal area;

    @Transient
    protected DXFLWPolyline polyLine;

    public void setMinimumDistance(BigDecimal minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public Boolean getPresentInDxf() {
        return presentInDxf;
    }

    public void setPresentInDxf(Boolean present) {
        presentInDxf = present;
    }

    public BigDecimal getLength() {
        return length;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getMean() {
        return mean;
    }

    public void setMean(BigDecimal mean) {
        this.mean = mean;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getMinimumDistance() {
        return minimumDistance;
    }

    public DXFLWPolyline getPolyLine() {
        return polyLine;
    }

    public void setPolyLine(DXFLWPolyline polyLine) {
        this.polyLine = polyLine;
    }
    
    @Override
    public String toString() {
        return "Measurement : presentInDxf=" + presentInDxf + "\n polyLine Count=" + "]";
    }

}
