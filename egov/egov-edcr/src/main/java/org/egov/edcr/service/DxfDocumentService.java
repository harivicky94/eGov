package org.egov.edcr.service;

import org.egov.edcr.entity.DxfDocument;
import org.egov.edcr.repository.DxfDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DxfDocumentService {

    @Autowired
    private DxfDocumentRepository dxfDocumentRepository;

    public void save(DxfDocument dxfDocument) {
        dxfDocumentRepository.save(dxfDocument);
    }

    public void saveAll(List<DxfDocument> dxfDocuments) {
        dxfDocumentRepository.save(dxfDocuments);
    }
}
