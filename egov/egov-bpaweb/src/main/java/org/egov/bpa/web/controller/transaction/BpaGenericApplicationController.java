/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2017>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.bpa.web.controller.transaction;

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.master.entity.*;
import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.bpa.master.service.*;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.enums.*;
import org.egov.bpa.transaction.service.*;
import org.egov.bpa.transaction.service.collection.BpaDemandService;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.transaction.workflow.BpaWorkFlowService;
import org.egov.bpa.utils.*;
import org.egov.dcb.bean.Receipt;
import org.egov.demand.model.EgDemandDetails;
import org.egov.demand.model.EgdmCollectedReceipt;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.eis.web.controller.workflow.GenericWorkFlowController;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.*;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.pims.commons.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.validation.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.egov.bpa.utils.BpaConstants.*;

public abstract class BpaGenericApplicationController extends GenericWorkFlowController {

    @Autowired
    private BoundaryService boundaryService;
    @Autowired
    private ServiceTypeService serviceTypeService;

    @Autowired
    private OccupancyService occupancyService;

    @Autowired
    private VillageNameService villageNameService;

    @Autowired
    private ConstructionStagesService constructionStagesService;

    @Autowired
    protected CheckListDetailService checkListDetailService;
    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;
    @Autowired
    private BuildingCategoryService buildingCategoryService;
    @Autowired
    protected ApplicationBpaService applicationBpaService;
    @Autowired
    protected BpaThirdPartyService bpaThirdPartyService;
    @Autowired
    protected FileStoreUtils fileStoreUtils;
    @Autowired
    protected BpaDemandService bpaDemandService;
    @Autowired
    protected BpaWorkFlowService bpaWorkFlowService;
    @Autowired
    protected ResourceBundleMessageSource messageSource;
    @Autowired
    protected BpaStatusService bpaStatusService;
    @Autowired
    protected BpaSchemeService bpaSchemeService;
    @Autowired
    private AppConfigValueService appConfigValueService;
    @Autowired
    protected BpaUtils bpaUtils;
    @Autowired
    protected UserService userService;
    @Autowired
    protected SecurityUtils securityUtils;
    @Autowired
    protected BpaApplicationValidationService bpaApplicationValidationService;
    @Autowired
    protected BuildingFloorDetailsService proposedBuildingFloorDetailsService;
    @Autowired
    protected ExistingBuildingFloorDetailsService existingBuildingFloorDetailsService;
    @Autowired
    protected BPASmsAndEmailService bpaSmsAndEmailService;

    @ModelAttribute("occupancyList")
    public List<Occupancy> getOccupancy() {
        return occupancyService.findAllOrderByOrderNumber();
    }

    @ModelAttribute("zones")
    public List<Boundary> zones() {
        return boundaryService.getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(ZONE,
                REVENUE_HIERARCHY_TYPE);
    }

    @ModelAttribute("serviceTypeList")
    public List<ServiceType> getServiceTypeList() {
        return serviceTypeService.getAllActiveMainServiceTypes();
    }

    @ModelAttribute("amenityTypeList")
    public List<ServiceType> getAmenityTypeList() {
        return serviceTypeService.getAllActiveAmenities();
    }

    @ModelAttribute("buildingCategorYlist")
    public List<BuildingCategory> getAllBuildingCategoryList() {
        return buildingCategoryService.findAll();
    }

    @ModelAttribute("stakeHolderTypeList")
    public List<StakeHolderType> getStakeHolderType() {
        return Arrays.asList(StakeHolderType.values());
    }

    @ModelAttribute("governmentTypeList")
    public List<GovernmentType> getGovernmentType() {
        return Arrays.asList(GovernmentType.values());
    }

    @ModelAttribute("villageNames")
    public List<VillageName> getVillage() {
        return villageNameService.findAll();
    }

    @ModelAttribute("constStages")
    public List<ConstructionStages> getCOnstructionStage() {
        return constructionStagesService.findAll();
    }

    @ModelAttribute("electionwards")
    public List<Boundary> wards() {
         List<Boundary> boundaries = boundaryService
                .getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(WARD, ADMINISTRATION_HIERARCHY_TYPE);
         sortBoundaryByBndryNumberAsc(boundaries);
         return boundaries;
    }

    @ModelAttribute("wards")
    public List<Boundary> adminWards() {
        List<Boundary> boundaries =  boundaryService.getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(WARD,
                REVENUE_HIERARCHY_TYPE);
        sortBoundaryByBndryNumberAsc(boundaries);
        return boundaries;
    }

    @ModelAttribute("street")
    public List<Boundary> blocks() {
        return boundaryService.getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(STREET,
                REVENUE_HIERARCHY_TYPE);
    }

    @ModelAttribute("localitys")
    public List<Boundary> localitys() {
        return boundaryService
                .getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(LOCALITY,
                        LOCATION_HIERARCHY_TYPE);
    }
    
    private void sortBoundaryByBndryNumberAsc(List<Boundary> boundaries) {
        boundaries.sort((Boundary b1, Boundary b2) -> b1.getBoundaryNum().compareTo(b2.getBoundaryNum()));
    }

    @ModelAttribute("applicationModes")
    public Map<String, String> applicationModes() {
        return getApplicationModeMap();
    }

    @ModelAttribute("buildingFloorList")
    public List<String> getBuildingFLoorList() {
        return getBuildingFloorsList();
    }

    @ModelAttribute("uomList")
    public BpaUom[] getUomList() {
        return BpaUom.values();
    }

    @ModelAttribute("applnStatusList")
    public List<BpaStatus> getApplnStatusList() {
        return bpaStatusService.findAllByModuleType();
    }

    @ModelAttribute("schemesList")
    public List<BpaScheme> getSchemesList() {
        return bpaSchemeService.findAll();
    }

    @ModelAttribute("nocStatusList")
    public NocStatus[] getNocStatusList() {
        return NocStatus.values();
    }

    public Map<String, String> getApplicationModeMap() {
        final Map<String, String> applicationModeMap = new LinkedHashMap<>(0);
        applicationModeMap.put(ApplicantMode.NEW.toString(), ApplicantMode.NEW.name());
        applicationModeMap.put(ApplicantMode.OTHERS.name(), ApplicantMode.OTHERS.name());
        return applicationModeMap;
    }

    /**
     * @param prepareModel
     * @param model
     * @param container This method we are calling In GET Method..
     */
    @Override
    protected void prepareWorkflow(final Model prepareModel, final StateAware model, final WorkflowContainer container) {
        prepareModel.addAttribute("approverDepartmentList", addAllDepartments());
        prepareModel.addAttribute("validActionList", bpaWorkFlowService.getValidActions(model, container));
        prepareModel.addAttribute("nextAction", bpaWorkFlowService.getNextAction(model, container));

    }

    protected void prepareCommonModelAttribute(final Model model, final BpaApplication bpaApplication) {
        Boolean citizenUser = bpaUtils.logedInuserIsCitizen();
        model.addAttribute("isCitizen", citizenUser);
        List<AppConfigValues> appConfigValueList = appConfigValueService.getConfigValuesByModuleAndKey(
                APPLICATION_MODULE_TYPE, BPA_CITIZENACCEPTANCE_CHECK);
        String validateCitizenAcceptance = !appConfigValueList.isEmpty() ? appConfigValueList.get(0).getValue() : "";
        model.addAttribute("validateCitizenAcceptance",
                (validateCitizenAcceptance.equalsIgnoreCase("YES") ? Boolean.TRUE : Boolean.FALSE));
        if (StringUtils.isNotBlank(validateCitizenAcceptance)) {
            model.addAttribute("citizenDisclaimerAccepted", bpaApplication.isCitizenAccepted());
        }
        String enableOrDisablePayOnline = bpaUtils.getAppconfigValueByKeyName(ENABLEONLINEPAYMENT);
        model.addAttribute("onlinePaymentEnable",
                (enableOrDisablePayOnline.equalsIgnoreCase("YES") ? Boolean.TRUE : Boolean.FALSE));
        model.addAttribute("citizenOrBusinessUser", bpaUtils.logedInuseCitizenOrBusinessUser());
    }

    protected void buildReceiptDetails(final BpaApplication application) {
        for (final EgDemandDetails demandDtl : application.getDemand().getEgDemandDetails())
            for (final EgdmCollectedReceipt collRecpt : demandDtl.getEgdmCollectedReceipts())
                if (!collRecpt.isCancelled()) {
                    Receipt receipt = new Receipt();
                    receipt.setReceiptNumber(collRecpt.getReceiptNumber());
                    receipt.setReceiptDate(collRecpt.getReceiptDate());
                    receipt.setReceiptAmt(collRecpt.getAmount());
                    application.getReceipts().add(receipt);
                }
    }

    protected void buildOwnerDetails(final BpaApplication bpaApplication) {
        List<User> users = userService.getUserByNameAndMobileNumberAndGenderForUserType(bpaApplication.getOwner().getUser().getName(),
                bpaApplication.getOwner().getUser().getMobileNumber(), bpaApplication.getOwner().getUser().getGender(),
                UserType.CITIZEN);
        if (!users.isEmpty()) {
            bpaApplication.getOwner().setUser(users.get(0));
            if (!bpaApplication.getOwner().getUser().isActive())
                bpaApplication.getOwner().getUser().setActive(true);
        } else {
            bpaApplication.getOwner().setUser(applicationBpaService.createApplicantAsUser(bpaApplication));
            bpaApplication.setMailPwdRequired(true);
        }
    }
    
    protected String getDesinationNameByPosition(Position pos) {
        return pos.getDeptDesig() != null && pos.getDeptDesig().getDesignation() != null
                ? pos.getDeptDesig().getDesignation().getName() : "";
    }

    protected void getAppointmentMsgForOnedayPermit(final BpaApplication bpaApplication, Model model) {
        if (bpaApplication.getIsOneDayPermitApplication() && !bpaApplication.getSlotApplications().isEmpty()) {
            String appmntDetailsMsg = messageSource.getMessage("msg.one.permit.schedule", new String[]{
                    bpaApplication.getOwner().getUser().getName(),
                    DateUtils.getDefaultFormattedDate(bpaApplication.getSlotApplications().get(0).getSlotDetail().getSlot().getAppointmentDate()),
                    bpaApplication.getSlotApplications().get(0).getSlotDetail().getAppointmentTime()}, LocaleContextHolder.getLocale());
            model.addAttribute("appmntDetailsMsg", appmntDetailsMsg);
        }
    }

    @ModelAttribute("serviceTypeEnumList") 
    public List<ApplicationType>  showSearchApprovedforFee() {
		return Arrays.asList(ApplicationType.values());
    }
    
    @ModelAttribute("oneDayPermitLandTypeList") 
    public List<OneDayPermitLandType> getOneDayPermitLandType() {
        return Arrays.asList(OneDayPermitLandType.values());
    }

    protected boolean validateOnDocumentScrutiny(Model model, BpaApplication application) {
        if(APPLICATION_STATUS_DOC_VERIFIED.equals(application.getStatus().getCode())) {
            model.addAttribute(MESSAGE, "Document verification of application is already completed.");
            return true;
        } else if(WF_REJECT_STATE.equals(application.getStatus().getCode())) {
            model.addAttribute(MESSAGE, "Application is already initiated for rejection.");
            return true;
        }
        return false;
    }

}