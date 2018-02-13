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

import static org.springframework.util.StringUtils.isEmpty;

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
            criteria.add(Restrictions.eq("planRule.plotArea", planDetail.getPlanInformation().getPlotArea()));
        }

        if (planDetail.getBuildingDetail() != null && planDetail.getBuildingDetail().getBuildingHeight() != null) {
            criteria.add(Restrictions.eq("planRule.heightofbuilding", planDetail.getBuildingDetail().getBuildingHeight()));

        }

        if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOccupancy() != null) {
            criteria.add(Restrictions.ilike("planRule.occupancy", planDetail.getPlanInformation().getOccupancy(),
                    MatchMode.ANYWHERE));
        }

        if (planDetail.getBuildingDetail()!= null && !planDetail.getBuildingDetail().getFloors().isEmpty()) {
            criteria.add(Restrictions.eq("planRule.nooffloors", planDetail.getBuildingDetail().getFloors().size()));
        }

        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        return criteria;
    }

}
