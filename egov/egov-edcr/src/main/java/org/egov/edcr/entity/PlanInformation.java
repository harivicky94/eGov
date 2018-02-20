package org.egov.edcr.entity;

import org.egov.infra.persistence.entity.AbstractAuditable;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;

@Entity
@Table(name = "EDCR_PLANINFO")
@SequenceGenerator(name = PlanInformation.SEQ_EDCR_PLANINFO, sequenceName = PlanInformation.SEQ_EDCR_PLANINFO, allocationSize = 1)
public class PlanInformation extends AbstractAuditable{

    public static final String SEQ_EDCR_PLANINFO = "SEQ_EDCR_PLANINFO";
    private static final long serialVersionUID = -8471202461472480934L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLANINFO, strategy = GenerationType.SEQUENCE)
    private Long id;

    private BigDecimal plotArea;

    private String ownerName;

    private String architectName;

    private String occupancy;

    private Boolean crzZoneArea=false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCrzZoneArea() {
        return crzZoneArea;
    }

    public void setCrzZoneArea(Boolean crzZoneArea) {
        this.crzZoneArea = crzZoneArea;
    }

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getArchitectName() {
        return architectName;
    }

    public void setArchitectName(String architectName) {
        this.architectName = architectName;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

 
}
