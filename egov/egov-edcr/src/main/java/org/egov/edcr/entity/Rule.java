package org.egov.edcr.entity;

import java.util.List;

public class Rule {

    private String name;

    private String clause;

    private List<SubRule> subRules;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClause() {
        return clause;
    }

    public void setClause(String clause) {
        this.clause = clause;
    }

    public List<SubRule> getSubRules() {
        return subRules;
    }

    public void setSubRules(List<SubRule> subRules) {
        this.subRules = subRules;
    }
}
