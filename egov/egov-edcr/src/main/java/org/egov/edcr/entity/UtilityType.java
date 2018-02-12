package org.egov.edcr.entity;

import org.apache.commons.lang3.StringUtils;

public enum UtilityType {

    WELL, COMPOUNTWALL;

    @Override
    public String toString() {
        return StringUtils.capitalize(name());
    }
}
