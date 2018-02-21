package org.egov.edcr.service;

import org.egov.edcr.entity.DcrDocument;
import org.egov.edcr.repository.DcrDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DcrDocumentService {

    @Autowired
    private DcrDocumentRepository dcrDocumentRepository;

    public void save(DcrDocument dcrDocument) {
        dcrDocumentRepository.save(dcrDocument);
    }

    public void saveAll(List<DcrDocument> dcrDocuments) {
        dcrDocumentRepository.save(dcrDocuments);
    }
}
