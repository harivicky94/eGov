/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
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
package org.egov.bpa.web.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.egov.bpa.application.entity.BpaScheme;
import org.egov.bpa.application.entity.BpaSchemeLandUsage;
import org.egov.bpa.application.entity.Occupancy;
import org.egov.bpa.application.entity.PostalAddress;
import org.egov.bpa.application.entity.StakeHolder;
import org.egov.bpa.application.entity.enums.StakeHolderType;
import org.egov.bpa.application.service.ApplicationBpaService;
import org.egov.bpa.application.service.PostalAddressService;
import org.egov.bpa.masters.service.BpaSchemeService;
import org.egov.bpa.masters.service.OccupancyService;
import org.egov.bpa.masters.service.StakeHolderService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.eis.entity.Assignment;
import org.egov.eis.entity.AssignmentAdaptor;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.DesignationService;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.CrossHierarchyService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.workflow.matrix.service.CustomizedWorkFlowService;
import org.egov.pims.commons.Designation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Controller
public class BpaAjaxController {

    @Autowired
    private DesignationService designationService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CustomizedWorkFlowService customizedWorkFlowService;

    @Autowired
    private ApplicationBpaService applicationBpaService;
    @Autowired
    private StakeHolderService stakeHolderService;
    @Autowired
    private OccupancyService occupancyService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostalAddressService postalAddressService;
    @Autowired
    private CrossHierarchyService crossHierarchyService;
    @Autowired
    private  BpaSchemeService bpaSchemeService;

    @RequestMapping(value = "/ajax/getAdmissionFees", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BigDecimal isConnectionPresentForProperty(@RequestParam final Long[] serviceTypeIds) {
        if (serviceTypeIds.length > 0) {
            return applicationBpaService.getTotalFeeAmountByPassingServiceTypeAndAmenities(Arrays.asList(serviceTypeIds));
        } else {
            return BigDecimal.ZERO;
        }
    }

    @RequestMapping(value = "/bpaajaxWorkFlow-getDesignationsByObjectTypeAndDesignation", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Designation> getDesignationsByObjectTypeAndDesignation(
            @ModelAttribute("designations") @RequestParam final String departmentRule,
            @RequestParam final String currentState, @RequestParam final String type,
            @RequestParam final BigDecimal amountRule, @RequestParam final String additionalRule,
            @RequestParam final String pendingAction, @RequestParam final Long approvalDepartment) {
        List<Designation> designationList = customizedWorkFlowService.getNextDesignations(type,
                departmentRule, amountRule, additionalRule, currentState,
                pendingAction, new Date());
        if (designationList.isEmpty())
            designationList = designationService.getAllDesignationByDepartment(approvalDepartment, new Date());
        return designationList;
    }

    @RequestMapping(value = "/ajax-designationsByDepartment", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Designation> getDesignationsByDepartmentId(
            @ModelAttribute("designations") @RequestParam final Long approvalDepartment) {
        List<Designation> designations = new ArrayList<>();
        if (approvalDepartment != null && approvalDepartment != 0 && approvalDepartment != -1)
            designations = designationService.getAllDesignationByDepartment(approvalDepartment, new Date());
        designations.forEach(designation -> designation.toString());
        return designations;
    }

    @RequestMapping(value = "/bpaajaxWorkFlow-positionsByDepartmentAndDesignationAndBoundary", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getPositionByDepartmentAndDesignationAndBoundary(@RequestParam final Long approvalDepartment,
            @RequestParam final Long approvalDesignation, @RequestParam final Long boundaryId, final HttpServletResponse response) {
        if (approvalDepartment != null && approvalDepartment != 0 && approvalDepartment != -1
                && approvalDesignation != null && approvalDesignation != 0 && approvalDesignation != -1) {
            List<Assignment> assignmentList = assignmentService.findAssignmentByDepartmentDesignationAndBoundary(approvalDepartment, approvalDesignation, boundaryId);
            final Gson jsonCreator = new GsonBuilder().registerTypeAdapter(Assignment.class, new AssignmentAdaptor())
                    .create();
            return jsonCreator.toJson(assignmentList, new TypeToken<Collection<Assignment>>() {
            }.getType());
        }
        return "[]";
    }

    @RequestMapping(value = "/ajax/stakeholdersbytype", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<StakeHolder> getStakeHolderByType(@RequestParam final String name,
            @RequestParam final StakeHolderType stakeHolderType) {
        return stakeHolderService.getStakeHolderListByType(stakeHolderType, name);
    }

    @RequestMapping(value = "/application/getoccupancydetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Occupancy> getOccupancyDetails() {
        return occupancyService.findAll();
    }
    
    @RequestMapping(value = "/getApplicantDetails", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, String> getApplicantDetailsForMobileNumber(@RequestParam final String mobileNumber) {
        Map<String, String> user = new HashMap<>();
        List<User> userList = userService.getUserByMobileNumberAndType(mobileNumber, UserType.CITIZEN);
        if (!userList.isEmpty()) {
            User dbUser = userList.get(0);
            user.put("name", dbUser.getName());
            user.put("address", dbUser.getAddress().get(0).getStreetRoadLine());
            user.put("emailId", dbUser.getEmailId());
            user.put("gender", dbUser.getGender().name());
            user.put("id", dbUser.getId().toString());

        }
        return user;
    }

    @RequestMapping(value = "/getApplicantDetailsForEmailId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User getApplicantDetailsForEmailId(@RequestParam final String emailId) {
        return userService.getUserByUsername(emailId);
    }

    @RequestMapping(value = "/ajax/postaladdress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<PostalAddress> getPostalAddress(@RequestParam final String pincode) {
        return postalAddressService.getPostalAddressList(pincode);
    }

    @RequestMapping(value = { "/boundary/ajaxBoundary-localityByWard" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void localityByWard(@RequestParam Long wardId, HttpServletResponse response) throws IOException {

        final List<Boundary> blocks = crossHierarchyService
                .findChildBoundariesByParentBoundaryIdParentBoundaryTypeAndChildBoundaryType(BpaConstants.WARD,
                        BpaConstants.REVENUE_HIERARCHY_TYPE,
                        BpaConstants.LOCALITY_BNDRY_TYPE,
                        wardId);
        final List<JSONObject> jsonObjects = new ArrayList<>();
        for (final Boundary block : blocks) {
            final JSONObject jsonObj = new JSONObject();
            jsonObj.put("localityId", block.getId());
            jsonObj.put("localityName", block.getName());
            jsonObjects.add(jsonObj);
        }
        IOUtils.write(jsonObjects.toString(), response.getWriter());
    }
    
    @RequestMapping(value = { "/ajax/getlandusagebyscheme" }, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void landUsageByScheme(@RequestParam Long schemeId, HttpServletResponse response) throws IOException {

        if (schemeId != null) {
            final BpaScheme scheme = bpaSchemeService
                    .findById(schemeId);

            final List<JSONObject> jsonObjects = new ArrayList<>();
            if (scheme != null) {
                for (final BpaSchemeLandUsage landUsage : scheme.getSchemeLandUsage()) {
                    final JSONObject jsonObj = new JSONObject();
                    jsonObj.put("usageId", landUsage.getId());
                    jsonObj.put("usageDesc", landUsage.getDescription());
                    jsonObjects.add(jsonObj);
                }
            }
            IOUtils.write(jsonObjects.toString(), response.getWriter());
        }
    }
}
