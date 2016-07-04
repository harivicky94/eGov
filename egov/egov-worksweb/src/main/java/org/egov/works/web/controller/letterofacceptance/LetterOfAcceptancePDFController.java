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
package org.egov.works.web.controller.letterofacceptance;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportConstants;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.web.utils.WebUtils;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.letterofacceptance.service.LetterOfAcceptanceService;
import org.egov.works.utils.WorksUtils;
import org.egov.works.workorder.entity.WorkOrder;
import org.egov.works.workorder.entity.WorkOrderActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/letterofacceptance")
public class LetterOfAcceptancePDFController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private WorksUtils worksUtils;

    @Autowired
    private LetterOfAcceptanceService letterOfAcceptanceService;

    public static final String LETTEROFACCEPTANCEPDF = "letterOfAcceptancePDF";
    private final Map<String, Object> reportParams = new HashMap<String, Object>();
    private ReportRequest reportInput = null;
    private ReportOutput reportOutput = null;

    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;

    @RequestMapping(value = "/letterOfAcceptancePDF/{letterOfAcceptanceId}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<byte[]> generateLineEstimatePDF(final HttpServletRequest request,
            @PathVariable("letterOfAcceptanceId") final Long id, final HttpSession session) throws IOException {
        final WorkOrder workOrder = letterOfAcceptanceService.getWorkOrderById(id);
        return generateReport(workOrder, request, session);
    }

    private ResponseEntity<byte[]> generateReport(final WorkOrder workOrder, final HttpServletRequest request,
            final HttpSession session) {
        if (workOrder != null) {
            final AbstractEstimate estimate = workOrder.getWorkOrderEstimates().get(0).getEstimate();
            final List<WorkOrderActivity> workOrderActivities = workOrder.getWorkOrderEstimates().get(0)
                    .getWorkOrderActivities();

            final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            final DecimalFormat df = new DecimalFormat("0.00");

            final String url = WebUtils.extractRequestDomainURL(request, false);
            reportParams.put("cityLogo", url.concat(ReportConstants.IMAGE_CONTEXT_PATH)
                    .concat((String) request.getSession().getAttribute("citylogo")));

            final String cityName = (String) request.getSession().getAttribute("citymunicipalityname");
            reportParams.put("cityName", cityName);
            reportParams.put("workOrderNumber",
                    workOrder.getWorkOrderNumber() != null ? workOrder.getWorkOrderNumber() : "");
            reportParams.put("workOrderDate",
                    workOrder.getWorkOrderDate() != null ? formatter.format(workOrder.getWorkOrderDate()) : "");
            reportParams.put("contractorName",
                    workOrder.getContractor().getName() != null ? workOrder.getContractor().getName() : "");
            reportParams.put("contractorAddress", workOrder.getContractor().getBankaccount() != null
                    ? workOrder.getContractor().getCorrespondenceAddress() : "");
            reportParams.put("panNo",
                    workOrder.getContractor().getPanNumber() != null ? workOrder.getContractor().getPanNumber() : "");
            reportParams.put("bank",
                    workOrder.getContractor().getBank() != null ? workOrder.getContractor().getBank().getName() : "");
            reportParams.put("accountNo", workOrder.getContractor().getBankaccount() != null
                    ? workOrder.getContractor().getBankaccount() : "");
            reportParams.put("subject", estimate.getName());
            if (estimate.getLineEstimateDetails() != null)
                reportParams.put("modeOfAllotment",
                        estimate.getLineEstimateDetails().getLineEstimate().getModeOfAllotment().toString());
            else
                reportParams.put("modeOfAllotment", "");
            reportParams.put("agreementAmount", df.format(workOrder.getWorkOrderAmount()));
            reportParams.put("emd", df.format(workOrder.getEmdAmountDeposited()));
            reportParams.put("asd", df.format(workOrder.getSecurityDeposit()));
            reportParams.put("WINCode", estimate.getProjectCode().getCode());
            reportParams.put("amountOfEstimate", estimate.getEstimateValue().setScale(2, BigDecimal.ROUND_HALF_EVEN));
            reportParams.put("headOfAccount", estimate.getFinancialDetails().get(0).getBudgetGroup().getName());
            reportParams.put("ward", estimate.getWard().getName());
            reportParams.put("activities", workOrderActivities);
            reportParams.put("activitySize", workOrderActivities.size());
            reportParams.put("tenderFinalizedPercentage", workOrder.getTenderFinalizedPercentage());
            reportParams.put("currDate", formatter.format(new Date()));

            if (!estimate.getEstimateTechnicalSanctions().isEmpty()) {
                final String technicalSanctionByDesignation = worksUtils
                        .getUserDesignation(estimate.getEstimateTechnicalSanctions()
                                .get(estimate.getEstimateTechnicalSanctions().size() - 1).getTechnicalSanctionBy());
                reportParams.put("technicalSanctionByDesignation", technicalSanctionByDesignation);
            } else
                reportParams.put("technicalSanctionByDesignation", "");

            reportInput = new ReportRequest(LETTEROFACCEPTANCEPDF, workOrderActivities, reportParams);

        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/pdf"));
        headers.add("content-disposition", "inline;filename=LetterOfAcceptance.pdf");
        reportOutput = reportService.createReport(reportInput);
        return new ResponseEntity<byte[]>(reportOutput.getReportOutputData(), headers, HttpStatus.CREATED);

    }

}
