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
package org.egov.bpa.transaction.service;

import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.dto.SearchBpaApplicationForm;
import org.egov.bpa.transaction.service.collection.BpaDemandService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchBpaApplicationService {

	@Autowired
	private BpaThirdPartyService bpaThirdPartyService;
	@Autowired
	private BpaDemandService bpaDemandService;

	@PersistenceContext
	private EntityManager entityManager;

	public Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public Date resetFromDateTimeStamp(final Date date) {
		final Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		return cal1.getTime();
	}

	public Date resetToDateTimeStamp(final Date date) {
		final Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		cal1.set(Calendar.HOUR_OF_DAY, 23);
		cal1.set(Calendar.MINUTE, 59);
		cal1.set(Calendar.SECOND, 59);
		cal1.set(Calendar.MILLISECOND, 999);
		return cal1.getTime();
	}

	public List<SearchBpaApplicationForm> search(final SearchBpaApplicationForm bpaApplicationForm) {
		final Criteria criteria = buildSearchCriteria(bpaApplicationForm);
		return buildApplicationDetailsResponse(criteria);
	}

	private List<SearchBpaApplicationForm> buildApplicationDetailsResponse(final Criteria criteria) {
		List<SearchBpaApplicationForm> searchBpaApplicationFormList = new ArrayList<>();
		for (BpaApplication bpaApplication : (List<BpaApplication>) criteria.list()) {
			SearchBpaApplicationForm searchBpaApplicationForm = new SearchBpaApplicationForm();
			searchBpaApplicationForm.setId(bpaApplication.getId());
			searchBpaApplicationForm.setApplicationNumber(bpaApplication.getApplicationNumber());
			searchBpaApplicationForm.setBuildingplanapprovalnumber(bpaApplication.getBuildingplanapprovalnumber());
			searchBpaApplicationForm.setApplicationDate(bpaApplication.getApplicationDate());
			searchBpaApplicationForm.setServiceTypeId(bpaApplication.getServiceType().getId());
			searchBpaApplicationForm.setStatusId(bpaApplication.getStatus().getId());
			searchBpaApplicationForm.setServiceCode(
					bpaApplication.getServiceType() != null ? bpaApplication.getServiceType().getCode() : "");
			searchBpaApplicationForm.setServiceType(
					bpaApplication.getServiceType() != null ? bpaApplication.getServiceType().getDescription() : "");
			searchBpaApplicationForm.setOccupancy(
					bpaApplication.getOccupancy() != null ? bpaApplication.getOccupancy().getDescription() : "");
			searchBpaApplicationForm.setStatus(bpaApplication.getStatus().getDescription());
			searchBpaApplicationForm.setPlanPermissionNumber(bpaApplication.getPlanPermissionNumber());
			searchBpaApplicationForm.setApplicantName(
					bpaApplication.getOwner() != null ? bpaApplication.getOwner().getUser().getName() : "");
			searchBpaApplicationForm.setStakeHolderName(!bpaApplication.getStakeHolder().isEmpty()
					&& bpaApplication.getStakeHolder().get(0).getStakeHolder() != null
							? bpaApplication.getStakeHolder().get(0).getStakeHolder().getName() : "");
			if (bpaApplication.getState() != null && bpaApplication.getState().getOwnerPosition() != null) {
				searchBpaApplicationForm.setCurrentOwner(bpaThirdPartyService
						.getUserPositionByPassingPosition(bpaApplication.getState().getOwnerPosition().getId())
						.getName());
				searchBpaApplicationForm.setPendingAction(bpaApplication.getState().getNextAction());
			}
			if (!bpaApplication.getSiteDetail().isEmpty() && bpaApplication.getSiteDetail().get(0) != null) {
				searchBpaApplicationForm
						.setElectionWard(bpaApplication.getSiteDetail().get(0).getElectionBoundary() != null
								? bpaApplication.getSiteDetail().get(0).getElectionBoundary().getName() : "");
				searchBpaApplicationForm.setWard(bpaApplication.getSiteDetail().get(0).getAdminBoundary() != null
						? bpaApplication.getSiteDetail().get(0).getAdminBoundary().getName() : "");
				searchBpaApplicationForm
						.setZone(bpaApplication.getSiteDetail().get(0).getAdminBoundary().getParent() != null
								? bpaApplication.getSiteDetail().get(0).getAdminBoundary().getParent().getName() : "");
				searchBpaApplicationForm.setLocality(bpaApplication.getSiteDetail().get(0).getLocationBoundary() != null
						? bpaApplication.getSiteDetail().get(0).getLocationBoundary().getName() : "");
				searchBpaApplicationForm.setReSurveyNumber(bpaApplication.getSiteDetail().get(0).getReSurveyNumber());
			}
			searchBpaApplicationForm.setFeeCollected(bpaDemandService.checkAnyTaxIsPendingToCollect(bpaApplication));
			searchBpaApplicationForm.setAddress(
					bpaApplication.getOwner() != null && !bpaApplication.getOwner().getUser().getAddress().isEmpty()
							? bpaApplication.getOwner().getUser().getAddress().get(0).getStreetRoadLine() : "");

			searchBpaApplicationFormList.add(searchBpaApplicationForm);
		}
		return searchBpaApplicationFormList;
	}

	public List<SearchBpaApplicationForm> searchForCollectionPending(
			final SearchBpaApplicationForm bpaApplicationForm) {
		final Criteria criteria = buildSearchCriteria(bpaApplicationForm);
		criteria.createAlias("bpaApplication.status", "status")
				.add(Restrictions.in("status.code", "Approved", "Registered", "Scheduled For Document Scrutiny",
						"Rescheduled For Document Scrutiny", "Pending For Rescheduling For Document Scrutiny"));
		return buildApplicationDetailsResponse(criteria);
	}

	public Criteria buildSearchCriteria(final SearchBpaApplicationForm searchBpaApplicationForm) {
		final Criteria criteria = getCurrentSession().createCriteria(BpaApplication.class, "bpaApplication");

		if (searchBpaApplicationForm.getApplicantName() != null) {
			criteria.createAlias("bpaApplication.owner", "owner");
			criteria.createAlias("owner.user", "user").add(
					Restrictions.ilike("user.name", searchBpaApplicationForm.getApplicantName(), MatchMode.ANYWHERE));
		}
		if (searchBpaApplicationForm.getApplicationNumber() != null) {
			criteria.add(Restrictions.eq("bpaApplication.applicationNumber",
					searchBpaApplicationForm.getApplicationNumber()));
		}
		if (searchBpaApplicationForm.getServiceTypeId() != null) {
			criteria.add(Restrictions.eq("bpaApplication.serviceType.id", searchBpaApplicationForm.getServiceTypeId()));
		}
		if (searchBpaApplicationForm.getServiceType() != null) {
			criteria.createAlias("bpaApplication.serviceType", "serviceType")
					.add(Restrictions.eq("serviceType.description", searchBpaApplicationForm.getServiceType()));
		}
		if (searchBpaApplicationForm.getStatusId() != null) {
			criteria.add(Restrictions.eq("bpaApplication.status.id", searchBpaApplicationForm.getStatusId()));
		}
		if (searchBpaApplicationForm.getStatus() != null) {
			criteria.createAlias("bpaApplication.status", "status")
					.add(Restrictions.eq("status.code", searchBpaApplicationForm.getStatus()));
		}
		if (searchBpaApplicationForm.getFromDate() != null)
			criteria.add(Restrictions.ge("bpaApplication.applicationDate",
					resetFromDateTimeStamp(searchBpaApplicationForm.getFromDate())));
		if (searchBpaApplicationForm.getToDate() != null)
			criteria.add(Restrictions.le("bpaApplication.applicationDate",
					resetToDateTimeStamp(searchBpaApplicationForm.getToDate())));
		if (searchBpaApplicationForm.getElectionWardId() != null || searchBpaApplicationForm.getWardId() != null
				|| searchBpaApplicationForm.getZoneId() != null || searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("bpaApplication.siteDetail", "siteDetail");
		}
		if (searchBpaApplicationForm.getWardId() != null || searchBpaApplicationForm.getZoneId() != null
				|| searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("siteDetail.adminBoundary", "adminBoundary");
		}
		if (searchBpaApplicationForm.getElectionWardId() != null) {
			criteria.createAlias("siteDetail.electionBoundary", "electionBoundary")
					.add(Restrictions.eq("electionBoundary.id", searchBpaApplicationForm.getElectionWardId()));
		}
		if (searchBpaApplicationForm.getWardId() != null) {
			criteria.add(Restrictions.eq("adminBoundary.id", searchBpaApplicationForm.getWardId()));
		}
		if (searchBpaApplicationForm.getZoneId() != null) {
			criteria.add(Restrictions.eq("adminBoundary.parent.id", searchBpaApplicationForm.getZoneId()));
		}

		if (searchBpaApplicationForm.getZoneId() == null && searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("adminBoundary.parent", "parent")
					.add(Restrictions.eq("parent.name", searchBpaApplicationForm.getZone()));
		}
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		return criteria;
	}

	public List<SearchBpaApplicationForm> searchForDocumentScrutinyPending(
			final SearchBpaApplicationForm bpaApplicationForm) {
		final Criteria criteria = buildDocumentScrutinySearchCriteria(bpaApplicationForm);
		criteria.createAlias("slotApplication.application.status", "status").add(
				Restrictions.in("status.code", "Scheduled For Document Scrutiny", "Rescheduled For Document Scrutiny"));
		return buildDocumentScrutinyApplicationDetailsResponse(criteria);
	}

	public Criteria buildDocumentScrutinySearchCriteria(final SearchBpaApplicationForm searchBpaApplicationForm) {
		final Criteria criteria = getCurrentSession().createCriteria(SlotApplication.class, "slotApplication");
		criteria.createAlias("slotApplication.application", "bpaApplication");
		criteria.createAlias("slotApplication.slotDetail", "slotDetail");
		criteria.createAlias("slotDetail.slot", "slot");
		
		if (searchBpaApplicationForm.getServiceTypeEnum() != null
				&& !searchBpaApplicationForm.getServiceTypeEnum().isEmpty()) {
			if (searchBpaApplicationForm.getServiceTypeEnum()
					.equalsIgnoreCase(ApplicationType.ALL_OTHER_SERVICES.toString())) {
				criteria.add(Restrictions.eq("bpaApplication.isOneDayPermitApplication", false));
				searchBpaApplicationForm.setToDate(new Date());
			} else if (searchBpaApplicationForm.getServiceTypeEnum()
					.equalsIgnoreCase(ApplicationType.ONE_DAY_PERMIT.toString()))
				criteria.add(Restrictions.eq("bpaApplication.isOneDayPermitApplication", true)); 
		}
		if (searchBpaApplicationForm.getToDate() != null)
			criteria.add(Restrictions.eq("slot.appointmentDate",
					resetToDateTimeStamp(searchBpaApplicationForm.getToDate())));
		if (searchBpaApplicationForm.getElectionWardId() != null || searchBpaApplicationForm.getWardId() != null
				|| searchBpaApplicationForm.getZoneId() != null || searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("bpaApplication.siteDetail", "siteDetail");
		}
		if (searchBpaApplicationForm.getWardId() != null || searchBpaApplicationForm.getZoneId() != null
				|| searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("siteDetail.adminBoundary", "adminBoundary");
		}
		if (searchBpaApplicationForm.getElectionWardId() != null) {
			criteria.createAlias("siteDetail.electionBoundary", "electionBoundary")
					.add(Restrictions.eq("electionBoundary.id", searchBpaApplicationForm.getElectionWardId()));
		}
		if (searchBpaApplicationForm.getWardId() != null) {
			criteria.add(Restrictions.eq("adminBoundary.id", searchBpaApplicationForm.getWardId()));
		}
		if (searchBpaApplicationForm.getZoneId() != null) {
			criteria.add(Restrictions.eq("adminBoundary.parent.id", searchBpaApplicationForm.getZoneId()));
		}

		if (searchBpaApplicationForm.getZoneId() == null && searchBpaApplicationForm.getZone() != null) {
			criteria.createAlias("adminBoundary.parent", "parent")
					.add(Restrictions.eq("parent.name", searchBpaApplicationForm.getZone()));
		}
		criteria.add(Restrictions.eq("slotApplication.active", true));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		criteria.setProjection(Projections.distinct(Projections.projectionList()
			    .add(Projections.property("application"), "application") )).setResultTransformer(Transformers.aliasToBean(SlotApplication.class)); 
		return criteria;
	}

	private List<SearchBpaApplicationForm> buildDocumentScrutinyApplicationDetailsResponse(final Criteria criteria) {
		List<SearchBpaApplicationForm> searchBpaApplicationFormList = new ArrayList<>();
		for (SlotApplication slotApplication : (List<SlotApplication>) criteria.list()) {
			SearchBpaApplicationForm searchBpaApplicationForm = new SearchBpaApplicationForm();
			searchBpaApplicationForm.setId(slotApplication.getApplication().getId());
			searchBpaApplicationForm.setApplicationNumber(slotApplication.getApplication().getApplicationNumber());
			searchBpaApplicationForm
					.setBuildingplanapprovalnumber(slotApplication.getApplication().getBuildingplanapprovalnumber());
			searchBpaApplicationForm.setApplicationDate(slotApplication.getApplication().getApplicationDate());
			searchBpaApplicationForm.setServiceTypeId(slotApplication.getApplication().getServiceType().getId());
			searchBpaApplicationForm.setStatusId(slotApplication.getApplication().getStatus().getId());
			searchBpaApplicationForm.setServiceCode(slotApplication.getApplication().getServiceType() != null
					? slotApplication.getApplication().getServiceType().getCode() : "");
			searchBpaApplicationForm.setServiceType(slotApplication.getApplication().getServiceType() != null
					? slotApplication.getApplication().getServiceType().getDescription() : "");
			searchBpaApplicationForm.setOccupancy(slotApplication.getApplication().getOccupancy() != null
					? slotApplication.getApplication().getOccupancy().getDescription() : "");
			searchBpaApplicationForm.setStatus(slotApplication.getApplication().getStatus().getDescription());
			searchBpaApplicationForm
					.setPlanPermissionNumber(slotApplication.getApplication().getPlanPermissionNumber());
			searchBpaApplicationForm.setApplicantName(slotApplication.getApplication().getOwner() != null
					? slotApplication.getApplication().getOwner().getUser().getName() : "");
			searchBpaApplicationForm.setStakeHolderName(!slotApplication.getApplication().getStakeHolder().isEmpty()
					&& slotApplication.getApplication().getStakeHolder().get(0).getStakeHolder() != null
							? slotApplication.getApplication().getStakeHolder().get(0).getStakeHolder().getName() : "");
			if (slotApplication.getApplication().getState() != null
					&& slotApplication.getApplication().getState().getOwnerPosition() != null) {
				searchBpaApplicationForm
						.setCurrentOwner(bpaThirdPartyService
								.getUserPositionByPassingPosition(
										slotApplication.getApplication().getState().getOwnerPosition().getId())
								.getName());
				searchBpaApplicationForm.setPendingAction(slotApplication.getApplication().getState().getNextAction());
			}
			if (!slotApplication.getApplication().getSiteDetail().isEmpty()
					&& slotApplication.getApplication().getSiteDetail().get(0) != null) {
				searchBpaApplicationForm.setElectionWard(
						slotApplication.getApplication().getSiteDetail().get(0).getElectionBoundary() != null
								? slotApplication.getApplication().getSiteDetail().get(0).getElectionBoundary()
										.getName()
								: "");
				searchBpaApplicationForm
						.setWard(slotApplication.getApplication().getSiteDetail().get(0).getAdminBoundary() != null
								? slotApplication.getApplication().getSiteDetail().get(0).getAdminBoundary().getName()
								: "");
				searchBpaApplicationForm.setZone(
						slotApplication.getApplication().getSiteDetail().get(0).getAdminBoundary().getParent() != null
								? slotApplication.getApplication().getSiteDetail().get(0).getAdminBoundary().getParent()
										.getName()
								: "");
				searchBpaApplicationForm.setLocality(
						slotApplication.getApplication().getSiteDetail().get(0).getLocationBoundary() != null
								? slotApplication.getApplication().getSiteDetail().get(0).getLocationBoundary()
										.getName()
								: "");
				searchBpaApplicationForm
						.setReSurveyNumber(slotApplication.getApplication().getSiteDetail().get(0).getReSurveyNumber());
			}
			searchBpaApplicationForm
					.setFeeCollected(bpaDemandService.checkAnyTaxIsPendingToCollect(slotApplication.getApplication()));
			searchBpaApplicationForm.setAddress(slotApplication.getApplication().getOwner() != null
					&& !slotApplication.getApplication().getOwner().getUser().getAddress().isEmpty()
							? slotApplication.getApplication().getOwner().getUser().getAddress().get(0)
									.getStreetRoadLine()
							: "");

			searchBpaApplicationFormList.add(searchBpaApplicationForm);
		}
		return searchBpaApplicationFormList;
	}

}
