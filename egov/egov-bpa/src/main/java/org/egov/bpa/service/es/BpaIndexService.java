package org.egov.bpa.service.es;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.egov.bpa.entity.es.BpaIndex;
import org.egov.bpa.repository.es.BpaIndexRepository;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.utils.BpaConstants;
import org.egov.commons.entity.Source;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.City;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.elasticsearch.entity.ApplicationIndex;
import org.egov.infra.elasticsearch.entity.enums.ApprovalStatus;
import org.egov.infra.elasticsearch.entity.enums.ClosureStatus;
import org.egov.infra.elasticsearch.service.ApplicationIndexService;
import org.egov.infra.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BpaIndexService {

    private static final String UPDATE_URL = "/bpa/application/update/%s";
    private static final String MODULE_NAME = "BPA";
    private static final String APP_CONFIG_KEY = "SLAFORBPAAPPLICATION";
    private static final String GOVERNMENT_TYPE_GOV = "Government";
    private static final String GOVERNMENT_TYPE_QUASI_GOV = "Quasi Government";
    private static final String CONSTRUCTION_STAGES_INPROGRESS = "In Progress";
    private static final String CONSTRUCTION_STAGES_COMPLETED = "Completed";

    @Autowired
    private CityService cityService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private BpaIndexRepository bpaIndexRepository;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private ApplicationIndexService applicationIndexService;

    public BpaIndex createBpaIndex(final BpaApplication bpaApplication) {
        final City cityWebsite = cityService.getCityByURL(ApplicationThreadLocals.getDomainName());
        BpaIndex bpaIndex = new BpaIndex();
        buildUlbDetails(cityWebsite, bpaIndex);
        bpaIndex.setId(bpaApplication.getId().toString());
        buildApplicantDetails(bpaApplication, bpaIndex);
        bpaIndex.setServiceType(bpaApplication.getServiceType().getDescription());
        bpaIndex.setApplicationNumber(bpaApplication.getApplicationNumber());
        bpaIndex.setApplicationDate(bpaApplication.getApplicationDate());
        bpaIndex.seteDcrNumber(bpaApplication.geteDcrNumber() == null ? EMPTY : bpaApplication.geteDcrNumber());
        if (bpaApplication.getIsOneDayPermitApplication())
            bpaIndex.setTypeOfLand(bpaApplication.getTypeOfLand().name());
        bpaIndex.setAdmissionFeeAmount(
                bpaApplication.getAdmissionfeeAmount() == null ? BigDecimal.ZERO : bpaApplication.getAdmissionfeeAmount());
        if (!bpaApplication.getStakeHolder().isEmpty()) {
            bpaIndex.setStakeHolderName(bpaApplication.getStakeHolder().get(0).getStakeHolder() == null ? EMPTY
                    : bpaApplication.getStakeHolder().get(0).getStakeHolder().getName());
            bpaIndex.setStakeHolderType(bpaApplication.getStakeHolder().get(0).getStakeHolder() == null ? EMPTY
                    : bpaApplication.getStakeHolder().get(0).getStakeHolder().getStakeHolderType().name());
        }
        bpaIndex.setRemarks(bpaApplication.getRemarks() == null ? EMPTY : bpaApplication.getRemarks());
        bpaIndex.setPlanPermissionNumber(
                bpaApplication.getPlanPermissionNumber() == null ? EMPTY : bpaApplication.getPlanPermissionNumber());
        if (bpaApplication.getPlanPermissionDate() != null)
            bpaIndex.setPlanPermissionDate(bpaApplication.getPlanPermissionDate());
        bpaIndex.setStatus(bpaApplication.getStatus() == null ? EMPTY : bpaApplication.getStatus().getCode());
        bpaIndex.setIsOneDayPermitApplication(bpaApplication.getIsOneDayPermitApplication());
        buildBpaSiteDetails(bpaApplication, bpaIndex);
        bpaIndex.setOccupancy(bpaApplication.getOccupancy() == null ? EMPTY : bpaApplication.getOccupancy().getDescription());
        buildGovernmentData(bpaApplication, bpaIndex);
        buildBpaBuildingDetails(bpaApplication, bpaIndex);
        bpaIndexRepository.save(bpaIndex);
        return bpaIndex;
    }

    private void buildGovernmentData(final BpaApplication bpaApplication, BpaIndex bpaIndex) {
        bpaIndex.setGovernmentType(
                bpaApplication.getGovernmentType() == null ? EMPTY : bpaApplication.getGovernmentType().name());
        if (bpaApplication.getGovernmentType() != null
                && bpaApplication.getGovernmentType().getGovernmentTypeVal().equals(GOVERNMENT_TYPE_GOV)
                || bpaApplication.getGovernmentType().getGovernmentTypeVal().equals(GOVERNMENT_TYPE_QUASI_GOV))
            bpaIndex.setIsEconomicallyWeakerSection(bpaApplication.getIsEconomicallyWeakerSection());
    }

    private void buildUlbDetails(final City cityWebsite, BpaIndex bpaIndex) {
        bpaIndex.setUlbName(cityWebsite.getName());
        bpaIndex.setDistrictName(cityWebsite.getDistrictName());
        bpaIndex.setRegionName(cityWebsite.getRegionName());
        bpaIndex.setUlbGrade(cityWebsite.getGrade());
        bpaIndex.setUlbCode(cityWebsite.getCode());
    }

    private void buildApplicantDetails(final BpaApplication bpaApplication, BpaIndex bpaIndex) {
        bpaIndex.setApplicantName(bpaApplication.getOwner().getUser().getName());
        bpaIndex.setApplicantMobileNumber(bpaApplication.getOwner().getUser().getMobileNumber());
        bpaIndex.setApplicantEmailId(bpaApplication.getOwner().getUser().getEmailId());
        bpaIndex.setApplicantAddress(bpaApplication.getOwner().getPermanentAddress().getStreetRoadLine());
        bpaIndex.setApplicantGender(bpaApplication.getOwner().getUser().getGender().name());
    }

    private void buildBpaBuildingDetails(BpaApplication bpaApplication, BpaIndex bpaIndex) {
        if (!bpaApplication.getBuildingDetail().isEmpty()) {
            bpaIndex.setTotalPlintArea(bpaApplication.getBuildingDetail().get(0).getTotalPlintArea() == null
                    ? BigDecimal.ZERO : bpaApplication.getBuildingDetail().get(0).getTotalPlintArea());
            bpaIndex.setFloorCount(bpaApplication.getBuildingDetail().get(0).getFloorCount() == null
                    ? 0 : bpaApplication.getBuildingDetail().get(0).getFloorCount());
            bpaIndex.setHeightFromGroundWithStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithStairRoom() == null
                            ? BigDecimal.ZERO : bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithStairRoom());
            bpaIndex.setHeightFromGroundWithOutStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithOutStairRoom() == null
                            ? BigDecimal.ZERO : bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithOutStairRoom());
            bpaIndex.setHeightFromStreetLevelWithStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithStairRoom() == null
                            ? BigDecimal.ZERO : bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithStairRoom());
            bpaIndex.setHeightFromStreetLevelWithOutStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithOutStairRoom() == null
                            ? BigDecimal.ZERO : bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithOutStairRoom());
        }
    }

    private void buildBpaSiteDetails(BpaApplication bpaApplication, BpaIndex bpaIndex) {
        if (!bpaApplication.getSiteDetail().isEmpty()) {
            buildRegularizationDetails(bpaApplication, bpaIndex);
            bpaIndex.setExtentOfLand(bpaApplication.getSiteDetail().get(0).getExtentOfLand() == null
                    ? BigDecimal.ZERO : bpaApplication.getSiteDetail().get(0).getExtentOfLand());
            bpaIndex.setExtentinsqmts(bpaApplication.getSiteDetail().get(0).getExtentinsqmts() == null
                    ? BigDecimal.ZERO : bpaApplication.getSiteDetail().get(0).getExtentinsqmts());
            buildSiteBoundaryDetails(bpaApplication, bpaIndex);
            bpaIndex.setReSurveyNumber(bpaApplication.getSiteDetail().get(0).getReSurveyNumber() == null
                    ? EMPTY : bpaApplication.getSiteDetail().get(0).getReSurveyNumber());
            bpaIndex.setNatureofOwnership(bpaApplication.getSiteDetail().get(0).getNatureofOwnership() == null
                    ? EMPTY : bpaApplication.getSiteDetail().get(0).getNatureofOwnership());
            buildSiteAddressDetails(bpaApplication, bpaIndex);
            bpaIndex.setScheme(bpaApplication.getSiteDetail().get(0).getScheme() == null
                    ? EMPTY : bpaApplication.getSiteDetail().get(0).getScheme().getDescription());
            bpaIndex.setLandUsage(bpaApplication.getSiteDetail().get(0).getLandUsage() == null
                    ? EMPTY : bpaApplication.getSiteDetail().get(0).getLandUsage().getDescription());
        }

    }

    private void buildSiteBoundaryDetails(BpaApplication bpaApplication, BpaIndex bpaIndex) {
        bpaIndex.setZone(bpaApplication.getSiteDetail().get(0).getAdminBoundary() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getAdminBoundary().getParent().getName());
        bpaIndex.setRevenueWard(bpaApplication.getSiteDetail().get(0).getAdminBoundary() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getAdminBoundary().getName());
        bpaIndex.setVillage(bpaApplication.getSiteDetail().get(0).getLocationBoundary() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getLocationBoundary().getName());
        bpaIndex.setElectionWard(bpaApplication.getSiteDetail().get(0).getElectionBoundary() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getElectionBoundary().getName());
        bpaIndex.setRegistrarOffice(bpaApplication.getSiteDetail().get(0).getRegistrarOffice() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getRegistrarOffice().getRegistrarOffice().getName());
    }

    private void buildSiteAddressDetails(BpaApplication bpaApplication, BpaIndex bpaIndex) {
        bpaIndex.setNearestbuildingnumber(bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber());
        bpaIndex.setDoorNumber(bpaApplication.getSiteDetail().get(0).getPlotdoornumber() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPlotdoornumber());
        bpaIndex.setStreetAddress(bpaApplication.getSiteDetail().get(0).getStreetaddress1() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getStreetaddress1());
        bpaIndex.setLocality(bpaApplication.getSiteDetail().get(0).getStreetaddress2() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getStreetaddress2());
        bpaIndex.setCityTown(bpaApplication.getSiteDetail().get(0).getCitytown() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getCitytown());
        bpaIndex.setPinCode(bpaApplication.getSiteDetail().get(0).getPostalAddress().getPincode() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getPincode());
        bpaIndex.setPlotTaluk(bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk());
        bpaIndex.setPostOffice(bpaApplication.getSiteDetail().get(0).getPostalAddress().getPostOffice() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getPostOffice());
        bpaIndex.setDistrict(bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict());
        bpaIndex.setState(bpaApplication.getSiteDetail().get(0).getPostalAddress().getState() == null
                ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict());
    }

    private void buildRegularizationDetails(BpaApplication bpaApplication, BpaIndex bpaIndex) {
        bpaIndex.setIsappForRegularization(bpaApplication.getSiteDetail().get(0).getIsappForRegularization());
        if (bpaApplication.getSiteDetail().get(0).getIsappForRegularization())
            bpaIndex.setConstStages(bpaApplication.getSiteDetail().get(0).getConstStages() == null
                    ? EMPTY : bpaApplication.getSiteDetail().get(0).getConstStages().getCode());
        if (bpaApplication.getSiteDetail().get(0).getConstStages() != null)
            if (bpaApplication.getSiteDetail().get(0).getConstStages().getCode().equals(CONSTRUCTION_STAGES_INPROGRESS)) {
                bpaIndex.setStateOfConstruction(bpaApplication.getSiteDetail().get(0).getStateOfConstruction() == null
                        ? EMPTY : bpaApplication.getSiteDetail().get(0).getStateOfConstruction());
                bpaIndex.setWorkCommencementDate(bpaApplication.getSiteDetail().get(0).getWorkCommencementDate());

            } else if (bpaApplication.getSiteDetail().get(0).getConstStages().getCode()
                    .equals(CONSTRUCTION_STAGES_COMPLETED)) {
                bpaIndex.setWorkCommencementDate(bpaApplication.getSiteDetail().get(0).getWorkCommencementDate());
                bpaIndex.setWorkCompletionDate(bpaApplication.getSiteDetail().get(0).getWorkCompletionDate());
            }
    }

    public void updateIndexes(final BpaApplication bpaApplication) {
        User user = getCurrentUser(bpaApplication);
        ApplicationIndex applicationIndex = applicationIndexService.findByApplicationNumber(bpaApplication
                .getApplicationNumber());
        if (applicationIndex != null && bpaApplication.getId() != null)
            buildApplicationIndexForUpdate(bpaApplication, user, applicationIndex);
        else {
            List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(MODULE_NAME,
                    APP_CONFIG_KEY);
            Date disposalDate = calculateDisposalDate(appConfigValue);
            applicationIndex = ApplicationIndex.builder().withModuleName(BpaConstants.APPL_INDEX_MODULE_NAME)
                    .withApplicationNumber(bpaApplication.getApplicationNumber())
                    .withApplicationDate(bpaApplication.getApplicationDate())
                    .withApplicationType(bpaApplication.getIsOneDayPermitApplication().equals(true)
                            ? BpaConstants.APPLICATION_TYPE_ONEDAYPERMIT : BpaConstants.APPLICATION_TYPE_REGULAR)
                    .withOwnername(user == null ? EMPTY : user.getUsername() + "::" + user.getName())
                    .withApplicantName(bpaApplication.getOwner().getUser().getName())
                    .withApplicantAddress(bpaApplication.getOwner().getPermanentAddress().getStreetRoadLine())
                    .withStatus(bpaApplication.getStatus().getCode())
                    .withChannel(bpaApplication.getSource() == null ? Source.SYSTEM.toString()
                            : bpaApplication.getSource().name())
                    .withConsumerCode(bpaApplication.getApplicationNumber())
                    .withMobileNumber(bpaApplication.getOwner().getUser().getMobileNumber())
                    .withAadharNumber(bpaApplication.getOwner().getUser().getAadhaarNumber() == null
                            ? EMPTY : bpaApplication.getOwner().getUser().getAadhaarNumber())
                    .withUrl(String.format(UPDATE_URL, bpaApplication.getApplicationNumber()))
                    .withClosed(ClosureStatus.NO)
                    .withSla(appConfigValue.get(0).getValue() == null ? 0 : Integer.valueOf(appConfigValue.get(0).getValue()))
                    .withDisposalDate(disposalDate)
                    .withApproved(ApprovalStatus.INPROGRESS)
                    .build();
            applicationIndexService.createApplicationIndex(applicationIndex);
            createBpaIndex(bpaApplication);
        }
    }

    private void buildApplicationIndexForUpdate(final BpaApplication bpaApplication, User user,
            ApplicationIndex applicationIndex) {
        applicationIndex.setStatus(bpaApplication.getStatus().getCode());
        applicationIndex.setOwnerName(user != null ? user.getUsername() + "::" + user.getName() : "");
        if (bpaApplication.getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_CANCELLED)) {
            applicationIndex.setApproved(ApprovalStatus.REJECTED);
            applicationIndex.setClosed(ClosureStatus.YES);
        } else if (bpaApplication.getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_ORDER_ISSUED)) {
            applicationIndex.setApproved(ApprovalStatus.APPROVED);
            applicationIndex.setClosed(ClosureStatus.YES);
        }

        if (bpaApplication.getPlanPermissionNumber() != null)
            applicationIndex.setConsumerCode(bpaApplication.getPlanPermissionNumber());
        applicationIndexService.updateApplicationIndex(applicationIndex);
        createBpaIndex(bpaApplication);
    }

    private User getCurrentUser(final BpaApplication bpaApplication) {
        List<Assignment> asignList = null;
        Assignment assignment = null;
        User user = null;
        if (bpaApplication.getState() != null
                && bpaApplication.getState().getOwnerPosition() != null) {
            assignment = assignmentService.getPrimaryAssignmentForPositionAndDate(bpaApplication.getState()
                    .getOwnerPosition().getId(), new Date());
            if (assignment != null) {
                asignList = new ArrayList<>();
                asignList.add(assignment);
            } else
                asignList = assignmentService.getAssignmentsForPosition(bpaApplication.getState()
                        .getOwnerPosition().getId(), new Date());
            if (!asignList.isEmpty())
                user = userService.getUserById(asignList.get(0).getEmployee().getId());
        } else
            user = securityUtils.getCurrentUser();
        return user;
    }

    public Date calculateDisposalDate(List<AppConfigValues> appConfigValue) {
        return DateUtils.addDays(new Date(),
                appConfigValue.get(0) != null && appConfigValue.get(0).getValue() != null
                        ? Integer.valueOf(appConfigValue.get(0).getValue()) : 0);
    }
}
