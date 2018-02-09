package org.egov.bpa.transaction.entity.enums;

public enum HolidayType {
	CASUAL("CASUAL"), SPECIAL("SPECIAL");

	private final String holidayTypeTypeVal;

	private HolidayType(String hTypeVal) {
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

