package org.egov.bpa.master.entity.enums;

public enum ApplicationType {
	ONE_DAY_PERMIT("ONE_DAY_PERMIT"), ALL_OTHER_SERVICES("ALL_OTHER_SERVICES");
    
    private final String holidayTypeTypeVal;

    private ApplicationType(String hTypeVal) {
        this.holidayTypeTypeVal = hTypeVal;
    }

    public String getHolidayTypeVal() {
		return holidayTypeTypeVal;
	}

	@Override
    public String toString() {
		return holidayTypeTypeVal.replace("_", "");
    }
}
