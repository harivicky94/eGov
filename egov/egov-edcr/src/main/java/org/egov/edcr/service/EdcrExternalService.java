/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2018>  eGovernments Foundation
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

package org.egov.edcr.service;

import org.egov.edcr.entity.*;
import org.egov.edcr.entity.dto.*;
import org.egov.infra.utils.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.math.*;

@Service
public class EdcrExternalService {

    @Autowired
    private EdcrApplicationDetailService edcrApplicationDetailService;

    public EdcrApplicationInfo loadEdcrApplicationDetails(String eDcrNumber) {
		EdcrApplicationDetail applicationDetail = edcrApplicationDetailService.findByDcrNumber(eDcrNumber);
        return buildDcrApplicationDetails(applicationDetail);
    }

    public EdcrApplicationInfo buildDcrApplicationDetails(EdcrApplicationDetail applicationDetail) {
        EdcrApplicationInfo applicationInfo = new EdcrApplicationInfo();
        applicationInfo.seteDcrApplicationId(applicationDetail.getApplication().getId());
        applicationInfo.setApplicationDate(DateUtils.toDefaultDateFormat(applicationDetail.getApplication().getApplicationDate()));
		applicationInfo.setCreatedDate(DateUtils.toDefaultDateTimeFormat(applicationDetail.getApplication().getCreatedDate()));
        applicationInfo.setApplicationNumber(applicationDetail.getApplication().getApplicationNumber());
        applicationInfo.setDcrNumber(applicationDetail.getDcrNumber());
        applicationInfo.seteDcrApplicationId(applicationDetail.getApplication().getId());
        applicationInfo.setDxfFile(applicationDetail.getDxfFileId());
        applicationInfo.setReportOutput(applicationDetail.getReportOutputId());
        if (applicationDetail.getApplication().getPlanInformation() != null) {
            applicationInfo.setPlanInformationId(applicationDetail.getApplication().getPlanInformation().getId());
            applicationInfo.setAmenities(applicationDetail.getApplication().getPlanInformation().getAmenities() == null ? "N/A" : applicationDetail.getApplication().getPlanInformation().getAmenities());
            applicationInfo.setServiceType(applicationDetail.getApplication().getPlanInformation().getServiceType() == null ? "N/A" : applicationDetail.getApplication().getPlanInformation().getServiceType());
            applicationInfo.setOccupancy(applicationDetail.getApplication().getPlanInformation().getOccupancy() == null ? "N/A" : applicationDetail.getApplication().getPlanInformation().getOccupancy());
            applicationInfo.setArchitectInformation(applicationDetail.getApplication().getPlanInformation().getArchitectInformation() == null ? "N/A" : applicationDetail.getApplication().getPlanInformation().getArchitectInformation());
            applicationInfo.setPlotArea(applicationDetail.getApplication().getPlanInformation().getPlotArea() == null ? BigDecimal.ZERO : applicationDetail.getApplication().getPlanInformation().getPlotArea());
            applicationInfo.setOwnerName(applicationDetail.getApplication().getPlanInformation().getOwnerName() == null ? "N/A" : applicationDetail.getApplication().getPlanInformation().getOwnerName());
        }
        return applicationInfo;
    }
}