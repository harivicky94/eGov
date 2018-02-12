package org.egov.edcr.entity;

import org.apache.commons.lang3.StringUtils;

public enum MeasurementType {
    SIDEYARD1, FRONTYARD, REARYARD, SIDEYARD, PLOTAREA, BUILDINGAREA;

    @Override
    public String toString() {
        return StringUtils.capitalize(name());
    }
}
