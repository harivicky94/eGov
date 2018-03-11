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
package org.egov.bpa.transaction.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.bpa.master.service.SlotMappingService;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.Slot;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.repository.SlotDetailRepository;
import org.egov.bpa.transaction.repository.SlotRepository;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.repository.BoundaryRepository;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.validation.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
public class ScheduleAppointmentForDocumentScrutinyService {
    private static final String MODULE_NAME = "BPA";
    private static final String APP_CONFIG_KEY = "GAPFORSCHEDULING";

    private static final Logger LOGGER = Logger.getLogger(ScheduleAppointmentForDocumentScrutinyService.class);

    @Autowired
    private SlotMappingService slotMappingService;
    @Autowired
    private BpaStatusRepository bpaStatusRepository;
    @Autowired
    private ApplicationBpaService applicationBpaService;
    @Autowired
    private SlotRepository slotRepository;
    @Autowired
    private SlotApplicationService slotApplicationService;
    @Autowired
    private SlotDetailRepository slotDetailRepository;
    @Autowired
    private BPASmsAndEmailService bpaSmsAndEmailService;
    @Autowired
    private BoundaryRepository boundaryRepository;
    @Autowired
    private BpaUtils bpaUtils;
    @Autowired
    private SlotApplicationRepository slotApplicationRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private AppConfigValueService appConfigValuesService;
    
    public void scheduleAppointmentsForDocumentScrutiny() {
        Calendar calendar = Calendar.getInstance();
        List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(MODULE_NAME,
                APP_CONFIG_KEY);
        String noOfDays = appConfigValue.get(0).getValue();
        calendar.add(Calendar.DAY_OF_YEAR, Integer.valueOf(noOfDays));
        List<Boundary> zonesList = slotMappingService.slotfindZoneByApplType(ApplicationType.ALL_OTHER_SERVICES);
        for (Boundary bndry : zonesList) {
            if(LOGGER.isInfoEnabled()) {
                LOGGER.info("******************Zone ------>>>>>>" + bndry.getName());
                LOGGER.info("******************Gap for application schedule date ------>>>>>>" + calendar.getTime());
            }
            List<Slot> slotList = slotRepository.findByZoneAndApplicationDate(bndry, calendar.getTime());
            if(LOGGER.isInfoEnabled())
                LOGGER.info("******************Slot List Size ------>>>>>>" + slotList.size());
            if (slotList.size() > 0) {
                for (Slot slot : slotList) {
                    List<SlotDetail> slotDetailList = slotDetailRepository.findBySlot(slot);
                    if(LOGGER.isInfoEnabled())
                        LOGGER.info("******************Slot Details List Size ------>>>>>>" + slotDetailList.size());
                    slot.setSlotDetail(slotDetailList);
                    if (slot.getSlotDetail().size() > 0) {
                        Integer totalAvailableSlots = 0;
                        for (SlotDetail slotDetail : slot.getSlotDetail()) {
                            Integer diffScheduledSlots = 0;
                            Integer diffRescheduledSlots = 0;
                            if (slotDetail.getMaxScheduledSlots() > slotDetail.getUtilizedScheduledSlots())
                                diffScheduledSlots = slotDetail.getMaxScheduledSlots()
                                - slotDetail.getUtilizedScheduledSlots();
                            if (slotDetail.getMaxRescheduledSlots() > slotDetail.getUtilizedRescheduledSlots())
                                diffRescheduledSlots = slotDetail.getMaxRescheduledSlots()
                                - slotDetail.getUtilizedRescheduledSlots();
                            totalAvailableSlots = totalAvailableSlots + diffScheduledSlots + diffRescheduledSlots;
                        }
                        BpaStatus bpaStatusPendingForRescheduling = bpaStatusRepository
                                .findByCode(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING);
                        BpaStatus bpaStatusRegistered = bpaStatusRepository
                                .findByCode(BpaConstants.APPLICATION_STATUS_REGISTERED);
                        List<BpaStatus> bpaStatusList = new ArrayList<>();
                        bpaStatusList.add(bpaStatusRegistered);
                        bpaStatusList.add(bpaStatusPendingForRescheduling);
                        List<Boundary> boundaryList = boundaryRepository
                                .findActiveImmediateChildrenWithOutParent(slot.getZone().getId());

                        List<BpaApplication> bpaApplications = applicationBpaService
                                .getBpaApplicationsForScheduleAndReSchedule(bpaStatusList, boundaryList, totalAvailableSlots);
                        if (bpaApplications.size() > 0) { 
                            List <Long> registrationNumber= new ArrayList<>();
                            
                            for (BpaApplication bpaApp : bpaApplications) {
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info(
                                            "******************Application Number ------>>>>>>" + bpaApp.getApplicationNumber());
                                    LOGGER.info("******************Application Date ------>>>>>>" + bpaApp.getApplicationDate());
                                }

                                try {
                                     
                                    TransactionTemplate template = new TransactionTemplate(
                                            transactionTemplate.getTransactionManager());
                                    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                                    
                                    template.execute(result -> {

                                        try {
                                          
                                            
                                            LOGGER.info("_---- transaction id "+ template.getTransactionManager().hashCode());
                                            if (LOGGER.isInfoEnabled())
                                                LOGGER.info(
                                                        "****************** Schedule appointment  Transaction start *****************");
                                            for (SlotDetail slotDetail : slot.getSlotDetail()) {
                                                slotDetail=slotDetailRepository.findOne(slotDetail.getId());
                                                if (LOGGER.isInfoEnabled())
                                                    LOGGER.info(
                                                            "******************Inside Transaction --- Slot Details List Size ------>>>>>>"
                                                                    + slot.getSlotDetail().size());
                                                if (bpaApp.getStatus().getCode().toString()
                                                        .equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
                                                    List<SlotApplication> slotApplications = slotApplicationRepository
                                                            .findByApplicationOrderByIdDesc(bpaApp);
                                                    slotApplications.get(0).setActive(false);
                                                    slotApplicationRepository.save(slotApplications.get(0));
                                                    Date appointmentDate = slotApplications.get(0).getSlotDetail().getSlot()
                                                            .getAppointmentDate();
                                                    if (LOGGER.isInfoEnabled())
                                                        LOGGER.info(
                                                                "******************Inside Transaction --- Appointment Date ------>>>>>>"
                                                                        + appointmentDate);
                                                    if (slotDetail.getSlot().getAppointmentDate()
                                                            .compareTo(appointmentDate) > 0) {
                                                        if (slotDetail.getMaxRescheduledSlots()
                                                                - slotDetail.getUtilizedRescheduledSlots() > 0) {
                                                            slotDetail.setUtilizedRescheduledSlots(
                                                                    slotDetail.getUtilizedRescheduledSlots() + 1);
                                                            SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
                                                                    slotDetail);
                                                            createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
                                                                    slotApplication);
                                                            break;
                                                        } else if (slotDetail.getMaxScheduledSlots()
                                                                - slotDetail.getUtilizedScheduledSlots() > 0) {
                                                            slotDetail.setUtilizedScheduledSlots(
                                                                    slotDetail.getUtilizedScheduledSlots() + 1);
                                                            SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
                                                                    slotDetail);
                                                            createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
                                                                    slotApplication);
                                                            break;
                                                        }
                                                    }
                                                } else {
                                                    if (LOGGER.isInfoEnabled())
                                                        LOGGER.info(
                                                                "**Inside Transaction --- Regular Application schedule start ------>>>>>>");
                                                    if (slotDetail.getMaxScheduledSlots()
                                                            - slotDetail.getUtilizedScheduledSlots() > 0) {
                                                        slotDetail.setUtilizedScheduledSlots(
                                                                slotDetail.getUtilizedScheduledSlots() + 1);
                                                        SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
                                                                slotDetail);
                                                        createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
                                                                slotApplication);
                                                        if (LOGGER.isInfoEnabled())
                                                            LOGGER.info(
                                                                    "**Inside Transaction --- Regular Application schedule end ------>>>>>>");
                                                        break;
                                                    }
                                                }
                                            }
                                     /*       if( bpaApp.getId()==372||
                                                    bpaApp.getId()==388||
                                                    bpaApp.getId()==393|| bpaApp.getId()==405)
                                                throw new RuntimeException();
                                */            
                                            if (LOGGER.isInfoEnabled())
                                                LOGGER.info(
                                                        "****************** Schedule appointment Transaction End *****************");
                                           // return true;
                                        } catch (Exception e) {
                                             registrationNumber.add(bpaApp.getId());
                                             throw e;
                                            //throw new RuntimeException(e.getMessage());
                                        }
                                        return true;
                                    });
                                    if (LOGGER.isInfoEnabled())
                                        LOGGER.info("****************** Outside Transaction Template *****************");
                                } catch (Exception e) {
                                    
                                    /*transactionTemplate.execute(result1 -> {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.error("Exception in Generating " + bpaApp.getId() );
                                        LOGGER.error(e);
                                    }
                                    return Boolean.FALSE;
                                });*/
                                    LOGGER.error(e.getMessage(), e);
                                    getErrorMessage(e);
                                }

                            }
                            
                            if(registrationNumber.size()>0)
                            {

                                for (Long applicationId : registrationNumber) {
                                    transactionTemplate.execute(result1 -> {

                                        BpaApplication bpaApplication = applicationBpaService
                                                .findById(applicationId);
                                        bpaApplication.setFailureInScheduler(Boolean.TRUE);
                                        applicationBpaService.saveApplicationForScheduler(bpaApplication);

                                        if (LOGGER.isDebugEnabled()) {
                                            LOGGER.error("Exception in document schedule Generation " + bpaApplication.getId());
                                            // LOGGER.error(e);
                                        }
                                        return Boolean.FALSE;

                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private SlotApplication buildSlotApplicationObject(BpaApplication bpaApp, SlotDetail slotDetail) {
        SlotApplication slotApplication = new SlotApplication();
        slotApplication.setActive(true);
        //Same api used for scheduler and one day permit. In one day permit, application not yet saved. 
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(bpaApp.getApplicationNumber());
        if (bpaApplication != null)
            slotApplication.setApplication(bpaApplication);
        else
            slotApplication.setApplication(bpaApp);

        slotApplication.setSlotDetail(slotDetail);
        if (bpaApp.getStatus().getCode().toString().equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
            slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);  
            bpaApp.setStatus(bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_RESCHEDULED));
            //TODO: MOVE STATUS SET FROM HERE. 
            return slotApplication;
        } else {
            slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.SCHEDULE);
            bpaApp.setStatus(bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_SCHEDULED));
            return slotApplication;
        }

    }

    private void createSlotApplicationAndUpdateStatus(SlotDetail slotDetail, BpaApplication bpaApp,
            SlotApplication slotApplication) {
        if(LOGGER.isInfoEnabled())
            LOGGER.info("******************Inside Transaction --- Before slotApplication Save ******************************" + slotApplication);
        slotApplicationService.saveApplicationForScheduler(slotApplication);
        if(LOGGER.isInfoEnabled())
            LOGGER.info("******************Inside Transaction --- After slotApplication Save ******************************" + slotApplication);
        if(LOGGER.isInfoEnabled())
            LOGGER.info("******************Inside Transaction --- Before Bpa Application Save ******************************" + bpaApp);
        applicationBpaService.saveApplicationForScheduler(bpaApp);
        
        if(LOGGER.isInfoEnabled())
            LOGGER.info("******************Inside Transaction --- After Bpa Application Save ******************************" + bpaApp);
        if(LOGGER.isInfoEnabled())
            LOGGER.info("****************** Schedule Appointment Type ******************************" + slotApplication.getScheduleAppointmentType().name());
        if (slotApplication.getScheduleAppointmentType().toString()
                .equals(ScheduleAppointmentType.RESCHEDULE.toString())) {
            bpaUtils.redirectToBpaWorkFlowForScheduler(bpaApp.getCurrentState().getOwnerPosition().getId(), bpaApp, null,
                    "document scrutiny re-scheduled", BpaConstants.WF_RESCHDLE_APPMNT_BUTTON, null);
        } else if (slotApplication.getScheduleAppointmentType().toString()
                .equals(ScheduleAppointmentType.SCHEDULE.toString())) {
            if(LOGGER.isInfoEnabled())
                LOGGER.info("******************Start workflow - Schedule Appointment******************************");
            bpaUtils.redirectToBpaWorkFlowForScheduler(
                    slotApplication.getApplication().getCurrentState().getOwnerPosition().getId(),
                    slotApplication.getApplication(), null, BpaConstants.APPLICATION_STATUS_SCHEDULED, "Forward", null);
            if(LOGGER.isInfoEnabled())
                LOGGER.info("******************End workflow - Schedule Appointment******************************");
        }
        if(LOGGER.isInfoEnabled())
            LOGGER.info("****************** before sending sms and email *****************");
        bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication, bpaApp);
        if(LOGGER.isInfoEnabled())
            LOGGER.info("****************** after sending sms and email *****************");
    }

    private String getErrorMessage(final Exception exception) {
        String error;
        if (exception instanceof ValidationException)
            error = ((ValidationException) exception).getErrors().get(0).getMessage();
        else
            error = "Error : " + exception;
        return error;
    }

    public void scheduleOneDayPermitApplicationsForDocumentScrutiny(BpaApplication bpaApplication, SlotDetail slotDetail) {
        SlotApplication slotApplication = buildSlotApplicationObject(bpaApplication, slotDetail);
        slotDetail.setUtilizedScheduledSlots(
                slotDetail.getUtilizedScheduledSlots() + 1);
        slotApplicationService.save(slotApplication);
        applicationBpaService.saveBpaApplication(bpaApplication);
        bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication, bpaApplication);
    }

}
