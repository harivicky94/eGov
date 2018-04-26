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
package org.egov.bpa.transaction.service.report;

import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.dto.SearchBpaApplicationForm;
import org.egov.bpa.transaction.entity.dto.SearchBpaApplicationReport;
import org.egov.bpa.transaction.entity.dto.SlotDetailsHelper;
import org.egov.bpa.transaction.service.SearchBpaApplicationService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.egov.bpa.utils.BpaConstants.ADDING_OF_EXTENSION;
import static org.egov.bpa.utils.BpaConstants.ALTERATION;
import static org.egov.bpa.utils.BpaConstants.AMENITIES;
import static org.egov.bpa.utils.BpaConstants.CHANGE_IN_OCCUPANCY;
import static org.egov.bpa.utils.BpaConstants.DEMOLITION;
import static org.egov.bpa.utils.BpaConstants.DIVISION_OF_PLOT;
import static org.egov.bpa.utils.BpaConstants.NEW_CONSTRUCTION;
import static org.egov.bpa.utils.BpaConstants.PERM_FOR_HUT_OR_SHED;
import static org.egov.bpa.utils.BpaConstants.POLE_STRUCTURES;
import static org.egov.bpa.utils.BpaConstants.RECONSTRUCTION;
import static org.egov.bpa.utils.BpaConstants.TOWER_CONSTRUCTION;

@Service
@Transactional(readOnly = true)
public class BpaReportsService {

    @Autowired
    private SearchBpaApplicationService searchBpaApplicationService;

    @PersistenceContext
    private EntityManager entityManager;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<SearchBpaApplicationReport> getResultsByServicetypeAndStatus(
            final SearchBpaApplicationForm searchBpaApplicationForm) {
        List<SearchBpaApplicationReport> searchBpaApplicationReportList = new ArrayList<>();
        List<SearchBpaApplicationForm> searchBpaApplnResultList = searchBpaApplicationService.search(searchBpaApplicationForm);
        Map<String, Map<String, Long>> resultMap = searchBpaApplnResultList.stream().collect(
                Collectors.groupingBy(SearchBpaApplicationForm::getStatus,
                        Collectors.groupingBy(SearchBpaApplicationForm::getServiceType, Collectors.counting())));
        for (final Entry<String, Map<String, Long>> statusCountResMap : resultMap.entrySet()) {
            Long newCostruction = 0l;
            Long demolition = 0l;
            Long reConstruction = 0l;
            Long alteration = 0l;
            Long divisionOfPlot = 0l;
            Long addingExtension = 0l;
            Long changeInOccupancy = 0l;
            Long amenities = 0l;
            Long hut = 0l;
            Long towerConstruction = 0l;
            Long poleStructure = 0l;
            SearchBpaApplicationReport bpaApplicationReport = new SearchBpaApplicationReport();
            bpaApplicationReport.setStatus(statusCountResMap.getKey());
            for (final Entry<String, Long> statusCountMap : statusCountResMap.getValue().entrySet()) {
                if (NEW_CONSTRUCTION.equalsIgnoreCase(statusCountMap.getKey())) {
                    newCostruction = newCostruction + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType01(newCostruction);
                } else if (DEMOLITION.equalsIgnoreCase(statusCountMap.getKey())) {
                    demolition = demolition + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType02(demolition);
                } else if (RECONSTRUCTION.equalsIgnoreCase(statusCountMap.getKey())) {
                    reConstruction = reConstruction + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType03(reConstruction);
                } else if (ALTERATION.equalsIgnoreCase(statusCountMap.getKey())) {
                    alteration = alteration + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType04(alteration);
                } else if (DIVISION_OF_PLOT.equalsIgnoreCase(statusCountMap.getKey())) {
                    divisionOfPlot = divisionOfPlot + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType05(divisionOfPlot);
                } else if (ADDING_OF_EXTENSION.equalsIgnoreCase(statusCountMap.getKey())) {
                    addingExtension = addingExtension + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType06(addingExtension);
                } else if (CHANGE_IN_OCCUPANCY.equalsIgnoreCase(statusCountMap.getKey())) {
                    changeInOccupancy = changeInOccupancy + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType07(changeInOccupancy);
                } else if (AMENITIES.equalsIgnoreCase(statusCountMap.getKey())) {
                    amenities = amenities + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType08(amenities);
                } else if (PERM_FOR_HUT_OR_SHED.equalsIgnoreCase(statusCountMap.getKey())) {
                    hut = hut + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType09(hut);
                } else if (TOWER_CONSTRUCTION.equalsIgnoreCase(statusCountMap.getKey())) {
                    towerConstruction = towerConstruction + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType14(towerConstruction);
                } else if (POLE_STRUCTURES.equalsIgnoreCase(statusCountMap.getKey())) {
                    poleStructure = poleStructure + statusCountMap.getValue();
                    bpaApplicationReport.setServiceType15(poleStructure);
                }
            }
            searchBpaApplicationReportList.add(bpaApplicationReport);
        }
        return searchBpaApplicationReportList;
    }

    public List<SearchBpaApplicationReport> getResultsForEachServicetypeByZone(
            final SearchBpaApplicationForm searchBpaApplicationForm) {
        List<SearchBpaApplicationReport> searchBpaApplicationReportList = new ArrayList<>();
        List<SearchBpaApplicationForm> searchBpaApplnResultList = searchBpaApplicationService.search(searchBpaApplicationForm);
        Map<String, Map<String, Long>> resultMap = searchBpaApplnResultList.stream().collect(
                Collectors.groupingBy(SearchBpaApplicationForm::getServiceType,
                        Collectors.groupingBy(SearchBpaApplicationForm::getZone, Collectors.counting())));
        for (final Entry<String, Map<String, Long>> statusCountResMap : resultMap.entrySet()) {
            Long zone1N = 0l;
            Long zone1S = 0l;
            Long zone2 = 0l;
            Long zone3 = 0l;
            Long zone4 = 0l;
            SearchBpaApplicationReport bpaApplicationReport = new SearchBpaApplicationReport();
            bpaApplicationReport.setServiceType(statusCountResMap.getKey());
            for (final Entry<String, Long> statusCountMap : statusCountResMap.getValue().entrySet()) {

                if ("ZONE-1 NORTH (MAIN OFFICE)".equalsIgnoreCase(statusCountMap.getKey())) {
                    zone1N = zone1N + statusCountMap.getValue();
                    bpaApplicationReport.setZone1N(zone1N);
                } else if ("ZONE-1 SOUTH (MAIN OFFICE)".equalsIgnoreCase(statusCountMap.getKey())) {
                    zone1S = zone1S + statusCountMap.getValue();
                    bpaApplicationReport.setZone1S(zone1S);
                } else if ("ZONE-2 (ELATHUR ZONAL OFFICE)".equalsIgnoreCase(statusCountMap.getKey())) {
                    zone2 = zone2 + statusCountMap.getValue();
                    bpaApplicationReport.setZone2(zone2);
                } else if ("ZONE-3 (BEYPORE ZONAL OFFICE)".equalsIgnoreCase(statusCountMap.getKey())) {
                    zone3 = zone3 + statusCountMap.getValue();
                    bpaApplicationReport.setZone3(zone3);
                } else if ("ZONE-4 (CHERUVANNURE NALLALAM ZONAL OFFICE)".equalsIgnoreCase(statusCountMap.getKey())) {
                    zone4 = zone4 + statusCountMap.getValue();
                    bpaApplicationReport.setZone4(statusCountMap.getValue());
                }
            }
            searchBpaApplicationReportList.add(bpaApplicationReport);
        }
        return searchBpaApplicationReportList;
    }

    public List<SlotDetailsHelper> searchSlotDetails(final SlotDetailsHelper slotDetailsHelper, final String applicationType) {
        final Criteria criteria = getCurrentSession().createCriteria(SlotDetail.class, "slotDetail");
        criteria.createAlias("slotDetail.slot", "slot");
        if(slotDetailsHelper.getZoneId() != null) {
            criteria.createAlias("slot.zone", "zone");
            criteria.add(Restrictions.eq("zone.id", slotDetailsHelper.getZoneId()));
        }
        //TODO:Need to work on get by days
        /*if(slotDetailsHelper.getByNoOfDays() != null)
            criteria.add(Restrictions.ge("slot.appointmentDate", new DateTime(new Date()).minusDays(slotDetailsHelper.getByNoOfDays()).toDate()));*/
        if(slotDetailsHelper.getAppointmentDate() != null)
            criteria.add(Restrictions.eq("slot.appointmentDate", slotDetailsHelper.getAppointmentDate()));
		if (slotDetailsHelper.getElectionWardId() != null) {
			criteria.createAlias("slot.electionWard", "electionWard")
					.add(Restrictions.eq("electionWard.id", slotDetailsHelper.getElectionWardId()));
		}
		if("onedaypermit".equals(applicationType)) {
			criteria.add(Restrictions.isNotNull("slot.electionWard"));
		} else
        	criteria.add(Restrictions.isNull("slot.electionWard"));

        criteria.addOrder(Order.desc("slot.appointmentDate"));
        criteria.addOrder(Order.asc("slot.zone"));
        return buildSlotDetailsResponse(criteria);
    }

    private List<SlotDetailsHelper> buildSlotDetailsResponse(final Criteria criteria) {
        List<SlotDetailsHelper> slotDetailsHelperList = new ArrayList<>();
        for(SlotDetail slotDetail : (List<SlotDetail>)criteria.list()) {
            SlotDetailsHelper slotDetailsHelper = new SlotDetailsHelper();
            slotDetailsHelper.setSlotId(slotDetail.getSlot().getId());
            slotDetailsHelper.setAppointmentDate(slotDetail.getSlot().getAppointmentDate());
            slotDetailsHelper.setZone(slotDetail.getSlot().getZone().getName());
            slotDetailsHelper.setZoneId(slotDetail.getSlot().getZone().getId());
            if(slotDetail.getSlot().getElectionWard() != null) {
                slotDetailsHelper.setElectionWard(slotDetail.getSlot().getElectionWard().getName());
                slotDetailsHelper.setElectionWardId(slotDetail.getSlot().getElectionWard().getId());
            }
            slotDetailsHelper.setSlotDetailsId(slotDetail.getId());
            slotDetailsHelper.setAppointmentTime(slotDetail.getAppointmentTime());
            slotDetailsHelper.setMaxScheduledSlots(slotDetail.getMaxScheduledSlots());
            slotDetailsHelper.setMaxRescheduledSlots(slotDetail.getMaxRescheduledSlots());
            slotDetailsHelper.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots());
            slotDetailsHelper.setUtilizedRescheduledSlots(slotDetail.getUtilizedRescheduledSlots());
            slotDetailsHelperList.add(slotDetailsHelper);
        }
        return slotDetailsHelperList;
    }


}
