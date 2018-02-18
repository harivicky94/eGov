package org.egov.edcr.web.controller;

import java.util.List;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.web.adaptor.EdcrApplicationJsonAdaptor;
import org.egov.edcr.service.DcrService;
import org.egov.edcr.service.EdcrApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping("/edcrapplication")
public class EdcrApplicationController {
    private final static String EDCRAPPLICATION_NEW = "edcrapplication-new";
    private final static String EDCRAPPLICATION_RESULT = "edcrapplication-result";
    private final static String EDCRAPPLICATION_EDIT = "edcrapplication-edit";
    private final static String EDCRAPPLICATION_VIEW = "edcrapplication-view";
    private final static String EDCRAPPLICATION_SEARCH = "edcrapplication-search";
    @Autowired
    private EdcrApplicationService edcrApplicationService;
   
    @Autowired
    private MessageSource messageSource;
  /*  @Autowired
  //  private PlanInformationService planInformationService;
*/
    private void prepareNewForm(Model model) {
       // model.addAttribute("planInformations", planInformationService.findAll());
    }

    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String newForm(final Model model) {
        prepareNewForm(model);
        model.addAttribute("edcrApplication", new EdcrApplication());
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

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") final Long id, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findOne(id);
        prepareNewForm(model);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_EDIT;
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

    @RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
    public String view(@PathVariable("id") final Long id, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findOne(id);
        prepareNewForm(model);
        model.addAttribute("edcrApplication", edcrApplication);
        return EDCRAPPLICATION_VIEW;
    }

    @RequestMapping(value = "/result/{id}", method = RequestMethod.GET)
    public String result(@PathVariable("id") final Long id, Model model) {
        EdcrApplication edcrApplication = edcrApplicationService.findOne(id);
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

    @RequestMapping(value = "/ajaxsearch/{mode}", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String ajaxsearch(@PathVariable("mode") final String mode, Model model,
            @ModelAttribute final EdcrApplication edcrApplication) {
        List<EdcrApplication> searchResultList = edcrApplicationService.search(edcrApplication);
        String result = new StringBuilder("{ \"data\":").append(toSearchResultJson(searchResultList)).append("}").toString();
        return result;
    }

    public Object toSearchResultJson(final Object object) {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.registerTypeAdapter(EdcrApplication.class, new EdcrApplicationJsonAdaptor()).create();
        final String json = gson.toJson(object);
        return json;
    }
}