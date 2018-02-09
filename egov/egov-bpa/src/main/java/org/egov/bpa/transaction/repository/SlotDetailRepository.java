package org.egov.bpa.transaction.repository;

import java.util.Date;
import java.util.List;

import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.infra.admin.master.entity.Boundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotDetailRepository extends JpaRepository<SlotDetail, Long> {
	
	@Query("select detail from SlotDetail detail where detail.maxScheduledSlots - detail.utilizedScheduledSlots > 0 order by detail.slot.appointmentDate , detail.id asc")
	List<SlotDetail> findSlotDetailOrderByAppointmentDate();

	@Query("select detail from SlotDetail detail where detail.appointmentTime  = :appointmentTime and detail.slot.appointmentDate = :rescheduleAppointmentDate and detail.slot.zone = :zone")
	SlotDetail findByAppointmentDateTimeAndZone(@Param("rescheduleAppointmentDate")Date rescheduleAppointmentDate, @Param("appointmentTime")String appointmentTime,
			@Param("zone")Boundary zone);
	
}
