package org.egov.edcr.service;

import org.apache.log4j.Logger;
import org.egov.edcr.autonumber.DcrApplicationNumberGenerator;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.repository.EdcrApplicationRepository;
import org.egov.edcr.utility.PortalInetgrationService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.egov.edcr.utility.DcrConstants.FILESTORE_MODULECODE;

@Service
@Transactional(readOnly = true)
public class EdcrApplicationService {
    private static Logger LOG = Logger.getLogger(DcrService.class);

    @Autowired
    protected SecurityUtils securityUtils;

    @Autowired
    private EdcrApplicationRepository edcrApplicationRepository;

    @Autowired
    private DcrService dcrService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private PortalInetgrationService portalInetgrationService;

    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    private DcrApplicationNumberGenerator dcrApplicationNumberGenerator;

    @Transactional
    public EdcrApplication create(final EdcrApplication edcrApplication) {

        edcrApplication.setApplicationDate(new Date());
        edcrApplication.setApplicationNumber(applicationNumberGenerator.generate());
        edcrApplication.setSavedDxfFile(saveDXF(edcrApplication));
        edcrApplication.setPlanInformation(edcrApplication.getPlanInformation());
        edcrApplicationRepository.save(edcrApplication);
        PlanDetail planDetail = callDcrProcess(edcrApplication);
        if (planDetail.getEdcrPassed()){
            String dcrApplicationNumber = dcrApplicationNumberGenerator.generateEDcrApplicationNumber(edcrApplication);
            edcrApplication.setDcrNumber(dcrApplicationNumber);
        }
        portalInetgrationService.createPortalUserinbox(edcrApplication, Arrays.asList(securityUtils.getCurrentUser()));
        return edcrApplication;
    }


    @Transactional
    public EdcrApplication update(final EdcrApplication edcrApplication) {
        edcrApplication.setSavedDxfFile(saveDXF(edcrApplication));
        EdcrApplication applicationRes = edcrApplicationRepository.save(edcrApplication);
        PlanDetail planDetail = callDcrProcess(edcrApplication);
        if (planDetail.getEdcrPassed()){
            String dcrApplicationNumber = dcrApplicationNumberGenerator.generateEDcrApplicationNumber(edcrApplication);
            edcrApplication.setDcrNumber(dcrApplicationNumber);
        }
        portalInetgrationService.updatePortalUserinbox(applicationRes, securityUtils.getCurrentUser());
        return applicationRes;
    }

    private PlanDetail callDcrProcess(EdcrApplication edcrApplication) {
        PlanDetail planDetail = new PlanDetail();
        try {
            planDetail = dcrService.process(edcrApplication.getSavedDxfFile(), edcrApplication);
        } catch (Exception e) {
            LOG.error("Error in edcr Processing", e);
            // e.printStackTrace();
        }
        return planDetail;
    }

    /*
     * public void saveDcrApplication(EdcrApplication edcrApplication) { saveDXF(edcrApplication);
     * dcrService.process(edcrApplication.getSavedDxfFile(), edcrApplication); }
     */
    private File saveDXF(EdcrApplication edcrApplication) {
        FileStoreMapper fileStoreMapper = addToFileStore(edcrApplication.getDxfFile());
        File dxfFile = fileStoreService.fetch(fileStoreMapper.getFileStoreId(), FILESTORE_MODULECODE);
        dcrService.buildDocuments(edcrApplication, fileStoreMapper, null,null);
        edcrApplication.setDcrDocuments(edcrApplication.getDcrDocuments());
        return dxfFile;

    }

    private FileStoreMapper addToFileStore(final MultipartFile file) {
        FileStoreMapper fileStoreMapper;
        try {
            fileStoreMapper = fileStoreService.store(file.getInputStream(), file.getOriginalFilename(),
                    file.getContentType(), FILESTORE_MODULECODE);
        } catch (final IOException e) {
            throw new ApplicationRuntimeException("Error occurred while getting inputstream", e);
        }
        return fileStoreMapper;
    }


    public List<EdcrApplication> findAll() {
        return edcrApplicationRepository.findAll(new Sort(Sort.Direction.ASC, "name"));
    }

    public EdcrApplication findOne(Long id) {
        return edcrApplicationRepository.findOne(id);
    }

    public EdcrApplication findByApplicationNo(String appNo) {
        return edcrApplicationRepository.findByApplicationNumber(appNo);
    }

    public List<EdcrApplication> search(EdcrApplication edcrApplication) {
        return edcrApplicationRepository.findAll();
    }
}