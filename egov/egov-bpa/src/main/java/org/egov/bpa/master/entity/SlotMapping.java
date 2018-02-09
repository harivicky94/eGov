package org.egov.bpa.master.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "egbpa_mstr_slotmapping")
@SequenceGenerator(name = SlotMapping.SEQ, sequenceName = SlotMapping.SEQ, allocationSize = 1)
public class SlotMapping extends AbstractAuditable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SEQ = "seq_egbpa_mstr_slotmapping";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "zone")
	private Boundary zone;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "ward")
	private Boundary ward;

	@NotNull
	@Enumerated(EnumType.STRING)
	private ApplicationType applicationType;

	public Boundary getWard() {
		return ward;
	}

	public void setWard(Boundary ward) {
		this.ward = ward;
	}

	public ApplicationType getApplicationtype() {
		return applicationType;
	}

	public void setApplicationtype(ApplicationType applicationtype) {
		this.applicationType = applicationtype;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	private String day;

	@NotNull
	private Integer maxSlotsAllowed;

	@NotNull
	private Integer maxRescheduledSlotsAllowed;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Boundary getZone() {
		return zone;
	}

	public void setZone(Boundary zone) {
		this.zone = zone;
	}

	public Integer getMaxSlotsAllowed() {
		return maxSlotsAllowed;
	}

	public void setMaxSlotsAllowed(Integer maxSlotsAllowed) {
		this.maxSlotsAllowed = maxSlotsAllowed;
	}

	public Integer getMaxRescheduledSlotsAllowed() {
		return maxRescheduledSlotsAllowed;
	}

	public void setMaxRescheduledSlotsAllowed(Integer maxRescheduledSlotsAllowed) {
		this.maxRescheduledSlotsAllowed = maxRescheduledSlotsAllowed;
	}

}
