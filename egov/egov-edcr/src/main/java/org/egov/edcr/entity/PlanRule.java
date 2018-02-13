package org.egov.edcr.entity;

import org.egov.infra.persistence.entity.AbstractAuditable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/*Used to determine the rules to be validated for a building plan*/
@Entity
@Table(name = "EDCR_PLANRULE")
@SequenceGenerator(name = PlanRule.SEQ_EDCR_PLANRULE, sequenceName = PlanRule.SEQ_EDCR_PLANRULE, allocationSize = 1)
public class PlanRule extends AbstractAuditable {

    public static final String SEQ_EDCR_PLANRULE = "SEQ_EDCR_PLANRULE";
    private static final long serialVersionUID = 6649463259841178605L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLANRULE, strategy = GenerationType.SEQUENCE)
    private Long id;

    private String service;

    private BigDecimal plotArea;

    private String occupancy;

    private Double noOfFloors;

    private BigDecimal heightOfBuilding;

    @ManyToOne
    @JoinColumn(name = "rule")
    private Rule rule;

    @NotNull
    private Boolean active;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public BigDecimal getHeightOfBuilding() {
        return heightOfBuilding;
    }

    public void setHeightOfBuilding(BigDecimal heightOfBuilding) {
        this.heightOfBuilding = heightOfBuilding;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Double getNoOfFloors() {
        return noOfFloors;
    }

    public void setNoOfFloors(Double noOfFloors) {
        this.noOfFloors = noOfFloors;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
