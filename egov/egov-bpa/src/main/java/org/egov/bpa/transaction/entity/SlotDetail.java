package org.egov.bpa.transaction.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "egbpa_slotdetail")
@SequenceGenerator(name = SlotDetail.SEQ, sequenceName = SlotDetail.SEQ, allocationSize = 1)
public class SlotDetail extends AbstractAuditable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SEQ = "seq_egbpa_slotdetail";

	@Id
	@GeneratedValue(generator = SEQ, strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	private String appointmentTime;

	@NotNull
	private Integer maxScheduledSlots;

	@NotNull
	private Integer maxRescheduledSlots;

	@NotNull
	private Integer utilizedScheduledSlots;

	@NotNull
	private Integer utilizedRescheduledSlots;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "slotid", nullable = false)
	private Slot slot;

	@OneToMany(mappedBy = "slotDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SlotApplication> slotApplication = new ArrayList<>();

	public Slot getSlot() {
		return slot;
	}

	public void setSlot(Slot slot) {
		this.slot = slot;
	}

	public List<SlotApplication> getSlotApplication() {
		return slotApplication;
	}

	public void setSlotApplication(List<SlotApplication> slotApplication) {
		this.slotApplication = slotApplication;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(String appointmentTime) {
		this.appointmentTime = appointmentTime;
	}

	public Integer getMaxScheduledSlots() {
		return maxScheduledSlots;
	}

	public void setMaxScheduledSlots(Integer maxScheduledSlots) {
		this.maxScheduledSlots = maxScheduledSlots;
	}

	public Integer getMaxRescheduledSlots() {
		return maxRescheduledSlots;
	}

	public void setMaxRescheduledSlots(Integer maxRescheduledSlots) {
		this.maxRescheduledSlots = maxRescheduledSlots;
	}

	public Integer getUtilizedScheduledSlots() {
		return utilizedScheduledSlots;
	}

	public void setUtilizedScheduledSlots(Integer utilizedScheduledSlots) {
		this.utilizedScheduledSlots = utilizedScheduledSlots;
	}

	public Integer getUtilizedRescheduledSlots() {
		return utilizedRescheduledSlots;
	}

	public void setUtilizedRescheduledSlots(Integer utilizedRescheduledSlots) {
		this.utilizedRescheduledSlots = utilizedRescheduledSlots;
	}

}
