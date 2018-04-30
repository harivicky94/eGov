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

package org.egov.bpa.web.controller.master;

import org.egov.bpa.master.entity.StakeHolder;
import org.egov.bpa.master.service.StakeHolderService;
import org.egov.bpa.transaction.entity.StakeHolderDocument;
import org.egov.bpa.transaction.entity.dto.SearchStakeHolderForm;
import org.egov.bpa.transaction.entity.enums.StakeHolderType;
import org.egov.bpa.transaction.service.BPADocumentService;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.web.controller.adaptor.SearchStakeHolderJsonAdaptor;
import org.egov.bpa.web.controller.adaptor.StakeHolderJsonAdaptor;
import org.egov.commons.entity.Source;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.persistence.entity.Address;
import org.egov.infra.persistence.entity.CorrespondenceAddress;
import org.egov.infra.persistence.entity.PermanentAddress;
import org.egov.infra.persistence.entity.enums.AddressType;
import org.egov.infra.persistence.entity.enums.Gender;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.portal.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.egov.infra.utils.JsonUtils.toJSON;

@Controller
@RequestMapping(value = "/stakeholder")
public class StakeHolderController {
	private static final String RDRCT_STKHLDR_RSLT = "redirect:/stakeholder/result/";
	private static final String MESSAGE = "message";
	private static final String STAKEHOLDER_RESULT = "stakeholder-result";
	private static final String SEARCH_STAKEHOLDER_EDIT = "search-stakeholder-edit";
	private static final String STAKE_HOLDER = "stakeHolder";
	private static final String STAKEHOLDER_UPDATE = "stakeholder-update";
	private static final String STAKEHOLDER_VIEW = "stakeholder-view";
	private static final String SEARCH_STAKEHOLDER_VIEW = "search-stakeholder-view";
	private static final String DATA = "{ \"data\":";
	private static final String STAKEHOLDER_NEW = "stakeholder-new";
	private static final String STAKEHOLDER_NEW_BY_CITIZEN = "stakeholder-new-bycitizen";
	private static final String SRCH_STKHLDR_FR_APPRVL = "search-stakeholder-forapproval";
	private static final String STAKE_HOLDER_FORM = "searchStakeHolderForm";
	private static final String STKHLDR_APP_REJ_CMNT = "stakeholder-remarks";
	public static final String STAKE_HOLDER_TYPES = "stakeHolderTypes";
	@Autowired
	private StakeHolderService stakeHolderService;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private BPADocumentService bpaDocumentService;
	@Autowired
	private CitizenService citizenService;
	@Autowired
	private BPASmsAndEmailService bpaSmsAndEmailService;
	@Autowired
	private UserService userService;
	@Autowired
	private SecurityUtils securityUtils;

	@ModelAttribute("stakeHolderDocumentList")
	public List<StakeHolderDocument> getStakeHolderDocuments() {
		return bpaDocumentService.getStakeHolderDocuments();
	}

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String showStakeHolder(final Model model) {
		StakeHolder stakeHolder = new StakeHolder();
		stakeHolder.setIsActive(true);
		prepareModel(model, stakeHolder);
		return STAKEHOLDER_NEW;
	}

	private void prepareModel(final Model model, final StakeHolder stakeHolder) {
		model.addAttribute(STAKE_HOLDER, stakeHolder);
		model.addAttribute("isEmployee", securityUtils.getCurrentUser().getType().equals(UserType.EMPLOYEE) || "egovernments".equals(securityUtils.getCurrentUser().getUsername()) ? Boolean.TRUE : Boolean.FALSE);
		model.addAttribute("isBusinessUser", securityUtils.getCurrentUser().getType().equals(UserType.SYSTEM) ? Boolean.TRUE : Boolean.FALSE);
		model.addAttribute("genderList", Arrays.asList(Gender.values()));
		model.addAttribute(STAKE_HOLDER_TYPES, Arrays.asList(StakeHolderType.values()));
		model.addAttribute("isOnbehalfOfOrganization", false);
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String createStakeholder(@ModelAttribute(STAKE_HOLDER) final StakeHolder stakeHolder,
									final Model model,
									final HttpServletRequest request,
									final BindingResult errors, final RedirectAttributes redirectAttributes) {
		validateStakeholder(stakeHolder, errors);
		if (errors.hasErrors() || checkIsUserExists(stakeHolder, model)) {
			prepareModel(model, stakeHolder);
			return STAKEHOLDER_NEW;
		}
		StakeHolder stakeHolderRes = stakeHolderService.save(stakeHolder);
		bpaSmsAndEmailService.sendSMSForStakeHolder(stakeHolderRes, false);
		bpaSmsAndEmailService.sendEmailForStakeHolder(stakeHolderRes, false);
		redirectAttributes.addFlashAttribute(MESSAGE, messageSource.getMessage("msg.approve.stakeholder.success", new String[]{stakeHolder.getStakeHolderType().getStakeHolderTypeVal()}, null));
		return RDRCT_STKHLDR_RSLT + stakeHolderRes.getId();
	}

	@RequestMapping(value = "/createbycitizen", method = RequestMethod.GET)
	public String showOnlineStakeHolder(final Model model) {
		StakeHolder stakeHolder = new StakeHolder();
		stakeHolder.setSource(Source.ONLINE);
		stakeHolder.setIsActive(false);
		prepareModel(model, stakeHolder);
		return STAKEHOLDER_NEW_BY_CITIZEN;
	}

	@RequestMapping(value = "/createbycitizen", method = RequestMethod.POST)
	public String createOnlineStakeholder(@ModelAttribute(STAKE_HOLDER) final StakeHolder stakeHolder,
										  final Model model,
										  final BindingResult errors, final RedirectAttributes redirectAttributes) {
		validateStakeholder(stakeHolder, errors);
		if (securityUtils.getCurrentUser().getType().equals(UserType.SYSTEM) &&
			!citizenService.isValidOTP(stakeHolder.getActivationCode(), stakeHolder.getMobileNumber()))
			errors.rejectValue("activationCode", "error.otp.verification.failed");

		if (errors.hasErrors() || checkIsUserExists(stakeHolder, model)) {
			prepareModel(model, stakeHolder);
			return STAKEHOLDER_NEW_BY_CITIZEN;
		}
		StakeHolder stakeHolderRes = stakeHolderService.save(stakeHolder);
		bpaSmsAndEmailService.sendSMSForStakeHolder(stakeHolderRes, true);
		bpaSmsAndEmailService.sendEmailForStakeHolder(stakeHolderRes, true);
		redirectAttributes.addFlashAttribute(MESSAGE, messageSource.getMessage("msg.create.stakeholder.ctizen.request", new String[]{stakeHolderRes.getStakeHolderType().getStakeHolderTypeVal(), stakeHolderRes.getCode()}, null));
		return RDRCT_STKHLDR_RSLT + stakeHolderRes.getId();
	}

	private boolean checkIsUserExists(@ModelAttribute(STAKE_HOLDER) StakeHolder stakeHolder, Model model) {
		List<User> users = userService.getUserByMobileNumberAndType(stakeHolder.getMobileNumber(), UserType.BUSINESS);
		if (!users.isEmpty()) {
			String message = messageSource.getMessage("msg.name.mobile.exists",
					new String[]{users.get(0).getName(), users.get(0).getMobileNumber(), users.get(0).getGender().name()},
					LocaleContextHolder.getLocale());
			model.addAttribute("invalidBuildingLicensee", message);
			prepareModel(model, stakeHolder);
			return true;
		}
		return false;
	}


	private void validateStakeholder(final StakeHolder stakeHolder, final BindingResult errors) {
		if (stakeHolderService.checkIsStakeholderCodeAlreadyExists(stakeHolder)) {
			errors.rejectValue("code", "msg.code.exists");
		}
		if (stakeHolderService.checkIsEmailAlreadyExists(stakeHolder)) {
			errors.rejectValue("emailId", "msg.email.exists");
		}
	}

	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String editStakeholder(@PathVariable final Long id, final Model model) {
		final StakeHolder stakeHolder = stakeHolderService.findById(id);
		preapreUpdateModel(stakeHolder, model);
		model.addAttribute("stakeHolderDocumentList", stakeHolder.getStakeHolderDocument());
		return STAKEHOLDER_UPDATE;
	}

	private void preapreUpdateModel(final StakeHolder stakeHolder, final Model model) {
		for (final Address address : stakeHolder.getAddress())
			if (AddressType.CORRESPONDENCE.equals(address.getType()))
				stakeHolder.setCorrespondenceAddress((CorrespondenceAddress) address);
			else
				stakeHolder.setPermanentAddress((PermanentAddress) address);
		model.addAttribute("isEmployee", securityUtils.getCurrentUser().getType().equals(UserType.EMPLOYEE) ? Boolean.TRUE : Boolean.FALSE);
		model.addAttribute(STAKE_HOLDER, stakeHolder);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String updateStakeholder(@ModelAttribute(STAKE_HOLDER) final StakeHolder stakeHolder,
									final Model model,
									final HttpServletRequest request,
									final BindingResult errors, final RedirectAttributes redirectAttributes) {

		if (errors.hasErrors()) {
			preapreUpdateModel(stakeHolder, model);
			return STAKEHOLDER_UPDATE;
		}
		stakeHolderService.update(stakeHolder);
		redirectAttributes.addFlashAttribute(MESSAGE, messageSource.getMessage("msg.update.stakeholder.success", null, null));
		return RDRCT_STKHLDR_RSLT + stakeHolder.getId();
	}

	@RequestMapping(value = "/result/{id}", method = RequestMethod.GET)
	public String resultStakeHolder(@PathVariable final Long id, final Model model) {
		model.addAttribute(STAKE_HOLDER, stakeHolderService.findById(id));
		return STAKEHOLDER_RESULT;
	}

	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewStakeHolder(@PathVariable final Long id, final Model model) {
		model.addAttribute(STAKE_HOLDER, stakeHolderService.findById(id));
		return STAKEHOLDER_VIEW;
	}

	@RequestMapping(value = "/search/update", method = RequestMethod.GET)
	public String searchEditStakeHolder(final Model model) {
		model.addAttribute(STAKE_HOLDER, new StakeHolder());
		model.addAttribute(STAKE_HOLDER_TYPES, Arrays.asList(StakeHolderType.values()));
		return SEARCH_STAKEHOLDER_EDIT;
	}

	@RequestMapping(value = "/search/update", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getStakeHolderResultForEdit(@ModelAttribute final StakeHolder stakeHolder, final Model model) {
		final List<StakeHolder> searchResultList = stakeHolderService.search(stakeHolder);
		return new StringBuilder(DATA).append(toJSON(searchResultList, StakeHolder.class, StakeHolderJsonAdaptor.class))
									  .append("}")
									  .toString();
	}

	@RequestMapping(value = "/search/view", method = RequestMethod.GET)
	public String searchViewStakeHolder(final Model model) {
		model.addAttribute(STAKE_HOLDER, new StakeHolder());
		model.addAttribute(STAKE_HOLDER_TYPES, Arrays.asList(StakeHolderType.values()));
		return SEARCH_STAKEHOLDER_VIEW;
	}

	@RequestMapping(value = "/search/view", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String getStakeHolderForView(@ModelAttribute final StakeHolder stakeHolder, final Model model) {
		final List<StakeHolder> searchResultList = stakeHolderService.search(stakeHolder);
		return new StringBuilder(DATA).append(toJSON(searchResultList, StakeHolder.class, StakeHolderJsonAdaptor.class))
									  .append("}")
									  .toString();
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String searchStakeHolderForApprovalView(Model model) {
		model.addAttribute(STAKE_HOLDER_FORM, new SearchStakeHolderForm());
		model.addAttribute(STAKE_HOLDER_TYPES, Arrays.asList(StakeHolderType.values()));
		return SRCH_STKHLDR_FR_APPRVL;
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
	@ResponseBody
	public String searchStakeHolderForApproval(@ModelAttribute final SearchStakeHolderForm srchStkeHldrFrm, final Model model) {
		List<SearchStakeHolderForm> srchStkeHldrFrmLst = stakeHolderService.searchForApproval(srchStkeHldrFrm);
		return new StringBuilder(DATA)
				.append(toJSON(srchStkeHldrFrmLst, SearchStakeHolderForm.class, SearchStakeHolderJsonAdaptor.class))
				.append("}")
				.toString();
	}

	@RequestMapping(value = "/search/citizen/{id}", method = RequestMethod.GET)
	public String searchStakeHolderForApproval(@PathVariable final Long id, Model model) {
		StakeHolder stakeHolder = stakeHolderService.findById(id);
		model.addAttribute(STAKE_HOLDER, stakeHolder);
		preapreUpdateModel(stakeHolder, model);
		model.addAttribute("stakeHolderDocumentList", stakeHolder.getStakeHolderDocument());
		model.addAttribute("isApproval", true);
		return STKHLDR_APP_REJ_CMNT;
	}

	@RequestMapping(value = "/citizen/update", method = RequestMethod.POST)
	public String approveStakeHolder(@ModelAttribute final StakeHolder stakeHolder, final Model model,
									 final RedirectAttributes redirectAttributes, final HttpServletRequest request) {
		String stakeHolderStatus = request.getParameter("stakeHolderStatus");
		StakeHolder stakeHolderResponse = stakeHolderService.updateForApproval(stakeHolder, stakeHolderStatus);
		if (stakeHolderStatus.equals("Approve")) {
			bpaSmsAndEmailService.sendSMSForStakeHolder(stakeHolderResponse, false);
			bpaSmsAndEmailService.sendEmailForStakeHolder(stakeHolderResponse, false);
			redirectAttributes.addFlashAttribute(MESSAGE,
					messageSource.getMessage("msg.approve.stakeholder.success", new String[]{stakeHolder.getStakeHolderType().getStakeHolderTypeVal()}, null));
		} else if (stakeHolderStatus.equals("Reject")) {
			bpaSmsAndEmailService.sendSMSToStkHldrForRejection(stakeHolderResponse);
			bpaSmsAndEmailService.sendEmailToStkHldrForRejection(stakeHolderResponse);
			redirectAttributes.addFlashAttribute(MESSAGE, messageSource.getMessage("msg.reject.stakeholder", new String[]{stakeHolderResponse.getComments()}, null));
		}
		return RDRCT_STKHLDR_RSLT + stakeHolder.getId();
	}

}
