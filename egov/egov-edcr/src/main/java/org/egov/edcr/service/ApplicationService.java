package org.egov.edcr.service;

import org.egov.edcr.entity.DcrDocument;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.egov.edcr.utility.DcrConstants.FILESTORE_MODULECODE;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private EdcrApplicationService edcrApplicationService;

    @Autowired
    private ApplicationNumberGenerator applicationNumberGenerator;

    @Autowired
    private DcrService dcrService;

    @Autowired
    private DcrDocumentService dcrDocumentService;

    public void saveDcrApplication(EdcrApplication edcrApplication) {
        saveDXF(edcrApplication);

        dcrService.process(edcrApplication.getDxfFile(), edcrApplication);
    }

    private void saveDXF(EdcrApplication edcrApplication) {

        FileStoreMapper fileStoreMapper = addToFileStore(edcrApplication.getDxfFile());

        buildDocuments(edcrApplication, fileStoreMapper, null);

        edcrApplication.setApplicationNumber(applicationNumberGenerator.generate());

        dcrDocumentService.saveAll(edcrApplication.getDcrDocuments());
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

        if (dxfFile != null) {
            dcrDocument.setDxfFileId(dxfFile);
        }

        if (reportOutput != null) {
            dcrDocument.setReportOutputId(reportOutput);
        }

        dcrDocument.setApplication(edcrApplication);

        List<DcrDocument> dcrDocuments = new ArrayList<>();
        dcrDocuments.add(dcrDocument);

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

}
