package org.egov.edcr.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.edcr.entity.SubRule;
import org.egov.edcr.repository.SubRuleRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

public class SubRuleService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SubRuleRepository subRuleRepository;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<SubRule> findAll() {
        return subRuleRepository.findAll();
    }

    public SubRule findOne(Long id) {
        return subRuleRepository.findOne(id);
    }
}
