package org.egov.edcr.service;

import java.util.List;

import org.egov.edcr.entity.EdcrApplicationDetail;
import org.egov.edcr.repository.EdcrApplicationDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EdcrApplicationDetailService {

    @Autowired
    private EdcrApplicationDetailRepository edcrApplicationDetailRepository;

    public void save(EdcrApplicationDetail edcrApplicationDetail) {
        edcrApplicationDetailRepository.save(edcrApplicationDetail);
    }

    public void saveAll(List<EdcrApplicationDetail> edcrApplicationDetails) {
        edcrApplicationDetailRepository.save(edcrApplicationDetails);
    }

    public List<EdcrApplicationDetail> fingByDcrApplicationId(Long dcrApplicationId) {
       return edcrApplicationDetailRepository.findByApplicationId(dcrApplicationId);
    }

    public EdcrApplicationDetail findByDcrNumber(final String dcrNumber) {
        return edcrApplicationDetailRepository.findByDcrNumber(dcrNumber);
    }
}
