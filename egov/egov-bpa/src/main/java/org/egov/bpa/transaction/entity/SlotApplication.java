package org.egov.bpa.transaction.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "egbpa_slotapplication")
@SequenceGenerator(name = SlotApplication.SEQ, sequenceName = SlotApplication.SEQ, allocationSize = 1)
public class SlotApplication extends AbstractAuditable {

	private static final long serialVersionUID = 1L;
	public static final String SEQ = "seq_egbpa_slotapplication";
	@Id
	@GeneratedValue(generator = SEQ, strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "application", nullable = false)
	private BpaApplication application;

	@Enumerated(EnumType.STRING)
	@NotNull
	private ScheduleAppointmentType scheduleAppointmentType;

	private Boolean isRescheduledByCitizen;

	private Boolean isRescheduledByEmployee;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "slotdetailid", nullable = false)
	private SlotDetail slotDetail;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public BpaApplication getApplication() {
		return application;
	}

	public void setApplication(BpaApplication application) {
		this.application = application;
	}

	public ScheduleAppointmentType getScheduleAppointmentType() {
		return scheduleAppointmentType;
	}

	public void setScheduleAppointmentType(ScheduleAppointmentType scheduleAppointmentType) {
		this.scheduleAppointmentType = scheduleAppointmentType;
	}

	public Boolean getIsRescheduledByCitizen() {
		return isRescheduledByCitizen;
	}

	public void setIsRescheduledByCitizen(Boolean isRescheduledByCitizen) {
		this.isRescheduledByCitizen = isRescheduledByCitizen;
	}

	public Boolean getIsRescheduledByEmployee() {
		return isRescheduledByEmployee;
	}

	public void setIsRescheduledByEmployee(Boolean isRescheduledByEmployee) {
		this.isRescheduledByEmployee = isRescheduledByEmployee;
	}

	public SlotDetail getSlotDetail() {
		return slotDetail;
	}

	public void setSlotDetail(SlotDetail slotDetail) {
		this.slotDetail = slotDetail;
	}

}
