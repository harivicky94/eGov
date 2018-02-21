package org.egov.edcr.service;

import org.egov.edcr.entity.DxfDocument;
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
    private DxfDocumentService dxfDocumentService;

    public void saveDcrApplication(EdcrApplication edcrApplication) {
        saveDXF(edcrApplication);

        dcrService.process(edcrApplication.getDxfFile(), edcrApplication);
    }

    private void saveDXF(EdcrApplication edcrApplication) {

        FileStoreMapper fileStoreMapper = addToFileStore(edcrApplication.getDxfFile());

        buildDocuments(edcrApplication, fileStoreMapper, null);

        edcrApplication.setApplicationNumber(applicationNumberGenerator.generate());

        dxfDocumentService.saveAll(edcrApplication.getDxfDocuments());
    }


    @Transactional
    public void saveOutputReport(EdcrApplication edcrApplication, ReportOutput reportOutput) {

        ByteArrayInputStream fileStream = new ByteArrayInputStream(reportOutput.getReportOutputData());

        final String fileName = edcrApplication.getApplicationNumber() + ".pdf";

        final FileStoreMapper fileStoreMapper = fileStoreService.store(fileStream, fileName, "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        buildDocuments(edcrApplication, null, fileStoreMapper);

        dxfDocumentService.saveAll(edcrApplication.getDxfDocuments());
    }

    private void buildDocuments(EdcrApplication edcrApplication, FileStoreMapper dxfFile, FileStoreMapper reportOutput) {
        DxfDocument dxfDocument = new DxfDocument();

        if (dxfFile != null) {
            dxfDocument.setDxfFileId(dxfFile);
        }

        if (reportOutput != null) {
            dxfDocument.setReportOutputId(reportOutput);
        }

        dxfDocument.setApplication(edcrApplication);

        List<DxfDocument> dxfDocuments = new ArrayList<>();
        dxfDocuments.add(dxfDocument);

        edcrApplication.setDxfDocuments(dxfDocuments);
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
