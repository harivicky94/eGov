package org.egov.edcr.service;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.repository.PlanRuleRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

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

        //BASED ON PLAN DETAIL, GET DIFFERENT RULES.
        final Criteria criteria = buildSearchCriteria(planDetail);
        return criteria.list();
    }

    public List<PlanRule> findAll() {
        return planRuleRepository.findAll();
    }

    public PlanRule findId(Long id) {
        return planRuleRepository.findOne(id);
    }

    private Criteria buildSearchCriteria(final PlanDetail planDetail) {
        final Criteria criteria = getCurrentSession().createCriteria(PlanRule.class, "planRule");

        if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getPlotArea() != null) {
            criteria.add(Restrictions.gt("minplotarea", planDetail.getPlanInformation().getPlotArea()));
            criteria.add(Restrictions.le("maxplotarea", planDetail.getPlanInformation().getPlotArea()));
        }

        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null) {
            criteria.add(Restrictions.gt("minbuildinghgt", planDetail.getBuilding().getBuildingHeight()));
            criteria.add(Restrictions.le("maxbuildinghgt", planDetail.getBuilding().getBuildingHeight()));
        }

        if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOccupancy() != null) {
            criteria.add(Restrictions.ilike("planRule.occupancy", planDetail.getPlanInformation().getOccupancy(),
                    MatchMode.ANYWHERE));
        }

        if (planDetail.getBuilding() != null && !planDetail.getBuilding().getFloors().isEmpty()) {
            criteria.add(Restrictions.gt("minfloors", planDetail.getBuilding().getFloors().size()));
            criteria.add(Restrictions.le("maxfloors", planDetail.getBuilding().getFloors().size()));
        }

        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria;
    }

}
