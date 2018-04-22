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
package org.egov.bpa.transaction.service.messaging;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_MODULE_TYPE;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_CANCELLED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_REGISTERED;
import static org.egov.bpa.utils.BpaConstants.CREATEDLETTERTOPARTY;
import static org.egov.bpa.utils.BpaConstants.EGMODULE_NAME;
import static org.egov.bpa.utils.BpaConstants.NO;
import static org.egov.bpa.utils.BpaConstants.SENDEMAILFORBPA;
import static org.egov.bpa.utils.BpaConstants.SENDSMSFORBPA;
import static org.egov.bpa.utils.BpaConstants.SMSEMAILTYPELETTERTOPARTY;
import static org.egov.bpa.utils.BpaConstants.SMSEMAILTYPENEWBPAREGISTERED;
import static org.egov.bpa.utils.BpaConstants.YES;

import java.util.List;
import java.util.Locale;

import org.egov.bpa.master.entity.StakeHolder;
import org.egov.bpa.transaction.entity.ApplicationStakeHolder;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaAppointmentSchedule;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.enums.AppointmentSchedulePurpose;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.notification.service.NotificationService;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.workflow.entity.StateHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BPASmsAndEmailService {
    private static final String MSG_KEY_SMS_STAKEHOLDER_NEW = "msg.newstakeholder.sms";
    private static final String SUBJECT_KEY_EMAIL_STAKEHOLDER_NEW = "msg.newstakeholder.email.subject";
    private static final String BODY_KEY_EMAIL_STAKEHOLDER_NEW = "msg.newstakeholder.email.body";
    private static final String MSG_KEY_SMS_BPA_APPLN_NEW = "msg.bpa.newappln.sms";
    private static final String MSG_KEY_SMS_BPA_APPLN_NEW_PWD = "msg.bpa.newappln.sms.pwd";
    private static final String SUBJECT_KEY_EMAIL_BPA_APPLN_NEW = "msg.bpa.newappln.email.subject";
    private static final String BODY_KEY_EMAIL_BPA_APPLN_NEW = "msg.bpa.newappln.email.body";
    private static final String BODY_KEY_EMAIL_BPA_APPLN_NEW_PWD = "msg.bpa.newappln.email.body.pwd";

    private static final String MSG_KEY_SMS_BPA_DOC_SCRUTINY = "msg.bpa.doc.scruty.schedule.sms";
    private static final String BODY_KEY_EMAIL_BPA_DOC_SCRUTINY = "msg.bpa.doc.scruty.schedule.email.body";
    private static final String MSG_KEY_SMS_BPA_DOC_SCRUTINY_RESCHE = "msg.bpa.doc.scruty.reschedule.sms";
    private static final String BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_RESCHE = "msg.bpa.doc.scruty.reschedule.email.body";
    private static final String MSG_KEY_SMS_BPA_DOC_SCRUTINY_PENDING_FOR_RESCHEDULING = "msg.bpa.doc.scruty.reschedule.pending.sms";
    private static final String BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_PENDING_FOR_RESCHEDULING = "msg.bpa.doc.scruty.reschedule.pending.email.body";
    private static final String MSG_KEY_SMS_BPA_DOC_SCRUTINY_CANCELLED = "msg.bpa.doc.scruty.cancel.sms";
    private static final String BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_CANCELLED = "msg.bpa.doc.scruty.cancel.email.body";
    private static final String SUB_KEY_EMAIL_BPA_DOCUMENT_SCRUTINY = "msg.bpa.doc.scruty.email.sub";
    private static final String MSG_KEY_SMS_BPA_FIELD_INS = "msg.bpa.field.ins.schedule.sms";
    private static final String SUBJECT_KEY_EMAIL_BPA_FIELD_INS = "msg.bpa.field.ins.schedule.email.subject";
    private static final String BODY_KEY_EMAIL_BPA_FIELD_INS = "msg.bpa.field.ins.schedule.email.body";
    private static final String MSG_KEY_SMS_BPA_FIELD_INS_RESCHE = "msg.bpa.field.ins.reschedule.sms";
    private static final String SUBJECT_KEY_EMAIL_BPA_FIELD_INS_RESCHE = "msg.bpa.field.ins.reschedule.email.subject";
    private static final String BODY_KEY_EMAIL_BPA_FIELD_INS_RESCHE = "msg.bpa.field.ins.reschedule.email.body";

    private static final String MSG_KEY_SMS_LETTERTOPARTY = "msg.bpa.lettertoparty.sms";
    private static final String SUBJECT_KEY_EMAIL_LETTERTOPARTY = "msg.bpa.lettertoparty.email.subject";
    private static final String BODY_KEY_EMAIL_LETTERTOPARTY = "msg.bpa.lettertoparty.email.body";
    private static final String MSG_KEY_SMS_CANCELL_APPLN = "msg.bpa.cancel.appln.sms";
    private static final String SUBJECT_KEY_EMAIL_CANCELL_APPLN = "msg.bpa.cancel.appln.email.subject";
    private static final String BODY_KEY_EMAIL_CANCELL_APPLN = "msg.bpa.cancel.appln.email.body";
    private static final String SMS_STK_CTZ = "msg.newstakeholder.sms.citizen";
    private static final String EMLB_STK_CTZ = "msg.newstakeholder.email.body.citizen";
    private static final String EMLS_STK_CTZ = "msg.newstakeholder.email.subject.citizen";
    private static final String SMS_STK_RJCT = "msg.stakeholder.rejection.sms";
    private static final String EMLB_STK_RJCT = "msg.stakeholder.rejection.email.body";
    private static final String EMLS_STK_RJCT = "msg.stakeholder.rejection.email.subject";

    @Autowired
    private NotificationService notificationService;
    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource bpaMessageSource;
    @Autowired
    private AppConfigValueService appConfigValuesService;
    @Autowired
    private BpaUtils bpaUtils;

    public String getMunicipalityName() {
        return ApplicationThreadLocals.getMunicipalityName();
    }

    public void sendSMSForStakeHolder(final StakeHolder stakeHolder, Boolean isCitizenCrtn) {
        String msgKey;
        if (isCitizenCrtn) {
            msgKey = SMS_STK_CTZ;
        } else {
            msgKey = MSG_KEY_SMS_STAKEHOLDER_NEW;
        }
        if (isSmsEnabled() && stakeHolder.getMobileNumber() != null) {
            String message = buildMessageDetails(stakeHolder, msgKey, isCitizenCrtn);
            notificationService.sendSMS(stakeHolder.getMobileNumber(), message);
        }
    }
    
    public void sendSMSToStkHldrForRejection(final StakeHolder stakeHolder) {
        String msgKey = SMS_STK_RJCT;
        if (isSmsEnabled() && stakeHolder.getMobileNumber() != null) {
            String message = buildMessageDtlsFrRejection(stakeHolder, msgKey);
            notificationService.sendSMS(stakeHolder.getMobileNumber(), message);
        }
    }

    private String buildMessageDtlsFrRejection(StakeHolder stakeHolder, String msgKey) {
        return bpaMessageSource.getMessage(msgKey, new String[] { stakeHolder.getName(),
                stakeHolder.getStakeHolderType().getStakeHolderTypeVal(), stakeHolder.getComments(), getMunicipalityName() },
                null);
    }
    
    public void sendEmailToStkHldrForRejection(StakeHolder stakeHolder) {
        String msgKeyMail = EMLB_STK_RJCT;
        String msgKeyMailSubject = EMLS_STK_RJCT;
        if (isEmailEnabled() && stakeHolder.getEmailId() != null) {
            final String message = buildMessageDtlsFrRejection(stakeHolder, msgKeyMail);
            final String subject = bpaMessageSource.getMessage(msgKeyMailSubject, null, null);
            notificationService.sendEmail(stakeHolder.getEmailId(), subject, message);
        }
    }

    public void sendEmailForStakeHolder(final StakeHolder stakeHolder, Boolean isCitizen) {
        String msgKeyMail;
        String msgKeyMailSubject;
        if (isCitizen) {
            msgKeyMail = EMLB_STK_CTZ;
            msgKeyMailSubject = EMLS_STK_CTZ;
        } else {
            msgKeyMail = BODY_KEY_EMAIL_STAKEHOLDER_NEW;
            msgKeyMailSubject = SUBJECT_KEY_EMAIL_STAKEHOLDER_NEW;
        }
        if (isEmailEnabled() && stakeHolder.getEmailId() != null) {
            final String message = buildMessageDetails(stakeHolder, msgKeyMail,isCitizen);
            final String subject = bpaMessageSource.getMessage(msgKeyMailSubject, null, null);
            notificationService.sendEmail(stakeHolder.getEmailId(), subject, message);
        }
    }

    public void sendSMSAndEmail(final BpaApplication bpaApplication) {
        String mobileNo;
        String email;
        String applicantName;
        String loginUserName;
        String password;
        if (isSmsEnabled() || isEmailEnabled()) {
            for (ApplicationStakeHolder applnStakeHolder : bpaApplication.getStakeHolder()) {
                if (applnStakeHolder.getApplication() != null && applnStakeHolder.getApplication().getOwner() != null) {
                    applicantName = applnStakeHolder.getApplication().getOwner().getName();
                    email = applnStakeHolder.getApplication().getOwner().getUser().getEmailId();
                    mobileNo = applnStakeHolder.getApplication().getOwner().getUser().getMobileNumber();
                    loginUserName = applnStakeHolder.getApplication().getOwner().getUser().getUsername();
                        password = mobileNo;
                    buildSmsAndEmailForBPANewAppln(bpaApplication, applicantName, mobileNo, email, loginUserName, password);
                }
                if (applnStakeHolder.getStakeHolder() != null && applnStakeHolder.getStakeHolder().getIsActive()) {
                    applicantName = applnStakeHolder.getStakeHolder().getName();
                    email = applnStakeHolder.getStakeHolder().getEmailId();
                    mobileNo = applnStakeHolder.getStakeHolder().getMobileNumber();
                    loginUserName = applnStakeHolder.getStakeHolder().getUsername();
                    password = EMPTY;
                    buildSmsAndEmailForBPANewAppln(bpaApplication, applicantName, mobileNo, email, loginUserName, password);
                }
            }
        }
    }

    public void sendSMSAndEmailToscheduleAppointment(final BpaAppointmentSchedule scheduleDetails,
            final BpaApplication bpaApplication) {
        if (isSmsEnabled() || isEmailEnabled()) {
            buildSmsAndEmailForScheduleAppointment(scheduleDetails, bpaApplication, bpaApplication.getOwner().getName(),
                    bpaApplication.getOwner().getUser().getMobileNumber(), bpaApplication.getOwner().getUser().getEmailId());
        }
    }

    public void sendSMSAndEmailToApplicantForLettertoparty(final BpaApplication bpaApplication) {
        if (isSmsEnabled() || isEmailEnabled()) {
            buildSmsAndEmailForBPANewAppln(bpaApplication, bpaApplication.getOwner().getName(),
                    bpaApplication.getOwner().getUser().getMobileNumber(),
                    bpaApplication.getOwner().getUser().getEmailId(), EMPTY, EMPTY);
        }
    }

    private void buildSmsAndEmailForBPANewAppln(final BpaApplication bpaApplication, final String applicantName,
            final String mobileNo, final String email, final String loginUserName, final String password) {
        String smsMsg = EMPTY;
        String body = EMPTY;
        String subject = EMPTY;
        String smsCode;
        String mailCode;
       if ((APPLICATION_STATUS_REGISTERED).equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
            if (isNotBlank(password)) {
                smsCode = MSG_KEY_SMS_BPA_APPLN_NEW_PWD;
                mailCode = BODY_KEY_EMAIL_BPA_APPLN_NEW_PWD;
            } else {
                smsCode = MSG_KEY_SMS_BPA_APPLN_NEW;
                mailCode = BODY_KEY_EMAIL_BPA_APPLN_NEW;
            }
            smsMsg = smsBodyByCodeAndArgsWithType(smsCode, applicantName, bpaApplication,
                    SMSEMAILTYPENEWBPAREGISTERED, loginUserName, password);
            body = emailBodyByCodeAndArgsWithType(mailCode, applicantName,
                    bpaApplication, SMSEMAILTYPENEWBPAREGISTERED, loginUserName, password);
            subject = emailSubjectforEmailByCodeAndArgs(SUBJECT_KEY_EMAIL_BPA_APPLN_NEW, bpaApplication.getApplicationNumber());
        }else if (CREATEDLETTERTOPARTY.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
            smsMsg = smsBodyByCodeAndArgsWithType(MSG_KEY_SMS_LETTERTOPARTY, applicantName,
                    bpaApplication, SMSEMAILTYPELETTERTOPARTY, EMPTY, EMPTY);
            body = emailBodyByCodeAndArgsWithType(BODY_KEY_EMAIL_LETTERTOPARTY, applicantName,
                    bpaApplication, SMSEMAILTYPELETTERTOPARTY, EMPTY, EMPTY);
            subject = emailSubjectforEmailByCodeAndArgs(SUBJECT_KEY_EMAIL_LETTERTOPARTY, bpaApplication.getApplicationNumber());
        } else if (APPLICATION_STATUS_CANCELLED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
            smsMsg = smsBodyByCodeAndArgsWithType(MSG_KEY_SMS_CANCELL_APPLN, applicantName,
                    bpaApplication, APPLICATION_STATUS_CANCELLED, EMPTY, EMPTY);
            body = emailBodyByCodeAndArgsWithType(BODY_KEY_EMAIL_CANCELL_APPLN, applicantName,
                    bpaApplication, APPLICATION_STATUS_CANCELLED, EMPTY, EMPTY);
            subject = emailSubjectforEmailByCodeAndArgs(SUBJECT_KEY_EMAIL_CANCELL_APPLN, bpaApplication.getApplicationNumber());
        }

        if (mobileNo != null && smsMsg != null)
            notificationService.sendSMS(mobileNo, smsMsg);
        if (email != null && body != null)
            notificationService.sendEmail(email, subject, body);
    }

    private void buildSmsAndEmailForScheduleAppointment(final BpaAppointmentSchedule scheduleDetails,
            final BpaApplication bpaApplication, final String applicantName, final String mobileNo, final String email) {
        String smsMsg = EMPTY;
        String body = EMPTY;
        String subject = EMPTY;
       if (AppointmentSchedulePurpose.INSPECTION.equals(scheduleDetails.getPurpose())) {
            if (!scheduleDetails.isPostponed()) {
                smsMsg = buildMessageDetailsForScheduleAppointment(scheduleDetails, bpaApplication, applicantName,
                        MSG_KEY_SMS_BPA_FIELD_INS);
                body = buildMessageDetailsForScheduleAppointment(scheduleDetails, bpaApplication, applicantName,
                        BODY_KEY_EMAIL_BPA_FIELD_INS);
                subject = emailSubjectforEmailForScheduleAppointmentForInspection(scheduleDetails, bpaApplication,
                        SUBJECT_KEY_EMAIL_BPA_FIELD_INS);
            } else {
                smsMsg = buildMessageDetailsForScheduleAppointment(scheduleDetails, bpaApplication, applicantName,
                        MSG_KEY_SMS_BPA_FIELD_INS_RESCHE);
                body = buildMessageDetailsForScheduleAppointment(scheduleDetails, bpaApplication, applicantName,
                        BODY_KEY_EMAIL_BPA_FIELD_INS_RESCHE);
                subject = emailSubjectforEmailForScheduleAppointmentForInspection(scheduleDetails, bpaApplication,
                        SUBJECT_KEY_EMAIL_BPA_FIELD_INS_RESCHE);
            }
        }
        if (isNotBlank(mobileNo) && isNotBlank(smsMsg))
            notificationService.sendSMS(mobileNo, smsMsg);
        if (isNotBlank(email) && isNotBlank(body))
            notificationService.sendEmail(email, subject, body);
    }

    private String buildMessageDetailsForScheduleAppointment(final BpaAppointmentSchedule scheduleDetails,
            final BpaApplication bpaApplication, String applicantName, String msgKey) {
        String mesg = EMPTY;
       if (AppointmentSchedulePurpose.INSPECTION.equals(scheduleDetails.getPurpose())) {

            if (!scheduleDetails.isPostponed()) {
                mesg = bpaMessageSource.getMessage(msgKey,
                        new String[] { applicantName, DateUtils.toDefaultDateFormat(scheduleDetails.getAppointmentDate()),
                                scheduleDetails.getAppointmentTime(),
                                bpaApplication.getApplicationNumber(), getMunicipalityName() },
                        null);
            } else {
                mesg = bpaMessageSource.getMessage(msgKey,
                        new String[] { applicantName,
                                DateUtils.toDefaultDateFormat(scheduleDetails.getAppointmentDate()),
                                scheduleDetails.getAppointmentTime(),
                                bpaApplication.getApplicationNumber(),
                                scheduleDetails.getPostponementReason(), getMunicipalityName() },
                        null);
            }
        }
        return mesg;
    }

    private String emailSubjectforEmailForScheduleAppointmentForInspection(final BpaAppointmentSchedule scheduleDetails,
            final BpaApplication bpaApplication, String msgKey) {
        final Locale locale = LocaleContextHolder.getLocale();
        return bpaMessageSource.getMessage(msgKey,
                new String[] { DateUtils.toDefaultDateFormat(scheduleDetails.getAppointmentDate()),
                        scheduleDetails.getAppointmentTime(), bpaApplication.getApplicationNumber() },
                locale);
    }

    private String emailSubjectforEmailByCodeAndArgs(String code, String applicationNumber) {
        final Locale locale = LocaleContextHolder.getLocale();
        return bpaMessageSource.getMessage(code, new String[] { applicationNumber }, locale);
    }

    private String emailBodyByCodeAndArgsWithType(String code, String applicantName, BpaApplication bpaApplication,
            String type, String loginUserName, String password) {
        String body = EMPTY;
        if (SMSEMAILTYPENEWBPAREGISTERED.equalsIgnoreCase(type)) {
            if (password != EMPTY)
                body = bpaMessageSource.getMessage(code,
                        new String[] { applicantName, bpaApplication.getApplicationNumber(), loginUserName, password,
                                getMunicipalityName() },
                        null);
            else
                body = bpaMessageSource.getMessage(code,
                        new String[] { applicantName, bpaApplication.getApplicationNumber(), loginUserName,
                                getMunicipalityName() },
                        null);
        } else if (SMSEMAILTYPELETTERTOPARTY.equalsIgnoreCase(type))
            body = bpaMessageSource.getMessage(code,
                    new String[] { applicantName, bpaApplication.getApplicationNumber(), getMunicipalityName() }, null);
        else if (APPLICATION_STATUS_CANCELLED.equalsIgnoreCase(type)) {
			body = getCancelApplnMessage(code, applicantName, bpaApplication);
        }
        return body;
    }

	private String getCancelApplnMessage(String code, String applicantName, BpaApplication bpaApplication) {
		StateHistory stateHistory = bpaUtils.getRejectionComments(bpaApplication);
		return bpaMessageSource.getMessage(code,
				new String[]{applicantName, bpaApplication.getApplicationNumber(),
						stateHistory != null && isNotBlank(stateHistory.getComments()) ? stateHistory.getComments() : EMPTY,
						getMunicipalityName()},
				null);
	}

    private String smsBodyByCodeAndArgsWithType(String code, String applicantName, BpaApplication bpaApplication,
            String type, String loginUserName, String password) {
        String smsMsg = EMPTY;
        if (SMSEMAILTYPENEWBPAREGISTERED.equalsIgnoreCase(type)) {
            if (isNotBlank(password))
                smsMsg = bpaMessageSource.getMessage(code,
                        new String[] { applicantName, bpaApplication.getApplicationNumber(), loginUserName, password,
                                getMunicipalityName() },
                        null);
            else
                smsMsg = bpaMessageSource.getMessage(code,
                        new String[] { applicantName, bpaApplication.getApplicationNumber(), loginUserName,
                                getMunicipalityName() },
                        null);
        } else if (SMSEMAILTYPELETTERTOPARTY.equalsIgnoreCase(type))
            smsMsg = bpaMessageSource.getMessage(code,
                    new String[] { applicantName, bpaApplication.getApplicationNumber(), getMunicipalityName() }, null);
        else if (APPLICATION_STATUS_CANCELLED.equalsIgnoreCase(type)) {

            smsMsg = getCancelApplnMessage(code, applicantName, bpaApplication);
        }
        return smsMsg;
    }

    private String buildMessageDetails(final StakeHolder stakeHolder, String msgKeyMail, Boolean isCitizen) {
        if(isCitizen){
            return bpaMessageSource.getMessage(msgKeyMail, new String[] { stakeHolder.getName(),stakeHolder.getStakeHolderType().getStakeHolderTypeVal(), getMunicipalityName() }, null);
        }
        else
            
        {
        return bpaMessageSource.getMessage(msgKeyMail, new String[] { stakeHolder.getName(), stakeHolder.getCode(),
                stakeHolder.getUsername(), stakeHolder.getMobileNumber(), getMunicipalityName() }, null);
        }
    }

    public Boolean isSmsEnabled() {
        return getAppConfigValueByPassingModuleAndType(EGMODULE_NAME, SENDSMSFORBPA);
    }

    public Boolean getAppConfigValueByPassingModuleAndType(String moduleName, String sendsmsoremail) {
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(moduleName,
                sendsmsoremail);
        return YES.equalsIgnoreCase(
                appConfigValue != null && !appConfigValue.isEmpty() ? appConfigValue.get(0).getValue() : NO);
    }

    public Boolean isEmailEnabled() {
        return getAppConfigValueByPassingModuleAndType(EGMODULE_NAME, SENDEMAILFORBPA);
    }

	public void sendSMSAndEmailForDocumentScrutiny(SlotApplication slotApplication) {
		if (isSmsEnabled() || isEmailEnabled()) {
			buildSmsAndEmailForDocumentScrutiny(slotApplication,
			        slotApplication.getApplication().getOwner().getName(),
			        slotApplication.getApplication().getOwner().getUser().getMobileNumber(),
			        slotApplication.getApplication().getOwner().getUser().getEmailId());
		}
	}

    private void buildSmsAndEmailForDocumentScrutiny(SlotApplication slotApplication,
            String name, String mobileNumber, String emailId) {
        String smsMsg = EMPTY;
        String body = EMPTY;
        String subject = EMPTY;
        if (slotApplication.getApplication().getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_SCHEDULED)) {
            smsMsg = buildMessageDetailsForSchAppForDocumentScrutiny(slotApplication, name,
                    MSG_KEY_SMS_BPA_DOC_SCRUTINY);
            body = buildMessageDetailsForSchAppForDocumentScrutiny(slotApplication, name,
                    BODY_KEY_EMAIL_BPA_DOC_SCRUTINY);
            subject = emailSubjectforEmailForScheduleAppointmentForScrutiny(SUB_KEY_EMAIL_BPA_DOCUMENT_SCRUTINY);
        } else if (slotApplication.getApplication().getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_RESCHEDULED)) {
            smsMsg = buildMessageDetailsForSchAppForDocumentScrutiny(slotApplication, name,
                    MSG_KEY_SMS_BPA_DOC_SCRUTINY_RESCHE);
            body = buildMessageDetailsForSchAppForDocumentScrutiny(slotApplication, name,
                    BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_RESCHE);
            subject = emailSubjectforEmailForScheduleAppointmentForScrutiny(SUB_KEY_EMAIL_BPA_DOCUMENT_SCRUTINY);
        } else if (slotApplication.getApplication().getStatus().getCode()
                .equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
            smsMsg = buildMessageDtlsFrPendingForRescheduling(slotApplication,
               name, MSG_KEY_SMS_BPA_DOC_SCRUTINY_PENDING_FOR_RESCHEDULING);
            body = buildMessageDtlsFrPendingForRescheduling(slotApplication,
                     name, BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_PENDING_FOR_RESCHEDULING);
            subject = emailSubjectforEmailForScheduleAppointmentForScrutiny(SUB_KEY_EMAIL_BPA_DOCUMENT_SCRUTINY);
        } else if (slotApplication.getApplication().getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_CANCELLED)) {
            smsMsg = buildMessageDtlsFrCancellation(slotApplication,
                     name, MSG_KEY_SMS_BPA_DOC_SCRUTINY_CANCELLED);
            body = buildMessageDtlsFrCancellation(slotApplication,
                     name, BODY_KEY_EMAIL_BPA_DOC_SCRUTINY_CANCELLED);
            subject = emailSubjectforEmailForScheduleAppointmentForScrutiny(SUB_KEY_EMAIL_BPA_DOCUMENT_SCRUTINY);
        }
        if (isNotBlank(mobileNumber) && isNotBlank(smsMsg))
            notificationService.sendSMS(mobileNumber, smsMsg);
        if (isNotBlank(emailId) && isNotBlank(body))
            notificationService.sendEmail(emailId, subject, body);
    }

    private String buildMessageDtlsFrPendingForRescheduling(SlotApplication slotApplication,
            String name, String msgKey) {
        return bpaMessageSource.getMessage(msgKey, new String[] { name, slotApplication.getApplication().getApplicationNumber(),
              getMunicipalityName() }, null);
    }

    private String emailSubjectforEmailForScheduleAppointmentForScrutiny(String msgKey) {
        return bpaMessageSource.getMessage(msgKey, new String[] { getMunicipalityName() }, null);
    }

    private String buildMessageDtlsFrCancellation(
            SlotApplication slotApplication,String name, String msgKey) {

        return bpaMessageSource.getMessage(msgKey, new String[] { name, slotApplication.getApplication().getApplicationNumber(),
                ApplicationThreadLocals.getDomainURL(),getMunicipalityName() }, null);
    }

    private String buildMessageDetailsForSchAppForDocumentScrutiny(SlotApplication slotApplication,
             String name, String msgKey) {
        String mesg;
        if (slotApplication.getApplication().getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_SCHEDULED)) {
            mesg = bpaMessageSource.getMessage(msgKey,
                    new String[] { name,
                            DateUtils.toDefaultDateFormat(
                                    slotApplication.getSlotDetail().getSlot().getAppointmentDate()),
                            slotApplication.getSlotDetail().getAppointmentTime(),
                            slotApplication.getSlotDetail().getSlot().getZone().getName(),
                            getAppconfigValueByKeyNameForHelpLineNumber(),
                            slotApplication.getApplication().getApplicationNumber(),
                            ApplicationThreadLocals.getDomainURL(),
                            getMunicipalityName()
                    },
                    null);
        } else {
            mesg = bpaMessageSource.getMessage(msgKey,
                    new String[] { name, slotApplication.getApplication().getApplicationNumber(),
                            DateUtils.toDefaultDateFormat(
                                    slotApplication.getSlotDetail().getSlot().getAppointmentDate()),
                            slotApplication.getSlotDetail().getAppointmentTime(),
                            slotApplication.getSlotDetail().getSlot().getZone().getName(),
                            getAppconfigValueByKeyNameForHelpLineNumber(),
                            ApplicationThreadLocals.getDomainURL(),
                            getMunicipalityName()
                    },
                    null);
        }
        return mesg;
    }

    public String getAppconfigValueByKeyNameForHelpLineNumber() {
        List<AppConfigValues> appConfigValueList = appConfigValuesService
                .getConfigValuesByModuleAndKey(APPLICATION_MODULE_TYPE, "HELPLINENUMBER");
        return !appConfigValueList.isEmpty() ? appConfigValueList.get(0).getValue() : "";
    }

  
}