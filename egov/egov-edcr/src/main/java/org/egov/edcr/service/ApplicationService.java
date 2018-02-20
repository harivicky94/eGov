package org.egov.edcr.service;

import org.egov.edcr.entity.DxfDocument;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.utils.ApplicationNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public void saveDcrApplication(EdcrApplication edcrApplication) {
        saveDXF(edcrApplication);

        dcrService.process(edcrApplication.getDxfFile(), edcrApplication);
    }

    private void saveDXF(EdcrApplication edcrApplication) {
        DxfDocument dxfDocument = new DxfDocument();

        FileStoreMapper fileStoreMapper = addToFileStore(edcrApplication.getDxfFile());
        dxfDocument.setFileStoreId(fileStoreMapper);
        dxfDocument.setApplication(edcrApplication);

        List<DxfDocument> dxfDocuments = new ArrayList<>();
        dxfDocuments.add(dxfDocument);

        edcrApplication.setDxfDocuments(dxfDocuments);
        edcrApplication.setApplicationNumber(applicationNumberGenerator.generate());

        edcrApplicationService.create(edcrApplication);
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
