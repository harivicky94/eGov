package org.egov.edcr.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.repository.PlanRuleRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanRuleService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlanRuleRepository planRuleRepository;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    public List<PlanRule> findRulesByPlanDetail(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        PlanRule planRule = new PlanRule();
        planRule.setRules("23,26,60,61,62");
        List<PlanRule> rules = new ArrayList<>();
        rules.add(planRule);
        return rules;
        // BASED ON PLAN DETAIL, GET DIFFERENT RULES.
        /*
         * final Criteria criteria = buildSearchCriteria(planDetail); return criteria.list();
         */
    }

    public List<PlanRule> findAll() {
        return planRuleRepository.findAll();
    }

    public PlanRule findId(Long id) {
        return planRuleRepository.findOne(id);
    }

}
