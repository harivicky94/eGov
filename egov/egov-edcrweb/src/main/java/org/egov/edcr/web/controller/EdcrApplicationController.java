package org.egov.edcr.web.controller;

import org.egov.bpa.master.entity.*;
import org.egov.bpa.master.service.*;
import org.egov.edcr.entity.*;
import org.egov.edcr.service.*;
import org.egov.edcr.web.adaptor.*;
import org.egov.infra.persistence.entity.*;
import org.egov.infra.persistence.entity.enums.*;
import org.egov.infra.security.utils.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;

import java.util.*;

import static org.egov.infra.utils.JsonUtils.*;

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
        StringBuilder architectInfo = new StringBuilder(256).append(stakeHolder.getName()).append(",")
                                                            .append(stakeHolder.getStakeHolderType().name()).append(",").
                                                                    append(stakeHolder.getMobileNumber()).append(",").
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