package org.egov.edcr.entity;

public enum Result {
    Accepted, NA, Not_Accepted,Verify;

    @Override
    public String toString() {

        return name().replace("_", "");
    }
}
