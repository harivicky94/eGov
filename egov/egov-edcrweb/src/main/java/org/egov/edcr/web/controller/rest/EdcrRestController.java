/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
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
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
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
 *
 */

package org.egov.edcr.web.controller.rest;

import org.egov.edcr.entity.*;
import org.egov.edcr.entity.dto.*;
import org.egov.edcr.service.*;
import org.egov.edcr.utility.*;
import org.egov.eis.contract.*;
import org.egov.infra.utils.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.math.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class EdcrRestController {

	@Autowired
	private EdcrExternalService edcrExternalService;
	@Autowired
	private EdcrApplicationDetailService edcrApplicationDetailService;

	@GetMapping(value = "/rest/approved-plan-details/by-edcr-number/{dcrNumber}", produces = APPLICATION_JSON_VALUE)
	public EdcrApplicationInfo validateEdcrPlanFile(@PathVariable final String dcrNumber) {
		EdcrApplicationDetail applicationDetail = edcrApplicationDetailService.findByDcrNumber(dcrNumber);
		if (applicationDetail == null) {
			ErrorDetail errorDetail = new ErrorDetail();
			errorDetail.setErrorCode(DcrConstants.ERROR_CODE_PLAN_NOT_EXIST);
			errorDetail.setErrorMessage(DcrConstants.ERROR_MSG_PLAN_NOT_EXIST);
			EdcrApplicationInfo applicationInfo = new EdcrApplicationInfo();
			applicationInfo.setErrorDetail(errorDetail);
			return applicationInfo;
		} else
			return edcrExternalService.loadEdcrApplicationDetails(dcrNumber);
	}
}
