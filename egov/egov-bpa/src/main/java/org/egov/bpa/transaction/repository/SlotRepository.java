package org.egov.bpa.transaction.repository;

import java.util.Date;
import java.util.List;

import org.egov.bpa.transaction.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

	@Query("select slot from Slot as slot left join  slot.slotDetail as slotdetail"
			+ " where slot.appointmentDate >= :appointmentDate and (slotdetail.maxScheduledSlots"
			+ " - slotdetail.utilizedScheduledSlots >0 or slotdetail.maxRescheduledSlots -"
			+ " slotdetail.utilizedRescheduledSlots >0 or (slotdetail.maxScheduledSlots -"
			+ " slotdetail.utilizedScheduledSlots >0 and slotdetail.maxRescheduledSlots -"
			+ " slotdetail.utilizedRescheduledSlots >0)) order by slot.appointmentDate ,"
			+ " slotdetail.id")
	List<Slot> findSlotsByAppointmentDate(@Param("appointmentDate")Date appointmentDate);

}
