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

import org.egov.bpa.transaction.entity.ApplicationFloorDetail;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BuildingDetail;
import org.egov.bpa.utils.BpaConstants;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.egov.bpa.utils.BpaConstants.BUILDINGHEIGHT_GROUND;
import static org.egov.bpa.utils.BpaConstants.EGMODULE_NAME;
import static org.egov.bpa.utils.BpaConstants.EXTENTINSQMTS;
import static org.egov.bpa.utils.BpaConstants.FLOOR_COUNT;
import static org.egov.bpa.utils.BpaConstants.SCALING_FACTOR;
import static org.egov.bpa.utils.BpaConstants.TOTAL_PLINT_AREA;
import static org.egov.bpa.utils.BpaConstants.getServicesForValidation;
import static org.egov.bpa.utils.BpaConstants.getStakeholderType1Restrictions;
import static org.egov.bpa.utils.BpaConstants.getStakeholderType2Restrictions;
import static org.egov.bpa.utils.BpaConstants.getStakeholderType3Restrictions;
import static org.egov.bpa.utils.BpaConstants.getStakeholderType4Restrictions;
import static org.egov.bpa.utils.BpaConstants.getStakeholderType5Restrictions;

/**
 * @author vinoth
 *
 */

@Service
@Transactional(readOnly = true)
public class BpaApplicationValidationService {

    private static final String TOWN_PLANNER_A = "Town Planner - A";
    private static final String TOWN_PLANNER_B = "Town Planner - B";
    private static final String BUILDINGDETAILSVALIDATIONREQUIRED = "BUILDINGDETAILSVALIDATIONREQUIRED";
    private static final String MSG_VIOLATION_WO_ADDNL_FEE = "msg.violation.wo.addnl.fee";
    private static final String MSG_VIOLATION_WITH_ADDNL_FEE = "msg.violation.with.addnl.fee";
    private static final String MIXED = "Mixed";
    private static final String VIOLATION_MESSAGE = "violationMessage";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String VIOLATION_MESSAGE_FOR_COVERAGE = "violationMessageForCoverage";
    private static final String MAXIMUM_ALLOWED_AREA_WITH_ADDNL_FEE = "maximumAllowedAreaWithAddnlFee";
    private static final String MAXIMUM_ALLOWED_AREA_WO_ADDNL_FEE = "maximumAllowedAreaWOAddnlFee";
    private static final String IS_VIOLATING = "isViolating";
    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource bpaMessageSource;
    @Autowired
    private ApplicationBpaFeeCalculationService applicationBpaFeeCalculationService;
    @Autowired
    private AppConfigValueService appConfigValuesService;
    @Autowired
    private BuildingFloorDetailsService buildingFloorDetailsService;

    /**
     * @param bpaApplication
     * @return
     */
    public boolean checkStakeholderIsValid(final BpaApplication bpaApplication) {
        Integer noOfFloors = 0;
        BigDecimal totalPlinthArea = BigDecimal.ZERO;
        BigDecimal heightOfBuilding = BigDecimal.ZERO;
        if(!bpaApplication.getBuildingDetail().isEmpty()) {
            BuildingDetail detail= bpaApplication.getBuildingDetail().get(0);
            noOfFloors = detail.getFloorCount();
            totalPlinthArea = detail.getTotalPlintArea();
            heightOfBuilding = detail.getHeightFromGroundWithOutStairRoom();
        }
        return validateStakeholder(bpaApplication.getServiceType().getCode(),
                bpaApplication.getStakeHolder().get(0).getStakeHolder().getStakeHolderType().getStakeHolderTypeVal(),
                bpaApplication.getSiteDetail().get(0).getExtentinsqmts(),
                noOfFloors, totalPlinthArea, heightOfBuilding);
    }

    /**
     * Validation building licensee eligible criteria to submit application.
     * @param serviceType
     * @param type
     * @param extentInArea
     * @param floorCount
     * @param buildingHeight
     * @param totalPlinthArea
     * @return isEligible ? true :false;
     */
    private boolean validateStakeholder(final String serviceType, final String type, final BigDecimal extentInArea,
            final Integer floorCount,
            final BigDecimal buildingHeight, final BigDecimal totalPlinthArea) {
        if (BpaConstants.ST_CODE_08.equalsIgnoreCase(serviceType)
                || BpaConstants.ST_CODE_09.equalsIgnoreCase(serviceType)) {
            // For service type of Amenities and Permission for Temporary hut or
            // shed any registered business user can apply and no validations.
            return true;
        } else if (TOWN_PLANNER_A.equalsIgnoreCase(type.toLowerCase())) {
            // For Town Planner - A there is no restrictions to submit
            // applications
            return true;
        } else if ((getStakeholderType1Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType))) {
            Map<String, BigDecimal> stakeHolderType1Restriction = getStakeholderType1Restrictions()
                    .get(type.toLowerCase());
            BigDecimal extentinsqmtsInput = stakeHolderType1Restriction.get(EXTENTINSQMTS);
            return extentInArea.compareTo(extentinsqmtsInput) <= 0;
        } else if (TOWN_PLANNER_B.equalsIgnoreCase(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            return false;
        } else if (getStakeholderType1Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            return true;
        } else if (getStakeholderType2Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType2Restriction = getStakeholderType2Restrictions()
                    .get(type.toLowerCase());
            BigDecimal extentinsqmtsInput = stakeHolderType2Restriction.get(EXTENTINSQMTS);
            return extentInArea.compareTo(extentinsqmtsInput) <= 0;
        } else if (getStakeholderType2Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType2Restriction = getStakeholderType2Restrictions()
                    .get(type.toLowerCase());
            BigDecimal extentinsqmtsInput = stakeHolderType2Restriction.get(EXTENTINSQMTS);
            BigDecimal plinthAreaInput = stakeHolderType2Restriction.get(TOTAL_PLINT_AREA);
            BigDecimal floorCountInput = stakeHolderType2Restriction.get(FLOOR_COUNT);
            BigDecimal buildingHeightInput = stakeHolderType2Restriction.get(BUILDINGHEIGHT_GROUND);
            return extentInArea.compareTo(extentinsqmtsInput) <= 0 && totalPlinthArea.compareTo(plinthAreaInput) <= 0
                   && buildingHeight.compareTo(buildingHeightInput) <= 0
                   && BigDecimal.valueOf(floorCount).compareTo(floorCountInput) <= 0;
        } else if ((getStakeholderType3Restrictions().containsKey(type.toLowerCase())
                || getStakeholderType4Restrictions().containsKey(type.toLowerCase()))
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType)) {
            return false;
        } else if (getStakeholderType3Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType3Restriction = getStakeholderType3Restrictions()
                    .get(type.toLowerCase());
            BigDecimal plinthAreaInput = stakeHolderType3Restriction.get(TOTAL_PLINT_AREA);
            BigDecimal floorCountInput = stakeHolderType3Restriction.get(FLOOR_COUNT);
            BigDecimal buildingHeightInput = stakeHolderType3Restriction.get(BUILDINGHEIGHT_GROUND);
            return totalPlinthArea.compareTo(plinthAreaInput) <= 0
                   && buildingHeight.compareTo(buildingHeightInput) <= 0
                   && BigDecimal.valueOf(floorCount).compareTo(floorCountInput) <= 0;
        } else if (getStakeholderType4Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType4Restriction = getStakeholderType4Restrictions()
                    .get(type.toLowerCase());
            BigDecimal plinthAreaInput = stakeHolderType4Restriction.get(TOTAL_PLINT_AREA);
            BigDecimal floorCountInput = stakeHolderType4Restriction.get(FLOOR_COUNT);
            BigDecimal buildingHeightInput = stakeHolderType4Restriction.get(BUILDINGHEIGHT_GROUND);
            return totalPlinthArea.compareTo(plinthAreaInput) <= 0
                   && buildingHeight.compareTo(buildingHeightInput) <= 0
                   && BigDecimal.valueOf(floorCount).compareTo(floorCountInput) <= 0;
        } else if ((getStakeholderType5Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType))) {
            Map<String, BigDecimal> stakeHolderType5Restriction = getStakeholderType5Restrictions()
                    .get(type.toLowerCase());
            BigDecimal extentinsqmtsInput = stakeHolderType5Restriction.get(EXTENTINSQMTS);
            return extentInArea.compareTo(extentinsqmtsInput) <= 0;
        } else if (getStakeholderType5Restrictions().containsKey(type.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType5Restriction = getStakeholderType5Restrictions()
                    .get(type.toLowerCase());
            BigDecimal plinthAreaInput = stakeHolderType5Restriction.get(TOTAL_PLINT_AREA);
            BigDecimal floorCountInput = stakeHolderType5Restriction.get(FLOOR_COUNT);
            return totalPlinthArea.compareTo(plinthAreaInput) <= 0
                   && BigDecimal.valueOf(floorCount).compareTo(floorCountInput) <= 0;
        }
        return true;
    }

    /**
     * @param bpaApplication
     * @return error message if building licensee is not meeting eligible criteria.
     */
    public String getValidationMessageForBusinessResgistration(final BpaApplication bpaApplication) {
        String stakeHolderType = bpaApplication.getStakeHolder().get(0).getStakeHolder().getStakeHolderType()
                .getStakeHolderTypeVal();
        String serviceType = bpaApplication.getServiceType().getCode();
        BigDecimal extentinsqmtsInput = BigDecimal.ZERO;
        BigDecimal plinthAreaInput = BigDecimal.ZERO;
        BigDecimal floorCountInput = BigDecimal.ZERO;
        BigDecimal buildingHeightInput = BigDecimal.ZERO;
        String message;

        if ((getStakeholderType1Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType))
                || (TOWN_PLANNER_B.equalsIgnoreCase(stakeHolderType.toLowerCase())
                        && BpaConstants.getServicesForDevelopPermit().contains(serviceType))) {
            Map<String, BigDecimal> stakeHolderType1Restriction = getStakeholderType1Restrictions()
                    .get(stakeHolderType.toLowerCase());
            extentinsqmtsInput = stakeHolderType1Restriction.get(EXTENTINSQMTS);
        } else if (getStakeholderType2Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType2Restriction = getStakeholderType2Restrictions()
                    .get(stakeHolderType.toLowerCase());
            extentinsqmtsInput = stakeHolderType2Restriction.get(EXTENTINSQMTS);
        } else if (getStakeholderType2Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType2Restriction = getStakeholderType2Restrictions()
                    .get(stakeHolderType.toLowerCase());
            extentinsqmtsInput = stakeHolderType2Restriction.get(EXTENTINSQMTS);
            plinthAreaInput = stakeHolderType2Restriction.get(TOTAL_PLINT_AREA);
            floorCountInput = stakeHolderType2Restriction.get(FLOOR_COUNT);
            buildingHeightInput = stakeHolderType2Restriction.get(BUILDINGHEIGHT_GROUND);
        } else if (getStakeholderType3Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType3Restriction = getStakeholderType3Restrictions()
                    .get(stakeHolderType.toLowerCase());
            plinthAreaInput = stakeHolderType3Restriction.get(TOTAL_PLINT_AREA);
            floorCountInput = stakeHolderType3Restriction.get(FLOOR_COUNT);
            buildingHeightInput = stakeHolderType3Restriction.get(BUILDINGHEIGHT_GROUND);
        } else if (getStakeholderType4Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType4Restriction = getStakeholderType4Restrictions()
                    .get(stakeHolderType.toLowerCase());
            plinthAreaInput = stakeHolderType4Restriction.get(TOTAL_PLINT_AREA);
            floorCountInput = stakeHolderType4Restriction.get(FLOOR_COUNT);
            buildingHeightInput = stakeHolderType4Restriction.get(BUILDINGHEIGHT_GROUND);
        } else if (getStakeholderType5Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            Map<String, BigDecimal> stakeHolderType5Restriction = getStakeholderType5Restrictions()
                    .get(stakeHolderType.toLowerCase());
            plinthAreaInput = stakeHolderType5Restriction.get(TOTAL_PLINT_AREA);
            floorCountInput = stakeHolderType5Restriction.get(FLOOR_COUNT);
        } else if ((getStakeholderType5Restrictions().containsKey(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType))) {
            Map<String, BigDecimal> stakeHolderType5Restriction = getStakeholderType5Restrictions()
                    .get(stakeHolderType.toLowerCase());
            extentinsqmtsInput = stakeHolderType5Restriction.get(EXTENTINSQMTS);
        }
        if (TOWN_PLANNER_B.equalsIgnoreCase(stakeHolderType.toLowerCase())
                && BpaConstants.getServicesForBuildPermit().contains(serviceType)) {
            message = bpaMessageSource.getMessage("msg.invalid.stakeholder4", new String[] { stakeHolderType },
                    LocaleContextHolder.getLocale());
        } else if ((getStakeholderType3Restrictions().containsKey(stakeHolderType.toLowerCase())
                || getStakeholderType4Restrictions().containsKey(stakeHolderType.toLowerCase()))
                && BpaConstants.getServicesForDevelopPermit().contains(serviceType)) {
            message = bpaMessageSource.getMessage("msg.invalid.stakeholder5",
                    new String[] { stakeHolderType }, LocaleContextHolder.getLocale());
        } else if (!extentinsqmtsInput.equals(BigDecimal.ZERO) && plinthAreaInput.equals(BigDecimal.ZERO)) {
            message = bpaMessageSource.getMessage("msg.invalid.stakeholder2",
                    new String[] { stakeHolderType, extentinsqmtsInput.toString(),
                            bpaApplication.getServiceType().getDescription() },
                    LocaleContextHolder.getLocale());
        } else if (BpaConstants.getStakeholderType5Restrictions().containsKey(stakeHolderType.toLowerCase())) {
            message = bpaMessageSource.getMessage("msg.invalid.stakeholder6",
                    new String[] { stakeHolderType, extentinsqmtsInput.toString(), plinthAreaInput.toString(),
                            floorCountInput.toString(), bpaApplication.getServiceType().getDescription() },
                    LocaleContextHolder.getLocale());
        } else {
            message = bpaMessageSource.getMessage("msg.invalid.stakeholder1",
                    new String[] { stakeHolderType, extentinsqmtsInput.toString(), plinthAreaInput.toString(),
                            floorCountInput.toString(), buildingHeightInput.toString(),
                            bpaApplication.getServiceType().getDescription() },
                    LocaleContextHolder.getLocale());
        }
        return message;
    }

    /**
     * checking each floor wise coverage area is violating for all occupancy where ever building details capturing to those
     * services this validation is applicable
     * @param application
     * @return
     */
    public Map<String, String> checkIsViolatingCoverageArea(final BpaApplication application) {
        Map<String, BigDecimal[]> floorMap = new HashMap<>();
        for (ApplicationFloorDetail floorDetail : application.getBuildingDetail().get(0).getApplicationFloorDetails()) {
            String floorDesc = floorDetail.getFloorDescription().concat("-").concat(floorDetail.getFloorNumber().toString());
            if (floorMap
                    .containsKey(floorDesc)) {
                BigDecimal permissableFloorCoveredArea = floorMap.get(floorDesc)[0]
                        .add(new BigDecimal(floorDetail.getOccupancy().getPermissibleAreaInPercentage())
                                .multiply(floorDetail.getPlinthArea()));
                BigDecimal floorWiseArea = floorMap.get(floorDesc)[1].add(floorDetail.getPlinthArea());
                floorMap.put(floorDesc, new BigDecimal[] { permissableFloorCoveredArea, floorWiseArea });
            } else {
                BigDecimal permissableFloorCoveredArea = new BigDecimal(
                        floorDetail.getOccupancy().getPermissibleAreaInPercentage()).multiply(floorDetail.getPlinthArea());
                floorMap.put(floorDesc, new BigDecimal[] { permissableFloorCoveredArea, floorDetail.getPlinthArea() });
            }
        }
        Map<String, String> violationCoverage = new HashMap<>();
        StringBuilder floorDescBuilder = new StringBuilder();
        StringBuilder floorAreaBuilder = new StringBuilder();
        for (Entry<String, BigDecimal[]> floorDescSet : floorMap.entrySet()) {
            String floorDesc = floorDescSet.getKey();
            BigDecimal permissableCoveredArea = floorDescSet.getValue()[0];
            BigDecimal plinthArea = floorDescSet.getValue()[1];
            BigDecimal extentInSqmts = application.getSiteDetail().get(0).getExtentinsqmts();
            BigDecimal weightedCoverage = permissableCoveredArea.divide(plinthArea, BigDecimal.ROUND_HALF_UP);
            BigDecimal coverageProvided = plinthArea.divide(extentInSqmts, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            BigDecimal permitedFloorArea = (weightedCoverage.multiply(extentInSqmts)
                    .divide(new BigDecimal(100))).setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP);

            if (plinthArea.setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP).compareTo(permitedFloorArea) > 0) {
                floorDescBuilder.append(floorDesc).append(", ");
                floorAreaBuilder.append(permitedFloorArea).append(", ");
            }
        }
        if (floorDescBuilder.length() > 0) {
            String message = bpaMessageSource.getMessage("msg.coverage.violate",
                    new String[] { floorDescBuilder.substring(0, floorDescBuilder.length() - 1),
                            floorAreaBuilder.substring(0, floorAreaBuilder.length() - 1) },
                    null);
            violationCoverage.put(VIOLATION_MESSAGE_FOR_COVERAGE, message);
            violationCoverage.put(IS_VIOLATING, TRUE);
        } else {
            violationCoverage.put(IS_VIOLATING, FALSE);
        }
        return violationCoverage;
    }

    /**
     * 
     * @param application
     * @param model
     * @return
     */
    public Boolean validateBuildingDetails(final BpaApplication application, final Model model) {
        if (isBuildingFloorDetailsValidationRequired()
                && getServicesForValidation().contains(application.getServiceType().getCode())) {
            List<ApplicationFloorDetail> deletedFloorDetails = new ArrayList<>();
            for (ApplicationFloorDetail applicationFloorDetails : application.getBuildingDetail().get(0)
                    .getApplicationFloorDetails()) {
                if (application.getBuildingDetail().get(0).getDeletedFloorIds() != null
                        && application.getBuildingDetail().get(0).getDeletedFloorIds().length > 0
                        && Arrays.asList(application.getBuildingDetail().get(0).getDeletedFloorIds())
                                .contains(applicationFloorDetails.getId())) {
                    deletedFloorDetails.add(applicationFloorDetails);
                }
            }
            application.getBuildingDetail().get(0).delete(deletedFloorDetails);
            buildingFloorDetailsService.buildNewlyAddedFloorDetails(application);
            Map<String, String> violationCoverage = checkIsViolatingCoverageArea(application);
            if (TRUE.equalsIgnoreCase(violationCoverage.get(IS_VIOLATING))) {
                model.addAttribute(VIOLATION_MESSAGE, violationCoverage.get(VIOLATION_MESSAGE_FOR_COVERAGE));
                return true;
            }

            Map<String, String> violationWOAddnlFee = checkIsViolatingMaximumPermissableWOAddnlFee(application);
            Map<String, String> violationWithAddnlFee = checkIsViolatingMaximumPermissableWithAddnlFee(application);
            if(new BigDecimal(violationWithAddnlFee.get(MAXIMUM_ALLOWED_AREA_WITH_ADDNL_FEE)).compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal sumOfFloorArea = applicationBpaFeeCalculationService
                        .getOccupancyWiseSumOfFloorArea(application.getBuildingDetail().get(0)).entrySet().stream()
                        .map(Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                if(sumOfFloorArea.compareTo(new BigDecimal(violationWOAddnlFee.get(MAXIMUM_ALLOWED_AREA_WO_ADDNL_FEE))) > 0) {
                    String message = bpaMessageSource.getMessage("msg.violation.max.with.wo.addnl.fee", new String[] {
                            application.getOccupancy().getDescription(), violationWOAddnlFee.get(MAXIMUM_ALLOWED_AREA_WO_ADDNL_FEE) },
                            null);
                    model.addAttribute(VIOLATION_MESSAGE, message);
                    return true;
                }
                return false;
            } else if (TRUE.equalsIgnoreCase(violationWOAddnlFee.get(IS_VIOLATING))
                    && !application.getBuildingDetail().get(0).getAdditionalFeePaymentAccepted()) {
                String message = bpaMessageSource.getMessage(MSG_VIOLATION_WO_ADDNL_FEE, new String[] {
                        application.getOccupancy().getDescription(), violationWOAddnlFee.get(MAXIMUM_ALLOWED_AREA_WO_ADDNL_FEE) },
                        null);
                model.addAttribute(VIOLATION_MESSAGE, message);
                return true;
            } else if (TRUE.equalsIgnoreCase(violationWithAddnlFee.get(IS_VIOLATING))
                    && application.getBuildingDetail().get(0).getAdditionalFeePaymentAccepted()) {
                String message = bpaMessageSource.getMessage(MSG_VIOLATION_WITH_ADDNL_FEE, new String[] {
                        application.getOccupancy().getDescription(),
                        violationWithAddnlFee.get(MAXIMUM_ALLOWED_AREA_WITH_ADDNL_FEE) },
                        null);
                model.addAttribute(VIOLATION_MESSAGE, message);
                return true;
            }
        }
        return false;
    }

    /**
     * checking total floor area is violating against maximum permissable area with out additional fee and where ever building
     * details capturing to those services this validation is applicable
     * @param application
     * @return isViolated and maximum allowed area with out additional fee
     */
    public Map<String, String> checkIsViolatingMaximumPermissableWOAddnlFee(final BpaApplication application) {
        Map<String, String> violation = new HashMap<>();
        BigDecimal sumOfFloorArea = applicationBpaFeeCalculationService
                .getOccupancyWiseSumOfFloorArea(application.getBuildingDetail().get(0)).entrySet().stream()
                .map(Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maximumAllowedAreaWOAddnlFee = BigDecimal.ZERO;
        if (MIXED.equalsIgnoreCase(application.getOccupancy().getDescription())) {
            if (application.getSiteDetail().get(0).getExtentinsqmts().compareTo(new BigDecimal(5000)) <= 0) {
                maximumAllowedAreaWOAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                        .multiply(applicationBpaFeeCalculationService.minimumFARWithoutAdditionalFee(application));
            } else if (application.getSiteDetail().get(0).getExtentinsqmts().compareTo(new BigDecimal(5000)) > 0) {
                maximumAllowedAreaWOAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                        .multiply(applicationBpaFeeCalculationService.weightageAverageFarWithoutAdditionalFee(
                                applicationBpaFeeCalculationService
                                        .getOccupancyWiseSumOfFloorArea(application.getBuildingDetail().get(0))));
            }
        } else {
            maximumAllowedAreaWOAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                    .multiply(new BigDecimal(application.getOccupancy().getNumOfTimesAreaPermissible()));
        }
        violation.put(IS_VIOLATING, sumOfFloorArea.compareTo(maximumAllowedAreaWOAddnlFee) > 0 ? TRUE : FALSE);
        violation.put(MAXIMUM_ALLOWED_AREA_WO_ADDNL_FEE,
                maximumAllowedAreaWOAddnlFee.setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP).toString());
        return violation;
    }

    /**
     * checking total floor area is violating against maximum permissable area with additional fee and is additional fee
     * applicable or not.
     * @param application
     * @return isViolated and maximum allowed area with additional fee
     */
    public Map<String, String> checkIsViolatingMaximumPermissableWithAddnlFee(final BpaApplication application) {
        Map<String, String> violation = new HashMap<>();
        BigDecimal sumOfFloorArea = applicationBpaFeeCalculationService
                .getOccupancyWiseSumOfFloorArea(application.getBuildingDetail().get(0)).entrySet().stream()
                .map(Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal maximumAllowedAreaWithAddnlFee = BigDecimal.ZERO;
        if (MIXED.equalsIgnoreCase(application.getOccupancy().getDescription())) {
            if (application.getSiteDetail().get(0).getExtentinsqmts().compareTo(new BigDecimal(5000)) <= 0) {
                maximumAllowedAreaWithAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                        .multiply(applicationBpaFeeCalculationService.minimumFARWithAdditionalFee(application));
            } else if (application.getSiteDetail().get(0).getExtentinsqmts().compareTo(new BigDecimal(5000)) > 0) {
                maximumAllowedAreaWithAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                        .multiply(applicationBpaFeeCalculationService.weightageAverageFarWithAdditionalFee(
                                applicationBpaFeeCalculationService
                                        .getOccupancyWiseSumOfFloorArea(application.getBuildingDetail().get(0))));
            }
        } else {
            maximumAllowedAreaWithAddnlFee = application.getSiteDetail().get(0).getExtentinsqmts()
                    .multiply(new BigDecimal(application.getOccupancy().getNumOfTimesAreaPermWitAddnlFee()));
        }

        violation.put(IS_VIOLATING, sumOfFloorArea.compareTo(maximumAllowedAreaWithAddnlFee) > 0 ? TRUE : FALSE);
        violation.put(MAXIMUM_ALLOWED_AREA_WITH_ADDNL_FEE,
                maximumAllowedAreaWithAddnlFee.setScale(SCALING_FACTOR, BigDecimal.ROUND_HALF_UP).toString());
        return violation;
    }

    public Boolean isBuildingFloorDetailsValidationRequired() {
        return getAppConfigValueByPassingModuleAndType(EGMODULE_NAME, BUILDINGDETAILSVALIDATIONREQUIRED);
    }

    public Boolean getAppConfigValueByPassingModuleAndType(String moduleName, String sendsmsoremail) {
        final List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(moduleName,
                sendsmsoremail);
        return "YES".equalsIgnoreCase(
                appConfigValue != null && !appConfigValue.isEmpty() ? appConfigValue.get(0).getValue() : "NO");
    }

}
