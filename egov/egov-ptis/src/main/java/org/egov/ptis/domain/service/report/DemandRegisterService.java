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

package org.egov.ptis.domain.service.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.egov.commons.CFinancialYear;
import org.egov.commons.dao.InstallmentDao;
import org.egov.infra.admin.master.entity.City;
import org.egov.infra.admin.master.repository.BoundaryRepository;
import org.egov.infra.admin.master.service.ModuleService;
import org.egov.infra.reporting.engine.ReportConstants.FileFormat;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.utils.DateUtils;
import org.egov.ptis.bean.DemandRegisterInfo;
import org.egov.ptis.constants.PropertyTaxConstants;
import org.egov.ptis.domain.repository.report.AssessmentTransactionsRepository;
import org.egov.ptis.service.utils.PropertyTaxCommonUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public class DemandRegisterService {

    @Autowired
    private AssessmentTransactionsRepository transactionsRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ReportService reportService;
    @Autowired
    private PropertyTaxCommonUtils ptCommonUtils;
    @Autowired
    private InstallmentDao installmentDao;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private BoundaryRepository boundaryRepository;

    public ReportOutput generateDemandRegisterReport(final String reportType, final Long wardId,
            final Date finYearStartDate, final String mode) {
        ReportOutput reportOutput;
        final Map<String, Object> reportParams = new HashMap<>();
        ReportRequest reportInput;
        reportInput = generateADRReport(reportParams, wardId, finYearStartDate, mode);
        reportInput.setPrintDialogOnOpenReport(true);
        reportInput.setReportFormat(FileFormat.PDF);
        reportOutput = reportService.createReport(reportInput);
        return reportOutput;

    }

    private ReportRequest generateADRReport(final Map<String, Object> reportParams, final Long wardId,
            final Date finYearStartDate, final String mode) {
        ReportRequest reportInput;
        List<DemandRegisterInfo> demandRegisterInfoList = new ArrayList<>();

        final List<String> assessmentList = transactionsRepository.getPriorAssessmentsByWard(wardId,
                new java.sql.Date(finYearStartDate.getTime()), getPropertyType(mode));
        if (!assessmentList.isEmpty()) {
            List<CFinancialYear> finYearList = ptCommonUtils
                    .getAllFinancialYearsBetweenDates(getFromDateforFinYearList(assessmentList), finYearStartDate);
            for (CFinancialYear cFinancialYear : finYearList) {
                demandRegisterInfoList.addAll(getDemandRegisterInfo(cFinancialYear.getFinYearRange(), wardId, mode));
            }
        }
        reportParams.put("demandRegisterInfoList", demandRegisterInfoList);
        final StringBuilder queryString = new StringBuilder();
        queryString.append("from City");
        final Query query = entityManager.createQuery(queryString.toString());
        final City city = (City) query.getSingleResult();
        reportParams.put("cityName", city.getName());
        reportParams.put("districtName", city.getDistrictName());
        reportParams.put("ward", boundaryRepository.getOne(wardId).getName());
        reportParams.put("propertyType", mode);
        reportInput = new ReportRequest(PropertyTaxConstants.REPORT_ARREAR_DEMAND_REGISTER, reportParams, reportParams);
        return reportInput;
    }

    public Date getMinTransactionDate(final List<String> assessmentList) {
        return transactionsRepository.getMinTransactionDateForAssessmentList(assessmentList);

    }

    public Date getFromDateforFinYearList(final List<String> assessmentList) {
        Date mintransactionDate = getMinTransactionDate(assessmentList);
        Date leastInstallmentDate = DateUtils.getDate("1960-04-01", "yyyy-MM-dd");
        if (mintransactionDate.before(leastInstallmentDate))
            mintransactionDate = leastInstallmentDate;
        return installmentDao.getInsatllmentByModuleForGivenDate(
                moduleService.getModuleByName(PropertyTaxConstants.PTMODULENAME), mintransactionDate).getFromDate();
    }

    @SuppressWarnings("unchecked")
    public List<DemandRegisterInfo> getDemandRegisterInfo(final String finYear, final Long ward, final String mode) {
        String propertyType = getPropertyType(mode);
        StringBuilder query = new StringBuilder(
                "select bp.propertyid \"assessmentNo\", at.ownersname \"ownerName\", at.doorno \"houseNo\", instm.financial_year \"financialYear\", cast(idi.demand as numeric) \"demand\", coalesce(cast(ici.collectiondate as character varying), '-') \"collectionDate\","
                        + "cast(coalesce(ici.amount, 0) as numeric) \"collectedAmount\", ici.collectionmode \"collectionMode\", cast(idi.totalcollection as numeric) \"totalCollection\", cast(idi.writeoff as numeric) \"writeOff\", cast(idi.advance as numeric) \"advanceAmount\" "
                        + " from egpt_assessment_transactions at, egpt_basic_property bp, egpt_installment_demand_info idi left join egpt_installment_collection_info ici on idi.id=ici.installment_demand_info, eg_installment_master instm, egpt_property_type_master ptm where "
                        + "idi.assessment_transactions=at.id and at.basicproperty=bp.id and idi.installment=instm.id and instm.financial_year=:finYear and at.ward =:ward and at.propertytype=ptm.id and ptm.code =:propertyType order by bp.propertyid, at.transaction_date");
        final SQLQuery sqlQuery = ptCommonUtils.getSession().createSQLQuery(query.toString());
        sqlQuery.setParameter("finYear", finYear);
        sqlQuery.setParameter("ward", ward);
        sqlQuery.setParameter("propertyType", propertyType);
        sqlQuery.setResultTransformer(Transformers.aliasToBean(DemandRegisterInfo.class));
        return filterDemandRegisterInfo(sqlQuery.list());
    }

    public List<DemandRegisterInfo> filterDemandRegisterInfo(final List<DemandRegisterInfo> demandRegisterInfoList) {
        List<DemandRegisterInfo> list = new ArrayList<>();
        DemandRegisterInfo demandRegisterInfo = null;
        for (DemandRegisterInfo registerInfo : demandRegisterInfoList) {
            if (demandRegisterInfo != null
                    && !demandRegisterInfo.getAssessmentNo().equals(registerInfo.getAssessmentNo()))
                list.add(demandRegisterInfo);
            demandRegisterInfo = registerInfo;

        }
        if (demandRegisterInfo != null)
            list.add(demandRegisterInfo);
        return list;
    }

    public String getPropertyType(String mode) {
        return mode.equals(PropertyTaxConstants.CATEGORY_TYPE_VACANTLAND_TAX)
                ? PropertyTaxConstants.OWNERSHIP_TYPE_VAC_LAND : PropertyTaxConstants.OWNERSHIP_TYPE_PRIVATE;
    }

}