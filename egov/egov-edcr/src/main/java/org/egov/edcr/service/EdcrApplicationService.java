package org.egov.edcr.service;

 
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.repository.EdcrApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EdcrApplicationService {

    @Autowired
    private EdcrApplicationRepository edcrApplicationRepository;

    @Autowired
    private PlaninfoService planinfoService;


    @Autowired
    private DxfDocumentService dxfDocumentService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DcrService dcrService;


    @Transactional
 
    public EdcrApplication create(final EdcrApplication edcrApplication) {
     
 
        dcrService.process(edcrApplication.getDxfFile(), edcrApplication);

        PlanInformation savePlanIfo = planinfoService.save(edcrApplication.getPlanInformation());
        edcrApplication.setPlanInformation(savePlanIfo);


        edcrApplicationRepository.save(edcrApplication);

        dxfDocumentService.saveAll(edcrApplication.getDxfDocuments());

        return edcrApplication;
    }

    @Transactional
    public EdcrApplication update(final EdcrApplication edcrApplication) {
        return edcrApplicationRepository.save(edcrApplication);
    }

    public List<EdcrApplication> findAll() {
        return edcrApplicationRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
    }

    public EdcrApplication findOne(Long id) {
        return edcrApplicationRepository.findOne(id);
    }

    public List<EdcrApplication> search(EdcrApplication edcrApplication) {
        return edcrApplicationRepository.findAll();
    }
}