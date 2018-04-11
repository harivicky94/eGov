package org.egov.edcr.entity;

public enum OccupancyType {

    OCCUPANCY_A1("RESIDENTIAL"), OCCUPANCY_A2("SPECIAL_RESIDENTIAL"), OCCUPANCY_B1("EDUCATIONAL(GENERAL)"), OCCUPANCY_B2(
            "EDUCATIONAL_HIGHSCHOOL"), OCCUPANCY_B3("HIGHER_EDUCATIONAL_INSTITUTE"), OCCUPANCY_C("MEDICAL/HOSPITAL"), OCCUPANCY_D(
                    "ASSEMBLY"), OCCUPANCY_E("OFFICE_BUSINESS"), OCCUPANCY_F("COMMERCIAL_MERCHANTILE"), OCCUPANCY_G1(
                            "INDUSTRIAL"), OCCUPANCY_G2("SMALL_INDUSTRIAL"), OCCUPANCY_H("STORAGE"), OCCUPANCY_I1(
                                    "HAZARDOUS_1"), OCCUPANCY_I2("HAZARDOUS_2"), OCCUPANCY_D1("ASSEMBLY_WORSHIP");

    private final String occupancyTypeVal;

    OccupancyType(String aTypeVal) {
        this.occupancyTypeVal = aTypeVal;
    }

    public String getOccupancyType() {
        return occupancyTypeVal;
    }

    public String getOccupancyTypeVal() {
        return occupancyTypeVal;
    }
}
