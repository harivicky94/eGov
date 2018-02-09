package org.egov.bpa.transaction.entity;

import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "egbpa_slot")
@SequenceGenerator(name = Slot.SEQ, sequenceName = Slot.SEQ, allocationSize = 1)
public class Slot extends AbstractAuditable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SEQ = "seq_egbpa_slot";

	@Id
	@GeneratedValue(generator = SEQ, strategy = GenerationType.SEQUENCE)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "zone")
	private Boundary zone;

	@Temporal(value = TemporalType.DATE)
	@NotNull
	private Date appointmentDate;

	@OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SlotDetail> slotDetail;

	public List<SlotDetail> getSlotDetail() {
		return slotDetail;
	}

	public void setSlotDetail(List<SlotDetail> slotDetail) {
		this.slotDetail = slotDetail;
	}

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

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

}
