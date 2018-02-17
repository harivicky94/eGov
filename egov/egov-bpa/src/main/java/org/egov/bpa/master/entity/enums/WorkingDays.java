package org.egov.bpa.master.entity.enums;

public enum WorkingDays {

	MON("MON"), TUE("TUE"),WED("WED"),THURS("THURS"),FRI("FRI");
    
    private final String wDaysVal;

    private WorkingDays(String wDaysVal) {
        this.wDaysVal = wDaysVal;
    }

    public String getHolidayTypeVal() {
		return wDaysVal;
	}

}
