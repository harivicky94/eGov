package org.egov.edcr.service;

import org.apache.log4j.Logger;
import org.egov.edcr.autonumber.DcrApplicationNumberGenerator;
import org.egov.edcr.entity.DcrDocument;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.repository.EdcrApplicationRepository;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.PortalInetgrationService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private DcrDocumentService dcrDocumentService;
    @Autowired
    private DcrService dcrService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private DcrApplicationNumberGenerator dcrApplicationNumberGenerator;
    @Autowired
    private FileStoreService fileStoreService;
    @Autowired
    private PortalInetgrationService portalInetgrationService;
    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;
  
    @Transactional
    public EdcrApplication create(final EdcrApplication edcrApplication) {

        edcrApplication.setApplicationDate(new Date());
        edcrApplication.setApplicationNumber(applicationNumberGenerator.generate());
        edcrApplication.setSavedDxfFile(saveDXF(edcrApplication));
        edcrApplication.setPlanInformation(edcrApplication.getPlanInformation());
        edcrApplicationRepository.save(edcrApplication);
        callDcrProcess(edcrApplication);
        portalInetgrationService.createPortalUserinbox(edcrApplication, Arrays.asList(securityUtils.getCurrentUser()));
        return edcrApplication;  
    }

    private void callDcrProcess(EdcrApplication edcrApplication) {
        try {
            dcrService.process(edcrApplication.getSavedDxfFile(), edcrApplication);
        } catch (Exception e) {
          LOG.error("Error in edcr Processing",e);
            //e.printStackTrace();
        }
    }

    /*public void saveDcrApplication(EdcrApplication edcrApplication) {
        saveDXF(edcrApplication);

        dcrService.process(edcrApplication.getSavedDxfFile(), edcrApplication);
    }
*/
    private File saveDXF(EdcrApplication edcrApplication) {
        FileStoreMapper fileStoreMapper = addToFileStore(edcrApplication.getDxfFile());
        File dxfFile = fileStoreService.fetch(fileStoreMapper.getFileStoreId(), FILESTORE_MODULECODE);
        buildDocuments(edcrApplication, fileStoreMapper, null);
        edcrApplication.setDcrDocuments(edcrApplication.getDcrDocuments());
        return dxfFile;

    }

    @Transactional
    public void saveOutputReport(EdcrApplication edcrApplication, ReportOutput reportOutput) {

        ByteArrayInputStream fileStream = new ByteArrayInputStream(reportOutput.getReportOutputData());

        final String fileName = edcrApplication.getApplicationNumber() + ".pdf";

        final FileStoreMapper fileStoreMapper = fileStoreService.store(fileStream, fileName, "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        buildDocuments(edcrApplication, null, fileStoreMapper);

        dcrDocumentService.saveAll(edcrApplication.getDcrDocuments());
    }

    private void buildDocuments(EdcrApplication edcrApplication, FileStoreMapper dxfFile, FileStoreMapper reportOutput) {
        DcrDocument dcrDocument = new DcrDocument();

        if (dxfFile != null)
            dcrDocument.setDxfFileId(dxfFile);

        if (reportOutput != null)
            dcrDocument.setReportOutputId(reportOutput);

        dcrDocument.setApplication(edcrApplication);
       
        List<DcrDocument> dcrDocuments = new ArrayList<>();
        dcrDocuments.add(dcrDocument);
        edcrApplication.setSavedDcrDocument(dcrDocument);
        edcrApplication.setDcrDocuments(dcrDocuments);
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

    @Transactional
    public EdcrApplication update(final EdcrApplication edcrApplication) {
        edcrApplication.setSavedDxfFile(saveDXF(edcrApplication));
        EdcrApplication applicationRes = edcrApplicationRepository.save(edcrApplication);
        callDcrProcess(edcrApplication);
        portalInetgrationService.updatePortalUserinbox(applicationRes, securityUtils.getCurrentUser());
        return applicationRes;
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