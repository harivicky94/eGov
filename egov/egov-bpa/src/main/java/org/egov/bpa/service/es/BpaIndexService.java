package org.egov.bpa.service.es;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BpaIndexService {
    private static final Logger LOG = LoggerFactory.getLogger(BpaIndexService.class);

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
    private BpaIndexService bpaIndexService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    private ApplicationIndexService applicationIndexService;

    public BpaIndex createBpaIndex(final BpaApplication bpaApplication) {

        final City cityWebsite = cityService.getCityByURL(ApplicationThreadLocals.getDomainName());
        BpaIndex bpaIndex = new BpaIndex();
        bpaIndex.setUlbName(cityWebsite.getName());
        bpaIndex.setDistrictName(cityWebsite.getDistrictName());
        bpaIndex.setRegionName(cityWebsite.getRegionName());
        bpaIndex.setUlbGrade(cityWebsite.getGrade());
        bpaIndex.setUlbCode(cityWebsite.getCode());
        bpaIndex.setId(bpaApplication.getId().toString());
        bpaIndex.setApplicantName(bpaApplication.getOwner().getUser().getName());
        bpaIndex.setApplicantMobileNumber(bpaApplication.getOwner().getUser().getMobileNumber());
        bpaIndex.setApplicantEmailId(bpaApplication.getOwner().getUser().getEmailId());
        bpaIndex.setApplicantAddress(bpaApplication.getOwner().getPermanentAddress().getStreetRoadLine());
        bpaIndex.setApplicantGender(bpaApplication.getOwner().getUser().getGender().name());
        bpaIndex.setServiceType(bpaApplication.getServiceType().getDescription());
        bpaIndex.setApplicationNumber(bpaApplication.getApplicationNumber());
        bpaIndex.setApplicationDate(bpaApplication.getApplicationDate());
        bpaIndex.seteDcrNumber(bpaApplication.geteDcrNumber() != null ? bpaApplication.geteDcrNumber() : EMPTY);
        if (bpaApplication.getIsOneDayPermitApplication())
            bpaIndex.setTypeOfLand(bpaApplication.getTypeOfLand().name());
        bpaIndex.setAdmissionFeeAmount(
                bpaApplication.getAdmissionfeeAmount() != null ? bpaApplication.getAdmissionfeeAmount() : BigDecimal.ZERO);
        if (bpaApplication.getStakeHolder().size() > 0) {
            bpaIndex.setStakeHolderName(bpaApplication.getStakeHolder().get(0).getStakeHolder() != null
                    ? bpaApplication.getStakeHolder().get(0).getStakeHolder().getName() : EMPTY);
            bpaIndex.setStakeHolderType(bpaApplication.getStakeHolder().get(0).getStakeHolder() != null
                    ? bpaApplication.getStakeHolder().get(0).getStakeHolder().getStakeHolderType().name() : EMPTY);
        }
        bpaIndex.setRemarks(bpaApplication.getRemarks() != null ? bpaApplication.getRemarks() : EMPTY);
        bpaIndex.setPlanPermissionNumber(
                bpaApplication.getPlanPermissionNumber() != null ? bpaApplication.getPlanPermissionNumber() : EMPTY);
        if (bpaApplication.getPlanPermissionDate() != null)
            bpaIndex.setPlanPermissionDate(bpaApplication.getPlanPermissionDate());
        bpaIndex.setStatus(bpaApplication.getStatus() != null ? bpaApplication.getStatus().getCode() : EMPTY);
        bpaIndex.setIsOneDayPermitApplication(bpaApplication.getIsOneDayPermitApplication());
        if (!bpaApplication.getSiteDetail().isEmpty()) {
            bpaIndex.setIsappForRegularization(bpaApplication.getSiteDetail().get(0).getIsappForRegularization());
            if (bpaApplication.getSiteDetail().get(0).getIsappForRegularization())
                bpaIndex.setConstStages(bpaApplication.getSiteDetail().get(0).getConstStages() != null
                        ? bpaApplication.getSiteDetail().get(0).getConstStages().getCode() : EMPTY);
            if (bpaApplication.getSiteDetail().get(0).getConstStages() != null) {
                if (bpaApplication.getSiteDetail().get(0).getConstStages().getCode().equals(CONSTRUCTION_STAGES_INPROGRESS)) {
                    bpaIndex.setStateOfConstruction(bpaApplication.getSiteDetail().get(0).getStateOfConstruction() != null
                            ? bpaApplication.getSiteDetail().get(0).getStateOfConstruction() : EMPTY);
                    bpaIndex.setWorkCommencementDate(bpaApplication.getSiteDetail().get(0).getWorkCommencementDate());

                } else if (bpaApplication.getSiteDetail().get(0).getConstStages().getCode()
                        .equals(CONSTRUCTION_STAGES_COMPLETED)) {
                    bpaIndex.setWorkCommencementDate(bpaApplication.getSiteDetail().get(0).getWorkCommencementDate());
                    bpaIndex.setWorkCompletionDate(bpaApplication.getSiteDetail().get(0).getWorkCompletionDate());
                }
            }
            bpaIndex.setExtentOfLand(bpaApplication.getSiteDetail().get(0).getExtentOfLand() != null
                    ? bpaApplication.getSiteDetail().get(0).getExtentOfLand() : BigDecimal.ZERO);
            bpaIndex.setExtentinsqmts(bpaApplication.getSiteDetail().get(0).getExtentinsqmts() != null
                    ? bpaApplication.getSiteDetail().get(0).getExtentinsqmts() : BigDecimal.ZERO);
            bpaIndex.setZone(bpaApplication.getSiteDetail().get(0).getAdminBoundary() != null
                    ? bpaApplication.getSiteDetail().get(0).getAdminBoundary().getParent().getName() : EMPTY);
            bpaIndex.setRevenueWard(bpaApplication.getSiteDetail().get(0).getAdminBoundary() != null
                    ? bpaApplication.getSiteDetail().get(0).getAdminBoundary().getName() : EMPTY);
            bpaIndex.setVillage(bpaApplication.getSiteDetail().get(0).getLocationBoundary() != null
                    ? bpaApplication.getSiteDetail().get(0).getLocationBoundary().getName() : EMPTY);
            bpaIndex.setElectionWard(bpaApplication.getSiteDetail().get(0).getElectionBoundary() != null
                    ? bpaApplication.getSiteDetail().get(0).getElectionBoundary().getName() : EMPTY);
            bpaIndex.setReSurveyNumber(bpaApplication.getSiteDetail().get(0).getReSurveyNumber() != null
                    ? bpaApplication.getSiteDetail().get(0).getReSurveyNumber() : EMPTY);
            bpaIndex.setNatureofOwnership(bpaApplication.getSiteDetail().get(0).getNatureofOwnership() != null
                    ? bpaApplication.getSiteDetail().get(0).getNatureofOwnership() : EMPTY);
            bpaIndex.setRegistrarOffice(bpaApplication.getSiteDetail().get(0).getRegistrarOffice() != null
                    ? bpaApplication.getSiteDetail().get(0).getRegistrarOffice().getRegistrarOffice().getName() : EMPTY);
            bpaIndex.setNearestbuildingnumber(bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() != null
                    ? bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() : EMPTY);
            bpaIndex.setDoorNumber(bpaApplication.getSiteDetail().get(0).getPlotdoornumber() != null
                    ? bpaApplication.getSiteDetail().get(0).getPlotdoornumber() : EMPTY);
            bpaIndex.setStreetAddress(bpaApplication.getSiteDetail().get(0).getStreetaddress1() != null
                    ? bpaApplication.getSiteDetail().get(0).getStreetaddress1() : EMPTY);
            bpaIndex.setLocality(bpaApplication.getSiteDetail().get(0).getStreetaddress2() != null
                    ? bpaApplication.getSiteDetail().get(0).getStreetaddress2() : EMPTY);
            bpaIndex.setCityTown(bpaApplication.getSiteDetail().get(0).getCitytown() != null
                    ? bpaApplication.getSiteDetail().get(0).getCitytown() : EMPTY);
            bpaIndex.setPinCode(bpaApplication.getSiteDetail().get(0).getPostalAddress().getPincode() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getPincode() : EMPTY);
            bpaIndex.setPlotTaluk(bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() : EMPTY);
            bpaIndex.setPostOffice(bpaApplication.getSiteDetail().get(0).getPostalAddress().getPostOffice() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getPostOffice() : EMPTY);
            bpaIndex.setDistrict(bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict() : EMPTY);
            bpaIndex.setState(bpaApplication.getSiteDetail().get(0).getPostalAddress().getState() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict() : EMPTY);
            bpaIndex.setScheme(bpaApplication.getSiteDetail().get(0).getScheme() != null
                    ? bpaApplication.getSiteDetail().get(0).getScheme().getDescription() : EMPTY);
            bpaIndex.setLandUsage(bpaApplication.getSiteDetail().get(0).getLandUsage() != null
                    ? bpaApplication.getSiteDetail().get(0).getLandUsage().getDescription() : EMPTY);
        }
        bpaIndex.setOccupancy(bpaApplication.getOccupancy() != null ? bpaApplication.getOccupancy().getDescription() : EMPTY);
        bpaIndex.setGovernmentType(
                bpaApplication.getGovernmentType() != null ? bpaApplication.getGovernmentType().name() : EMPTY);
        if (bpaApplication.getGovernmentType() != null) {
            if (bpaApplication.getGovernmentType().getGovernmentTypeVal().equals(GOVERNMENT_TYPE_GOV)
                    || bpaApplication.getGovernmentType().getGovernmentTypeVal().equals(GOVERNMENT_TYPE_QUASI_GOV))
                bpaIndex.setIsEconomicallyWeakerSection(bpaApplication.getIsEconomicallyWeakerSection());
        }
        if (!bpaApplication.getBuildingDetail().isEmpty()) {
            bpaIndex.setTotalPlintArea(bpaApplication.getBuildingDetail().get(0).getTotalPlintArea() != null
                    ? bpaApplication.getBuildingDetail().get(0).getTotalPlintArea() : BigDecimal.ZERO);
            bpaIndex.setFloorCount(bpaApplication.getBuildingDetail().get(0).getFloorCount() != null
                    ? bpaApplication.getBuildingDetail().get(0).getFloorCount() : 0);
            bpaIndex.setHeightFromGroundWithStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithStairRoom() != null
                            ? bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithStairRoom() : BigDecimal.ZERO);
            bpaIndex.setHeightFromGroundWithOutStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithOutStairRoom() != null
                            ? bpaApplication.getBuildingDetail().get(0).getHeightFromGroundWithOutStairRoom() : BigDecimal.ZERO);
            bpaIndex.setHeightFromStreetLevelWithStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithStairRoom() != null
                            ? bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithStairRoom() : BigDecimal.ZERO);
            bpaIndex.setHeightFromStreetLevelWithOutStairRoom(
                    bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithOutStairRoom() != null
                            ? bpaApplication.getBuildingDetail().get(0).getFromStreetLevelWithOutStairRoom() : BigDecimal.ZERO);
        }
        bpaIndexRepository.save(bpaIndex);
        return bpaIndex;
    }
    
    
    public void updateIndexes(final BpaApplication bpaApplication) {
        final SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {

            if (bpaApplication.getApplicationDate() != null) {
                final String applicationDate = myFormat.format(bpaApplication.getApplicationDate());
                bpaApplication.setApplicationDate(myFormat.parse(applicationDate));
            }

        } catch (final ParseException e) {
            LOG.error("Exception parsing Date " + e.getMessage());
        }
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
            } else if (assignment == null)
                asignList = assignmentService.getAssignmentsForPosition(bpaApplication.getState()
                        .getOwnerPosition().getId(), new Date());
            if (!asignList.isEmpty())
                user = userService.getUserById(asignList.get(0).getEmployee().getId());
        } else
            user = securityUtils.getCurrentUser();

        ApplicationIndex applicationIndex = applicationIndexService.findByApplicationNumber(bpaApplication
                .getApplicationNumber());
        if (applicationIndex != null && bpaApplication.getId() != null) {
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
            bpaIndexService.createBpaIndex(bpaApplication);

        } else {
            List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(MODULE_NAME,
                    APP_CONFIG_KEY);
            String slaDays = null;
            if (appConfigValue.size() > 0)
                slaDays = appConfigValue.get(0).getValue();
            Date disposalDate = getDisposalDate(bpaApplication, Integer.valueOf(slaDays));
            Date disDate = null;
            try {
                if (disposalDate != null) {
                    String disposalDt = myFormat.format(disposalDate);
                    disDate = myFormat.parse(disposalDt);
                }
            } catch (final ParseException e) {
                LOG.error("Exception parsing Date " + e.getMessage());
            }
            applicationIndex = ApplicationIndex.builder().withModuleName(BpaConstants.APPL_INDEX_MODULE_NAME)
                    .withApplicationNumber(bpaApplication.getApplicationNumber())
                    .withApplicationDate(bpaApplication.getApplicationDate())
                    .withApplicationType(bpaApplication.getIsOneDayPermitApplication().equals(true)
                            ? BpaConstants.APPLICATION_TYPE_ONEDAYPERMIT : BpaConstants.APPLICATION_TYPE_REGULAR)
                    .withOwnername(user.getUsername() + "::" + user.getName())
                    .withApplicantName(bpaApplication.getOwner().getUser().getName())
                    .withApplicantAddress(bpaApplication.getOwner().getPermanentAddress().getStreetRoadLine())
                    .withStatus(bpaApplication.getStatus().getCode())
                    .withChannel(bpaApplication.getSource() == null ? Source.SYSTEM.toString()
                            : bpaApplication.getSource().name())
                    .withConsumerCode(bpaApplication.getApplicationNumber())
                    .withMobileNumber(bpaApplication.getOwner().getUser().getMobileNumber())
                    .withAadharNumber(bpaApplication.getOwner().getUser().getAadhaarNumber() != null
                            ? bpaApplication.getOwner().getUser().getAadhaarNumber() : EMPTY)
                    .withUrl(String.format(UPDATE_URL, bpaApplication.getApplicationNumber()))
                    .withClosed(ClosureStatus.NO)
                    .withSla(slaDays != null ? Integer.valueOf(slaDays) : 0)
                    .withDisposalDate(disDate)
                    .withApproved(ApprovalStatus.INPROGRESS)
                    .build();
            applicationIndexService.createApplicationIndex(applicationIndex);
            bpaIndexService.createBpaIndex(bpaApplication);
        }
    }

    public Date getDisposalDate(final BpaApplication bpaApplication,
            final Integer sla) {
        final Calendar c = Calendar.getInstance();
        c.setTime(bpaApplication.getApplicationDate());
        c.add(Calendar.DATE, sla);
        return c.getTime();
    }

}
