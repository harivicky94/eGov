package org.egov.edcr.rule;

import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.DcrConstants;

public class Rule26 implements DcrGeneralRule {

    private HashMap<String, HashMap<String, Object>> reportOutput = new HashMap<String, HashMap<String, Object>>();
    private HashMap<String, String> errors = new HashMap<String, String>();
    private HashMap<String, String> generalInformation = new HashMap<String, String>();

    @Override
    public PlanDetail validate(PlanDetail planDetail) {

        if (planDetail != null && planDetail.getLandDetail() != null) {

            //If either notified or non notified road width not defined, then show error.
            if (planDetail.getLandDetail().getNotifiedRoad() != null &&
                    planDetail.getLandDetail().getNonNotifiedRoad() != null
                    && !(planDetail.getLandDetail().getNonNotifiedRoad().getPresentInDxf() ||
                    planDetail.getLandDetail().getNotifiedRoad().getPresentInDxf() )) {
                errors.put(DcrConstants.ERROR_ROAD_NOTDEFINED_KEY,
                        DcrConstants.ERROR_ROAD_NOTDEFINED_VALUE);
            }
        }

        if (planDetail != null && planDetail.getBuildingDetail() != null) {

            // shortest distinct to road defined or not
            if (planDetail.getBuildingDetail().getShortestDistanceToRoad() != null &&
                    planDetail.getBuildingDetail().getShortestDistanceToRoad().getPresentInDxf()) {
                generalInformation.put(DcrConstants.SHORTESTDISTINCTTOROAD_DEFINED_KEY,
                        DcrConstants.SHORTESTDISTINCTTOROAD_DEFINED_VALUE
                                + planDetail.getBuildingDetail().getShortestDistanceToRoad().getLength());
            } else {
                errors.put(DcrConstants.ERROR_SHORTESTDISTINCTTOROAD_NOTDEFINED_KEY,
                        DcrConstants.ERROR_SHORTESTDISTINCTTOROAD_NOTDEFINED_VALUE);

            }
            //waste disposal defined or not
            if (planDetail.getBuildingDetail().getWasteDisposal() != null &&
                    planDetail.getBuildingDetail().getWasteDisposal().getPresentInDxf()) {
                generalInformation.put(DcrConstants.WASTEDISPOSAL_DEFINED_KEY,
                        DcrConstants.WASTEDISPOSAL_DEFINED_VALUE);
            } else {
                errors.put(DcrConstants.WASTEDISPOSAL_DEFINED_KEY,
                        DcrConstants.WASTEDISPOSAL_NOTDEFINED_VALUE);

            }
        }
        planDetail.addErrors(errors);
        planDetail.addGeneralInformation(generalInformation);
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        
        //TODO: NEED TO ADD APPLICABLE NOT APPLICABLE.. IRRESPECTIVE OF DATA PROVIDED or not.
        
        //TODO: CHECK MULTIPLE ROADS APPROACHING SITE.
        HashMap<String, Object> shortDistanceToRoad = new HashMap<String, Object>();
        
        if (planDetail.getBuildingDetail().getShortestDistanceToRoad() != null )
            if(!planDetail.getBuildingDetail().getShortestDistanceToRoad().getPresentInDxf()) {
            shortDistanceToRoad.put(DcrConstants.SHORTESTDISTINCTTOROAD_DEFINED_KEY,
                    DcrConstants.ERROR_SHORTESTDISTINCTTOROAD_NOTDEFINED_VALUE
                           );
            if(reportOutput.containsKey(DcrConstants.RULE26))
            {
               reportOutput.get(DcrConstants.RULE26).put(DcrConstants.RULE26, shortDistanceToRoad);
            }else
                reportOutput.put(DcrConstants.RULE26, shortDistanceToRoad);
             }
        
         rule26A(planDetail);
        
        return planDetail;
    }

    private void rule26A(PlanDetail planDetail) {
        
        HashMap<String, Object> wasteDisposal = new HashMap<String, Object>();
        if (planDetail.getBuildingDetail().getWasteDisposal() != null &&
                planDetail.getBuildingDetail().getWasteDisposal().getPresentInDxf()) {
            wasteDisposal.put(DcrConstants.ERROR_SHORTESTDISTINCTTOROAD_NOTDEFINED_KEY,
                    DcrConstants.WASTEDISPOSAL_DEFINED_VALUE);

            if (reportOutput.containsKey(DcrConstants.RULE26)) {
                reportOutput.get(DcrConstants.RULE26).put(DcrConstants.RULE26, wasteDisposal);
            } else
                reportOutput.put(DcrConstants.RULE26, wasteDisposal);
        } else

        {
            wasteDisposal.put(DcrConstants.WASTEDISPOSAL_DEFINED_KEY,
                    DcrConstants.WASTEDISPOSAL_NOTDEFINED_VALUE);
            if (reportOutput.containsKey(DcrConstants.RULE26)) {
                reportOutput.get(DcrConstants.RULE26).put(DcrConstants.RULE26, wasteDisposal);
            } else
                reportOutput.put(DcrConstants.RULE26, wasteDisposal);
        }
    }
        

}
