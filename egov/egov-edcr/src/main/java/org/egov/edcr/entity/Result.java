package org.egov.edcr.entity;

public enum Result {
    Accepted, NA, Not_Accepted;

    @Override
    public String toString() {

        return name().replace("_", "");
    }
}
