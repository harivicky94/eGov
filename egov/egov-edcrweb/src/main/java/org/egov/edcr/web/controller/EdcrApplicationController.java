package org.egov.edcr.web.controller;

import org.egov.bpa.master.entity.StakeHolder;
import org.egov.bpa.master.service.OccupancyService;
import org.egov.bpa.master.service.ServiceTypeService;
import org.egov.bpa.master.service.StakeHolderService;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.service.EdcrApplicationService;
import org.egov.edcr.web.adaptor.EdcrApplicationJsonAdaptor;
import org.egov.infra.persistence.entity.Address;
import org.egov.infra.persistence.entity.enums.AddressType;
import org.egov.infra.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.egov.infra.utils.JsonUtils.toJSON;

@Controller
@RequestMapping("/edcrapplication")
public class EdcrApplicationController {
    private final static String EDCRAPPLICATION_NEW = "edcrapplication-new";
    private final static String EDCRAPPLICATION_RESULT = "edcrapplication-result";
    private final static String EDCRAPPLICATION_EDIT = "edcrapplication-edit";
    private final static String EDCRAPPLICATION_VIEW = "edcrapplication-view";
    private final static String EDCRAPPLICATION_SEARCH = "edcrapplication-search";
    private final static String EDCRAPPLICATION_RE_UPLOAD = "edcr-reupload-form";
    @Autowired
    private EdcrApplicationService edcrApplicationService;
   
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private OccupancyService occupancyService;
    @Autowired
    private ServiceTypeService serviceTypeService;
    @Autowired
    private SecurityUtils securityUtils;
    @Autowired
    private StakeHolderService stakeHolderService;
  /*  @Autowired
  //  private PlanInformationService planInformationService;
*/
    private void prepareNewForm(Model model) {
       // model.addAttribute("planInformations", planInformationService.findAll());
        model.addAttribute("serviceTypeList", serviceTypeService.getAllActiveMainServiceTypes());
        model.addAttribute("amenityTypeList", serviceTypeService.getAllActiveAmenities());
        model.addAttribute("occupancyList", occupancyService.findAllOrderByOrderNumber());
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newForm(final Model model) {
        prepareNewForm(model);
        StakeHolder stakeHolder = stakeHolderService.findById(securityUtils.getCurrentUser().getId());
        Address permanentAddress = stakeHolder.getAddress().stream().filter(permtAddress -> permtAddress.getType().equals(AddressType.PERMANENT)).findAny().orElse(null);
        StringBuilder architectInfo = new StringBuilder(256).append(stakeHolder.getName())
                                                            .append(stakeHolder.getStakeHolderType().name()).
                                                                    append(stakeHolder.getMobileNumber()).
                                                                    append(permanentAddress.getStreetRoadLine()).append(".");
        EdcrApplication edcrApplication = new EdcrApplication();
        PlanInformation planInformation = new PlanInformation();
      //  planInformation.setArchitectInformation(architectInfo.toString());
        edcrApplication.setPlanInformation(planInformation);
        model.addAttribute("edcrApplication", edcrApplication);

        return EDCRAPPLICATION_NEW;
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute final EdcrApplication edcrApplication, final BindingResult errors,
            final Model model, final RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            prepareNewForm(model);
            return EDCRAPPLICATION_NEW;
        }
        edcrApplicationService.create(edcrApplication);
       
        redirectAttrs.addFlashAttribute("message", messageSource.getMessage("msg.edcrapplication.success", null, null));
        return "redirect:/edcrapplication/result/" + edcrApplication.getApplicationNumber();
    }

    @RequestMapping(value = "/edit/{applicationNumber}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") final String applicationNumber, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findByApplicationNo(applicationNumber);
        prepareNewForm(model);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_EDIT;
    }

    @RequestMapping(value = "/resubmit", method = RequestMethod.GET)
    public String uploadAgain(Model model) {
        prepareNewForm(model);
        model.addAttribute("edcrApplication", new EdcrApplication());
        return EDCRAPPLICATION_RE_UPLOAD;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public String update(@ModelAttribute final EdcrApplication edcrApplication, final BindingResult errors,
            final Model model, final RedirectAttributes redirectAttrs) {
        if (errors.hasErrors()) {
            prepareNewForm(model);
            return EDCRAPPLICATION_EDIT;
        }
        edcrApplicationService.update(edcrApplication);
        redirectAttrs.addFlashAttribute("message", messageSource.getMessage("msg.edcrapplication.success", null, null));
        return "redirect:/edcrapplication/result/" + edcrApplication.getApplicationNumber();
    }

    @RequestMapping(value = "/view/{applicationNumber}", method = RequestMethod.GET)
    public String view(@PathVariable final String applicationNumber, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findByApplicationNo(applicationNumber);
        prepareNewForm(model);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_VIEW;
    }

    @RequestMapping(value = "/result/{applicationNumber}", method = RequestMethod.GET)
    public String result(@PathVariable final String applicationNumber, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findByApplicationNo(applicationNumber);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_RESULT;
    }

    @RequestMapping(value = "/search/{mode}", method = RequestMethod.GET)
    public String search(@PathVariable("mode") final String mode, Model model) {
        EdcrApplication edcrApplication = new EdcrApplication();
        prepareNewForm(model);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_SEARCH;

    }

    @RequestMapping(value = "/get-information/{applicationNumber}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EdcrApplication getEdcrApplicationDetailsByApplnNumber(@PathVariable final String applicationNumber, Model model) {
        return edcrApplicationService.findByApplicationNo(applicationNumber);
    }

    @RequestMapping(value = "/ajaxsearch/{mode}", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String ajaxsearch(@PathVariable("mode") final String mode, Model model,
            @ModelAttribute final EdcrApplication edcrApplication) {
        List<EdcrApplication> searchResultList = edcrApplicationService.search(edcrApplication);
        return new StringBuilder("{ \"data\":")
                .append(toJSON(searchResultList, EdcrApplication.class, EdcrApplicationJsonAdaptor.class))
                .append("}")
                .toString();
    }

}