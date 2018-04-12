/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2017>  eGovernments Foundation
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
package org.egov.bpa.transaction.repository;

import java.util.Date;
import java.util.List;

import org.egov.bpa.transaction.entity.Slot;
import org.egov.infra.admin.master.entity.Boundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Query("select slot from Slot slot where slot.zone = :zone and slot.appointmentDate >= :slotDate and slot.type = 'Normal' order by slot.appointmentDate asc")
    List<Slot> findByZoneAndApplicationDate(@Param("zone") Boundary zone, @Param("slotDate") Date slotDate);

    @Query("select slot from Slot slot where slot.zone = :zone and slot.appointmentDate = :appointmentDate and slot.type='Normal'")
    List<Slot> findByZoneAndAppointmentDate(@Param("zone") Boundary zone,
            @Param("appointmentDate") Date appointmentDate);

    @Query("select slot from Slot slot where slot.zone = :zone and slot.appointmentDate = :appointmentDate and slot.type =:type")
    Slot getOpenSlot(@Param("zone") Boundary zone,
            @Param("appointmentDate") Date appointmentDate, @Param("type") String type);
    
    @Query("select slot from Slot slot where slot.zone = :zone and slot.appointmentDate = :appointmentDate and slot.type =:type and slot.electionWard = :electionWard")
    Slot getOpenSlotForOneDayPermit(@Param("zone") Boundary zone, @Param("electionWard") Boundary electionWard,
            @Param("appointmentDate") Date appointmentDate, @Param("type") String type);

    Slot findByZoneAndElectionWardAndAppointmentDate(Boundary zone, Boundary electionWard, Date appointmentDate);

    @Query("select slot from Slot slot where slot.zone = :zone  and slot.electionWard = :ward and slot.appointmentDate >= :slotDate and slot.type = 'One Day Permit' order by slot.appointmentDate asc")
    List<Slot> findByZoneWardAndApplicationDateForOneDayPermit(@Param("zone") Boundary zone, @Param("ward") Boundary ward,
            @Param("slotDate") Date slotDate);
}