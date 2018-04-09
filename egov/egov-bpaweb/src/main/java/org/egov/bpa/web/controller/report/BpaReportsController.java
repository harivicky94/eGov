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
package org.egov.bpa.web.controller.report;

import org.egov.bpa.transaction.entity.dto.*;
import org.egov.bpa.transaction.service.*;
import org.egov.bpa.transaction.service.report.*;
import org.egov.bpa.utils.*;
import org.egov.bpa.web.controller.adaptor.*;
import org.egov.bpa.web.controller.transaction.*;
import org.egov.eis.entity.Employee;
import org.egov.eis.entity.Jurisdiction;
import org.egov.eis.service.EmployeeService;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.BoundaryType;
import org.egov.infra.admin.master.service.BoundaryTypeService;
import org.egov.infra.admin.master.service.CrossHierarchyService;
import org.egov.infra.utils.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.egov.bpa.utils.BpaConstants.BOUNDARY_TYPE_CITY;
import static org.egov.bpa.utils.BpaConstants.REVENUE_HIERARCHY_TYPE;
import static org.egov.bpa.utils.BpaConstants.WARD;
import static org.egov.infra.utils.JsonUtils.*;

@Controller
@RequestMapping(value = "/reports")
public class BpaReportsController extends BpaGenericApplicationController {
    private static final String SEARCH_BPA_APPLICATION_FORM = "searchBpaApplicationForm";
    private static final String FAILURE_IN_SCHEDULER_REPORT = "search-scheduler-failure-records-report";
    private static final String DATA = "{ \"data\":";

    @Autowired
    private BpaReportsService bpaReportsService;
    @Autowired
    private SearchBpaApplicationService searchBpaApplicationService;
    @Autowired
    private FailureInSchedulerService failureInSchedulerService;
    @Autowired
    private BoundaryTypeService boundaryTypeService;
    @Autowired
    private CrossHierarchyService crossHierarchyService;
    @Autowired
    private EmployeeService employeeService;

    @RequestMapping(value = "/servicewise-statusreport", method = RequestMethod.GET)
    public String searchStatusCountByServicetypeForm(final Model model) {
        prepareFormData(model);
        model.addAttribute(SEARCH_BPA_APPLICATION_FORM, new SearchBpaApplicationForm());
        return "search-servicewise-status-report";
    }

    @RequestMapping(value = "/servicewise-statusreport", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getStatusCountByServicetypeResult(final Model model,
            @ModelAttribute final SearchBpaApplicationForm searchBpaApplicationForm) {
        final List<SearchBpaApplicationReport> searchResultList = bpaReportsService
                .getResultsByServicetypeAndStatus(searchBpaApplicationForm);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SearchBpaApplicationReport.class, SearchBpaApplicationReportAdaptor.class))
                .append("}")
                .toString();
    }

    @RequestMapping(value = "/servicewise-statusreport/view", method = RequestMethod.GET)
    public String viewStatusCountByServicetypeDetails(@RequestParam final String applicantName,
            @RequestParam final String applicationNumber,
            @RequestParam final Long ward, @RequestParam final Date fromDate,
            @RequestParam final Date toDate, @RequestParam final Long revenueWard, @RequestParam final Long electionWard,
            @RequestParam final Long zoneId, @RequestParam final String status, @RequestParam final String serviceType,
            @RequestParam final String zone, final Model model) {
        model.addAttribute("applicantName", applicantName);
        model.addAttribute("applicationNumber", applicationNumber);
        model.addAttribute("ward", ward);
        if (fromDate == null) {
            model.addAttribute("fromDate", fromDate);
        } else {
            model.addAttribute("fromDate", DateUtils.toDefaultDateFormat(fromDate));
        }
        if (toDate == null) {
            model.addAttribute("toDate", toDate);
        } else {
            model.addAttribute("toDate", DateUtils.toDefaultDateFormat(toDate));
        }
        model.addAttribute("revenueWard", revenueWard);
        model.addAttribute("electionWard", electionWard);
        model.addAttribute("zone", zone);
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("status", status);
        model.addAttribute("serviceType", serviceType);
        return "view-servicewise-appln-details";
    }

    @RequestMapping(value = "/servicewise-statusreport/view", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String viewStatusCountByServicetypeDetails(@ModelAttribute final SearchBpaApplicationForm searchBpaApplicationForm,
            final Model model) {
        final List<SearchBpaApplicationForm> searchResultList = searchBpaApplicationService.search(searchBpaApplicationForm);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SearchBpaApplicationForm.class, SearchBpaApplicationFormAdaptor.class))
                .append("}")
                .toString();
    }

    @RequestMapping(value = "/zonewisedetails", method = RequestMethod.GET)
    public String searchZoneWiseServicesForm(final Model model) {
        prepareFormData(model);
        model.addAttribute(SEARCH_BPA_APPLICATION_FORM, new SearchBpaApplicationForm());
        return "search-zonewise-report";
    }

    @RequestMapping(value = "/zonewisedetails", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getZoneWiseServicesResult(final Model model,
            @ModelAttribute final SearchBpaApplicationForm searchBpaApplicationForm) {
        final List<SearchBpaApplicationReport> searchResultList = bpaReportsService
                .getResultsForEachServicetypeByZone(searchBpaApplicationForm);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SearchBpaApplicationReport.class, SearchBpaApplicationReportAdaptor.class))
                .append("}")
                .toString();
    }

    @RequestMapping(value = "/slotdetails/{applicationType}", method = RequestMethod.GET)
    public String searchSlotDetailsForm(@PathVariable String applicationType, final Model model) {
        prepareFormData(model);
        model.addAttribute("slotDetailsHelper", new SlotDetailsHelper());
        model.addAttribute("applicationType",applicationType);
        model.addAttribute("searchByNoOfDays", BpaConstants.getSearchByNoOfDays());
        if("onedaypermit".equals(applicationType))
            return "search-onedaypermit-slotdetails-report";
        else
        	return "search-regular-slotdetails-report";
    }

    @RequestMapping(value = "/slotdetails/{applicationType}", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String getSlotDetailsResult(@PathVariable String applicationType, final Model model,
                                            @ModelAttribute final SlotDetailsHelper slotDetailsHelper) {
        final List<SlotDetailsHelper> searchResultList = bpaReportsService.searchSlotDetails(slotDetailsHelper,applicationType);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SlotDetailsHelper.class, SlotDetailsAdaptor.class))
                .append("}")
                .toString();
    }

	@RequestMapping(value = "/slotdetails/viewapplications", method = RequestMethod.GET)
	public String viewUtilizedSlotDetailsByApplicationHelper(@RequestParam final String applicationType, @RequestParam final String scheduleType,
															 @RequestParam final Date appointmentDate,
															 @RequestParam final String appointmentTime,
															 @RequestParam final Long zoneId,
															 @RequestParam final Long electionWardId, final Model model) {
        if (appointmentDate == null) {
            model.addAttribute("appointmentDate", appointmentDate);
        } else {
            model.addAttribute("appointmentDate", DateUtils.toDefaultDateFormat(appointmentDate));
        }
        model.addAttribute("appointmentTime", appointmentTime);
        model.addAttribute("zoneId", zoneId);
        model.addAttribute("electionWardId", electionWardId);
		model.addAttribute("applicationType", applicationType);
		model.addAttribute("scheduleType", scheduleType);
        return "view-slot-application-details";
    }

    @RequestMapping(value = "/slotdetails/viewapplications", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String viewUtilizedSlotDetailsByApplication(@ModelAttribute final SearchBpaApplicationForm searchBpaApplicationForm) {
        final List<SearchBpaApplicationForm> searchResultList = searchBpaApplicationService.buildSlotApplicationDetails(searchBpaApplicationForm);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SearchBpaApplicationForm.class, SearchBpaApplicationFormAdaptor.class))
                .append("}")
                .toString();
    }
    
    @RequestMapping(value = "/failureinscheduler", method = RequestMethod.GET)
    public String getFailureInSchedulerRecordsForm(final Model model) {
        List<Boundary> employeeMappedZone = getEmployeeMappedZone();
        model.addAttribute("employeeMappedZone", employeeMappedZone);
        model.addAttribute(SEARCH_BPA_APPLICATION_FORM, new SearchBpaApplicationForm());
        return FAILURE_IN_SCHEDULER_REPORT;
    }

    @RequestMapping(value = "/failureinscheduler", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String showApplicationFailureInSchedulerRecords(final Model model,
            final SearchBpaApplicationForm searchBpaApplicationForm) {
        List<SearchBpaApplicationForm> searchResultList = failureInSchedulerService
                .getAllFailedApplications(searchBpaApplicationForm);
        return new StringBuilder(DATA)
                .append(toJSON(searchResultList, SearchBpaApplicationForm.class, SearchBpaApplicationFormAdaptor.class))
                .append("}")
                .toString();
    }

    private List<Boundary> getEmployeeMappedZone() {
        List<Boundary> employeeMappedZone = new ArrayList<>();
        List<Boundary> mappedElectionWard = new ArrayList<>();
        BoundaryType revenueType = boundaryTypeService.getBoundaryTypeByNameAndHierarchyTypeName(WARD, REVENUE_HIERARCHY_TYPE);
        final Employee employee = employeeService.getEmployeeById(securityUtils.getCurrentUser().getId());
        for (Jurisdiction jurisdiction : employee.getJurisdictions()) {
            if (!BOUNDARY_TYPE_CITY.equals(jurisdiction.getBoundaryType().getName())) {
                mappedElectionWard.add(jurisdiction.getBoundary());
                break;
            }
        }
        List<Boundary> revenueWards = crossHierarchyService
                .getParentBoundaryByChildBoundaryAndParentBoundaryType(mappedElectionWard.get(0).getId(), revenueType.getId());
        employeeMappedZone.add(revenueWards.get(0).getParent());
        return employeeMappedZone;
    }

}