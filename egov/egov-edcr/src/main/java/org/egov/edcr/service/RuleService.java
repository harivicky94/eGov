package org.egov.edcr.service;

import org.egov.edcr.entity.Rule;
import org.egov.edcr.repository.RuleRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RuleService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RuleRepository ruleRepository;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }

    public Rule findById(Long id) {
        return ruleRepository.findOne(id);
    }

    public Rule findByName(String name) {
        return ruleRepository.findByName(name);
    }
}
