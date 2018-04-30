package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.service.MinDistance;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SetBackService extends GeneralRule implements RuleService {
    private static final double VALUE_0_5 = 0.5;
    private static final String LEVEL = " Level ";
    private static final String RULE_24 = "24";
    private static final String RULE_24_3 = "24-(3)";
    private static final String RULE_54 = "54";
    private static final String RULE_54_3_I = "54-(3-i)";
    private static final String RULE_54_3 = "54(3)";

    private static final String RULE_54_3_II = "54-(3-ii)";

    private static final String Rule_55 = "55";
    private static final String RULE_55_2_1 = "55-2-(1)";
    private static final String RULE_55_2_2 = "55-2-(2)";
    private static final String RULE_55_2_PROV = "55-2(Prov)";
    private static final String RULE_55_2_3 = "55-2-(3)";
    private static final String RULE_56 = "56";
    private static final String RULE563D = "56-(3d)";
    private static final String RULE_57 = "57";
    private static final String RULE_57_4 = "57-(4)";
    private static final String RULE_59 = "59";
    private static final String RULE_59_3 = "59-(3)";
    private static final String RULE_62 = "62";
    private static final String RULE_62_1_A = "62-(1-a)";
    private static final String SUB_RULE_24_3 = "24(3)";
    private static final String SUB_RULE_24_5 = "24(5)";

    private static final String SUB_RULE_24_3_DESCRIPTION = "Front yard distance";
    private static final String  meanMinumumLabel = "(Minimum distance,Mean distance) ";
    private static final BigDecimal FIVE = BigDecimal.valueOf(5);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_8 = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_1_2 = BigDecimal.valueOf(1.2);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_4_5 = BigDecimal.valueOf(4.5);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_5 = FIVE;
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_6 = BigDecimal.valueOf(6);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE_7_5 = BigDecimal.valueOf(7.5);

    private static final BigDecimal FRONTYARDMEAN_DISTANCE_1_8 = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_3 = BigDecimal.valueOf(3);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_5 = FIVE;
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_6 = BigDecimal.valueOf(6);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_7_5 = BigDecimal.valueOf(7.5);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE_10_5 = BigDecimal.valueOf(10.5);
    private static final BigDecimal SIDEVALUE_ZERO = BigDecimal.valueOf(0);
    private static final BigDecimal SIDEVALUE_SIXTY_CM = BigDecimal.valueOf(0.60);
    private static final BigDecimal SIDEVALUE_SEVENTYFIVE_CM = BigDecimal.valueOf(0.75);
    private static final BigDecimal SIDEVALUE_NINTY_CM = BigDecimal.valueOf(0.90);
    private static final BigDecimal SIDEVALUE_ONE = BigDecimal.valueOf(1);
    private static final BigDecimal SIDEVALUE_ONE_TWO = BigDecimal.valueOf(1.2);

    private static final BigDecimal SIDEVALUE_ONEPOINTFIVE = BigDecimal.valueOf(1.5);
    private static final BigDecimal SIDEVALUE_TWO = BigDecimal.valueOf(2);
    private static final BigDecimal SIDEVALUE_THREE = BigDecimal.valueOf(3);
    private static final BigDecimal SIDEVALUE_FOUR= BigDecimal.valueOf(4);
    private static final BigDecimal SIDEVALUE_FIVE= BigDecimal.valueOf(5);
    private static final BigDecimal SIDEVALUE_SEVEN_FIVE = BigDecimal.valueOf(7.5);


    
    private static final int SITEAREA_125 = 125;
    private static final int BUILDUPAREA_300 = 300;
    private static final int BUILDUPAREA_150 = 150;

    private static final int FLOORAREA_800 = 800;
    private static final int FLOORAREA_500 = 500;
    private static final int FLOORAREA_300 =300;
    private static final String SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING = " 0 MTR With No opening on side up to 2.1 MTR height and NOC to Abut next plot";
    private static final String SIDE_YARD_1_EXPECTED_NO_OPENING = " Minimum .75 MTR With no opening on side up to 2.1 MTR height";
    private static final String SIDE_YARD_1_EXPECTED = " Minimum 1 MTR ";
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(1.2);

    
    private static final String SIDE_YARD_1_DESC = "Minimum open space on Side 1";
    private static final String SIDE_YARD_2_DESC = "Minimum open space on Side 2";
    
    private static HashMap<String, String> errors = new HashMap<>();

    private Logger logger = Logger.getLogger(SetBackService.class);
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractSetBack(pl, doc);
          
     
        if (pl.getBasement() != null) {
            pl.getPlot().setBsmtFrontYard(getYard(pl, doc, DxfFileConstants.BSMNT_FRONT_YARD));
            pl.getPlot().setBsmtRearYard(getYard(pl, doc, DxfFileConstants.BSMNT_REAR_YARD));
            pl.getPlot().setBsmtSideYard1(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_1));
            pl.getPlot().setBsmtSideYard2(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_2));

            pl.getPlot().getBsmtFrontYard()
                    .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_FRONT_YARD));
            pl.getPlot().getBsmtSideYard1()
                    .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_1));
            pl.getPlot().getBsmtSideYard2()
                    .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_2));
            pl.getPlot().getBsmtRearYard()
                    .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_REAR_YARD));
        }


        
        return pl;
    }

    private void extractSetBack(PlanDetail pl, DXFDocument doc) {
        DXFLayer yardLayer = new DXFLayer();
        String yardName ="";
        int yardLevel = 0;
        while (yardLayer != null) {        
                 SetBack setBack = new SetBack();
                 setBack.setLevel(yardLevel);
                 
                  yardName = DxfFileConstants.FRONT_YARD  + "_L_"+ yardLevel;
                  yardLayer = doc.getDXFLayer(yardName);
                   if (!yardLayer.getName().equalsIgnoreCase(yardName)) {
                      break;
                  }
                   
                   String height = Util.getMtextByLayerName(doc, DxfFileConstants.MTEXT_NAME_HEIGHT_M);
                   if (!height.isEmpty()){
                       height = height.replaceAll("[^\\d.]", "");
                       setBack.setHeight(BigDecimal.valueOf(Double.parseDouble(height)));
                   }
                         
                  Yard yard= getYard(pl, doc, DxfFileConstants.FRONT_YARD+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.FRONT_YARD+ "_L_"+ yardLevel));
                        setBack.setFrontYard(yard);
                      //  pl.getPlot().setFrontYard(yard);
                    }
            
                    yard = getYard(pl, doc, DxfFileConstants.REAR_YARD+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.REAR_YARD+ "_L_"+ yardLevel));
                        setBack.setRearYard(yard);
                       // pl.getPlot().setRearYard(yard);
                    }
            
                    yard = getYard(pl, doc, DxfFileConstants.SIDE_YARD_1+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_1+ "_L_"+ yardLevel));
                        setBack.setSideYard1(yard);
                        //pl.getPlot().setSideYard1(yard);
                    }
                    yard = getYard(pl, doc, DxfFileConstants.SIDE_YARD_2+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_2+ "_L_"+ yardLevel));
            
                        setBack.setSideYard2(yard);
                      //  pl.getPlot().setSideYard2(yard);
                    }
                    yardLevel++;
                    pl.getPlot().getSetBacks().add(setBack);

        }
    }

    private Yard getYard(PlanDetail pl, DXFDocument doc, String yardName) {
        Yard yard = new Yard();
        List<DXFLWPolyline> frontYardLines = Util.getPolyLinesByLayer(doc, yardName);
        if (!frontYardLines.isEmpty()) {
            yard.setPolyLine(frontYardLines.get(0));
            yard.setArea(Util.getPolyLineArea(yard.getPolyLine()));
            yard.setMean(yard.getArea().divide(BigDecimal.valueOf(yard.getPolyLine().getBounds().getWidth()), 5,
                    RoundingMode.HALF_UP));
            if(logger.isDebugEnabled()) logger.debug(yardName + " Mean " + yard.getMean());
            yard.setPresentInDxf(true);

        } else
            pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[] { yardName }, null));

        return yard;

    }
    
   
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    
    public PlanDetail process(PlanDetail pl) {
        rule24_3(pl, DcrConstants.NON_BASEMENT);

        return pl;
    }

    private void rule24_5(PlanDetail planDetail, String type) {
        validateRule24_5(planDetail);
        Yard sideYard1 = null;
        Yard sideYard2 = null;
        String subRule = "";
        String side1Desc = "";
        String side2Desc = "";
        String side1FieldName = "";
        String side2FieldName = "";
        
        if (planDetail.getPlot() == null)
            return;
        SetBack setback = null;
        subRule = RULE_24_3;
        Plot plot = planDetail.getPlot();

        if (plot != null && !planDetail.getPlot().getSetBacks().isEmpty()) {
            setback = planDetail.getPlot().getLevelZeroSetBack(); //Default taking level zero setback.
            if (setback != null && setback.getSideYard1() != null) {
                sideYard1 = setback.getSideYard1();
            }
            if (setback != null && setback.getSideYard2() != null) {
                sideYard2 = setback.getSideYard2();
            }
           }
          
            side1Desc = SIDE_YARD_1_DESC;
            side2Desc = SIDE_YARD_2_DESC;
            side1FieldName = DcrConstants.SIDE_YARD1_DESC;
            side2FieldName = DcrConstants.SIDE_YARD2_DESC;
        

        if (sideYard1.getMean() == null)
            return;

        if (sideYard1.getMinimumDistance() == null)
            return;

        if (sideYard2.getMean() == null)
            return;

        if (sideYard2.getMinimumDistance() == null)
            return;

        if (planDetail.getBuilding() == null || planDetail.getVirtualBuilding().getBuildingHeight() == null)
            return;

        Boolean valid1 = false;
        Boolean valid2 = false;
        BigDecimal buildingHeight = planDetail.getVirtualBuilding().getBuildingHeight();
        
                    
        double min = 0;
        double max = 0;
        if (sideYard2.getMinimumDistance().doubleValue() > sideYard1.getMinimumDistance().doubleValue()) {
            min = sideYard1.getMinimumDistance().doubleValue();
            max = sideYard2.getMinimumDistance().doubleValue();
        } else {
            min = sideYard2.getMinimumDistance().doubleValue();
            max = sideYard1.getMinimumDistance().doubleValue();
        }
 
        for(OccupancyType  mostRestrictiveOccupancy : planDetail.getVirtualBuilding().getOccupancies() )  {
            if (buildingHeight != null) {
                if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0) {
                     checkSideYardLessThanTenOrEqualToMts(planDetail, plot,   min, max,mostRestrictiveOccupancy);

                }  else if (buildingHeight.compareTo(BigDecimal.valueOf(10))> 0
                        && buildingHeight.compareTo(BigDecimal.valueOf(16)) <= 0) {
                     checkSideYardBetweenTenToSixteenMts(planDetail, plot,   min, max,mostRestrictiveOccupancy);

                } else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) > 0) {
                     checkSideYardMoreThanSixteenMts(planDetail, plot,   min, max,mostRestrictiveOccupancy);

                }

            }
      }
    }
    
 private void checkSideYardMoreThanSixteenMts(PlanDetail planDetail, Plot plot, double min, double max,
            OccupancyType mostRestrictiveOccupancy) {

     String subRule = RULE_24_3;
     String rule=DcrConstants.RULE24;
     String side1Desc = SIDE_YARD_1_DESC;
     String side2Desc = SIDE_YARD_2_DESC;
     String side1FieldName = DcrConstants.SIDE_YARD1_DESC;
     String side2FieldName = DcrConstants.SIDE_YARD2_DESC;
 
     Boolean valid1 = false;
     Boolean valid2 = false;
     BigDecimal buildingHeight = planDetail.getVirtualBuilding().getBuildingHeight();
     BigDecimal side1val = SIDEVALUE_ONE;
     BigDecimal side2val = SIDEVALUE_ONE_TWO;

     
     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {


         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
             rule = RULE_62;
             subRule = RULE_62_1_A;
             side1val = SIDEVALUE_SIXTY_CM;
             side2val = SIDEVALUE_NINTY_CM;
         }else
         {
             subRule = SUB_RULE_24_3;
             rule = DcrConstants.RULE24;
             side1val = SIDEVALUE_ONE;
             side2val = SIDEVALUE_ONE_TWO;
         }
         for (SetBack setbacks : planDetail.getPlot().getSetBacks()) {
             if (setbacks.getHeight() != null && setbacks.getHeight().compareTo(BigDecimal.TEN) >= 0) {
                 rule = RULE_24;
                 subRule = RULE_24_3;
                 BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));
                    side1val = side1val.add(distanceIncrementBasedOnHeight).compareTo(FIVE) <= 0 ? FIVE
                            : side1val.add(distanceIncrementBasedOnHeight);
                    side2val = side2val.add(distanceIncrementBasedOnHeight).compareTo(FIVE) <= 0 ? FIVE
                            : side2val.add(distanceIncrementBasedOnHeight);
     
                 if (max >= (side2val).doubleValue())
                     valid2 = true;
                 if (min >= (side1val).doubleValue())
                     valid1 = true;
                    
                 if (valid1) {

                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                                     side1FieldName,
                                     side1val.toString(),
                                     min + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                                     side1FieldName,
                                     side1val.toString(),
                                     min + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));

                 }
                 if (valid2) {

                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                                     side2FieldName,
                                     side2val.toString() + DcrConstants.IN_METER,
                                     max + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                                     side2FieldName,
                                     side2val.toString() + DcrConstants.IN_METER,
                                     max + DcrConstants.IN_METER,
                                     Result.Not_Accepted,
                                     null));

                 }
             }

         }
        } else {
            BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil((buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));

            processSideYardOtherThanResidentialCases(planDetail, plot, min, max, mostRestrictiveOccupancy, subRule, rule,
                    side1Desc, side2Desc, side1FieldName, side2FieldName, valid1, valid2, buildingHeight, side1val, side2val,distanceIncrementBasedOnHeight,true);

        }
 
        
    }

private void checkSideYardBetweenTenToSixteenMts(PlanDetail planDetail, Plot plot, double min, double max,
            OccupancyType mostRestrictiveOccupancy) {
     String subRule = RULE_24_3;
     String rule=DcrConstants.RULE24;
     String side1Desc = SIDE_YARD_1_DESC;
     String side2Desc = SIDE_YARD_2_DESC;
     String side1FieldName = DcrConstants.SIDE_YARD1_DESC;
     String side2FieldName = DcrConstants.SIDE_YARD2_DESC;
 
     Boolean valid1 = false;
     Boolean valid2 = false;
     BigDecimal buildingHeight = planDetail.getVirtualBuilding().getBuildingHeight();
     String side1Expected = "";
     BigDecimal side1val = SIDEVALUE_ONE;
     BigDecimal side2val = SIDEVALUE_ONE_TWO;

     
     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {


         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
             rule = RULE_62;
             subRule = RULE_62_1_A;
             side1val = SIDEVALUE_SIXTY_CM;
             side2val = SIDEVALUE_NINTY_CM;
         }else
         {
             subRule = SUB_RULE_24_3;
             rule = DcrConstants.RULE24;
             side1val = SIDEVALUE_ONE;
             side2val = SIDEVALUE_ONE_TWO;
         }
         for (SetBack setbacks : planDetail.getPlot().getSetBacks()) {
             if (setbacks.getHeight() != null && setbacks.getHeight().compareTo(BigDecimal.TEN) >= 0) {
                 rule = RULE_24;
                 subRule = RULE_24_3;
                 BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));
                         side2val=    side2val.add(distanceIncrementBasedOnHeight);
                         side1val=    side1val.add(distanceIncrementBasedOnHeight);                 
                 if (max >= (side2val).doubleValue())
                     valid2 = true;
                 if (min >= (side1val).doubleValue())
                     valid1 = true;
           
                 
                 if (valid1) {

                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                                     side1FieldName,
                                     side1val.toString(),
                                     min + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                                     side1FieldName,
                                     side1val.toString(),
                                     min + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));

                 }
                 if (valid2) {

                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                                     side2FieldName,
                                     side2val.toString() + DcrConstants.IN_METER,
                                     max + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                                     side2FieldName,
                                     side2val.toString() + DcrConstants.IN_METER,
                                     max + DcrConstants.IN_METER,
                                     Result.Not_Accepted,
                                     null));

                 }
             }

         }
        } else {
            BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                    .multiply(BigDecimal
                            .valueOf(Math.ceil((buildingHeight.subtract(BigDecimal.TEN)
                                    .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));

            processSideYardOtherThanResidentialCases(planDetail, plot, min, max, mostRestrictiveOccupancy, subRule, rule,
                    side1Desc, side2Desc, side1FieldName, side2FieldName, valid1, valid2, buildingHeight, side1val, side2val,distanceIncrementBasedOnHeight,false);

        }
 }

private void processSideYardOtherThanResidentialCases(PlanDetail planDetail, Plot plot, double min, double max,
        OccupancyType mostRestrictiveOccupancy, String subRule, String rule, String side1Desc, String side2Desc,
        String side1FieldName, String side2FieldName, Boolean valid1, Boolean valid2, BigDecimal buildingHeight,
        BigDecimal side1val, BigDecimal side2val,BigDecimal distanceIncrementBasedOnHeight, Boolean checkMinimum5mtsCondition) {
    String side1Expected;
    if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B1) ||
            mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B2) ||
            mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_C)) {
        subRule = SUB_RULE_24_5;
        rule = DcrConstants.RULE24;
        if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {
            if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {
                if (planDetail.getPlanInformation().getOpeningBelow2mts()) {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                } else {
                    if (planDetail.getPlanInformation().getOpeningAbove2mts()) {

                        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVENTYFIVE_CM);
                        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);

                    } else {

                        if (planDetail.getPlanInformation().getNocToAbutAdjascentSide()) {
                            side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ZERO);
                            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                        } else {
                            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVENTYFIVE_CM);
                            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                        }
                    }
                }

            } else {
                if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0 &&
                        planDetail.getVirtualBuilding().getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                } else {
                    rule = RULE_54;
                    subRule = RULE_54_3_II;
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_TWO);
                }

            }
        } else {
            rule = RULE_54;
            subRule = RULE_54_3_II;
            if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) <= 0) {

                side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
            } else {

                if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_150)) > 0 &&
                        planDetail.getVirtualBuilding().getTotalBuitUpArea()
                                .compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                } else {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_TWO);
                }
            }

        }
        if (max >= side2val.doubleValue())
            valid2 = true;
        if (min >= side1val.doubleValue())
            valid1 = true;

    } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_E) ||
            mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_H)) {
        subRule = SUB_RULE_24_5;
        rule = DcrConstants.RULE24;
        if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) <= 0) {
          if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {
            if (planDetail.getPlanInformation().getOpeningBelow2mts()) {
                side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
            } else {
                if (planDetail.getPlanInformation().getOpeningAbove2mts()) {

                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVENTYFIVE_CM);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);

                } else {

                    if (planDetail.getPlanInformation().getNocToAbutAdjascentSide()) {
                        side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ZERO);
                        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                    } else {
                        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVENTYFIVE_CM);
                        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                    }
                }
            }
          }else
          {
              side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
              side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
          }
        } else {
            rule = RULE_54;
            subRule = RULE_54_3;
            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_TWO);
        }
        if (max >= side2val.doubleValue())
            valid2 = true;
        if (min >= side1val.doubleValue())
            valid1 = true;

    } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D) ||
            mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                 
        if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800))> 0) {
            rule = Rule_55;
            subRule = RULE_55_2_3;
              side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
              side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FIVE);
          
        } else  if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500))>0 &&
                planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800))<=0 ) {
            rule = Rule_55;
            subRule = RULE_55_2_2;
            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FOUR);
        } else  if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300))>0 &&
                planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500))<=0 ) {
            rule = Rule_55;
            subRule = RULE_55_2_1;
            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_TWO);
            
        }else
        {
            rule = Rule_55;
            subRule = RULE_55_2_PROV;
            if( mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)){
                side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
                side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONEPOINTFIVE);
            }else
            {
                side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
            }
            
        }
        if (max >= side2val.doubleValue())
            valid2 = true;
        if (min >= side1val.doubleValue())
            valid1 = true;

    }else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {
        rule = Rule_55;
        subRule = RULE_55_2_1;
      
        if (planDetail.getFloorUnits().isEmpty()) {
            if (planDetail.getPlanInformation().getParkingToMainBuilding()) {
                if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                } else {
                    side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE);
                    side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_ONE_TWO);
                }

            } else {
                side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FIVE);
                side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FIVE);
            }

        } else {//check this .. not explained in clearn in sheet.
            side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FIVE);
            side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_FIVE);

        }
    } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G1) ||
            mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G2)) {
        rule = RULE_57;
        subRule = RULE_57_4;
        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_THREE);
        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_THREE);
    }  else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I1)) {
        rule = RULE_59;
        subRule = RULE_59_3;
        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_THREE);
        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_THREE);
    } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I2)) {
        rule = RULE_59;
        subRule = RULE_59_3;
        side1val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVEN_FIVE);
        side2val = distanceIncrementBasedOnHeight.add(SIDEVALUE_SEVEN_FIVE);
    }
    
        if (checkMinimum5mtsCondition) {
            side1val = side1val.compareTo(FIVE) <= 0 ? FIVE : side1val;
            side2val = side2val.compareTo(FIVE) <= 0 ? FIVE : side2val;
        }
    
    if (max >= side2val.doubleValue())
        valid2 = true;
    if (min >= side1val.doubleValue())
        valid1 = true;
    
    if (valid1) {

        planDetail.reportOutput
                .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                        side1FieldName,
                        side1val.toString(),
                        min + DcrConstants.IN_METER,
                        Result.Accepted, null));
    } else {
        planDetail.reportOutput
                .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                        side1FieldName,
                        side1val.toString(),
                        min + DcrConstants.IN_METER,
                        Result.Not_Accepted, null));

    }
    if (valid2) {

        planDetail.reportOutput
                .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                        side2FieldName,
                        side2val.toString() + DcrConstants.IN_METER,
                        max + DcrConstants.IN_METER,
                        Result.Accepted, null));
    } else {
        planDetail.reportOutput
                .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                        side2FieldName,
                        side2val.toString() + DcrConstants.IN_METER,
                        max + DcrConstants.IN_METER,
                        Result.Not_Accepted,
                        null));

    }
}

private void checkSideYardLessThanTenOrEqualToMts(PlanDetail planDetail, Plot plot, 
            double min, double max, OccupancyType mostRestrictiveOccupancy) {
        String subRule = RULE_24_3;
        String rule=DcrConstants.RULE24;
        String side1Desc = SIDE_YARD_1_DESC;
        String side2Desc = SIDE_YARD_2_DESC;
        String side1FieldName = DcrConstants.SIDE_YARD1_DESC;
        String side2FieldName = DcrConstants.SIDE_YARD2_DESC;
    
        Boolean valid1 = false;
        Boolean valid2 = false;
        BigDecimal buildingHeight = planDetail.getVirtualBuilding().getBuildingHeight();
        String side1Expected = "";
        BigDecimal side1val = SIDEVALUE_ONE;
        BigDecimal side2val = SIDEVALUE_ONE_TWO;

        
        if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {
            // Plot area less than or equal to 125
            if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {

                rule = DcrConstants.RULE62;
                subRule = RULE_62_1_A;
                side1val = SIDEVALUE_ONE;
                side2val = SIDEVALUE_ONE_TWO;

                if (planDetail.getVirtualBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(3)) <= 0) {
                    // yes

                    if (planDetail.getPlanInformation().getOpeningOnSide()) {
                        side1val = SIDEVALUE_SIXTY_CM;
                        side2val = SIDEVALUE_NINTY_CM;
                    } else {
                        if (planDetail.getPlanInformation().getNocToAbutSide()) {
                            side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                            side1val = SIDEVALUE_ZERO;
                            side2val = SIDEVALUE_NINTY_CM;

                        } else {
                            side1val = SIDEVALUE_SIXTY_CM;
                            side2val = SIDEVALUE_NINTY_CM;

                        }
                    }
                } else {
                    // no
                    if (planDetail.getPlanInformation().getOpeningBelow2mts()) {
                        side1val = SIDEVALUE_ONE;
                        side2val = SIDEVALUE_ONE_TWO;

                    } else {
                        if (planDetail.getPlanInformation().getOpeningAbove2mts()) {
                            side1val = SIDEVALUE_SEVENTYFIVE_CM;
                            side2val = SIDEVALUE_ONE_TWO;

                        } else {

                            if (planDetail.getPlanInformation().getNocToAbutAdjascentSide()) {
                                side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                                side1val = SIDEVALUE_ZERO;
                                side2val = SIDEVALUE_ONE_TWO;
                            } else {
                                side1val = SIDEVALUE_SEVENTYFIVE_CM;
                                side2val = SIDEVALUE_ONE_TWO;

                            }
                        }
                    }

                }

            } else {
                subRule = SUB_RULE_24_3;
                rule = DcrConstants.RULE24;
                // Plot area greater than 125 mts

                if (buildingHeight.compareTo(BigDecimal.valueOf(7)) <= 0) {

                    if (planDetail.getPlanInformation().getOpeningBelow2mts()) {
                        side1val = SIDEVALUE_ONE;
                        side2val = SIDEVALUE_ONE_TWO;
                    } else {
                        if (planDetail.getPlanInformation().getOpeningAbove2mts()) {

                            side1val = SIDEVALUE_SEVENTYFIVE_CM;
                            side2val = SIDEVALUE_ONE_TWO;

                        } else {

                            if (planDetail.getPlanInformation().getNocToAbutAdjascentSide()) {
                                side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                                side1val = SIDEVALUE_ZERO;
                                side2val = SIDEVALUE_ONE_TWO;
                            } else {
                                side1val = SIDEVALUE_SEVENTYFIVE_CM;
                                side2val = SIDEVALUE_ONE_TWO;

                            }

                        }
                    }

                } else {
                    side1Expected = SIDE_YARD_1_EXPECTED;
                    side1val = SIDEVALUE_ONE;
                    side2val = SIDEVALUE_ONE_TWO;
                }

            }

            if (max >= side2val.doubleValue())
                valid2 = true;
            if (min >= side1val.doubleValue())
                valid1 = true;

           
       
        if (max >= side2val.doubleValue())
            valid2 = true;
        if (min >= side1val.doubleValue())
            valid1 = true;
        
        if (valid1) {

            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                            side1FieldName,
                            side1val.toString(),
                            min + DcrConstants.IN_METER,
                            Result.Accepted, null));
        } else {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule + side1FieldName, side1Desc,
                            side1FieldName,
                            side1val.toString(),
                            min + DcrConstants.IN_METER,
                            Result.Not_Accepted, null));

        }
        if (valid2) {

            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                            side2FieldName,
                            side2val.toString() + DcrConstants.IN_METER,
                            max + DcrConstants.IN_METER,
                            Result.Accepted, null));
        } else {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule + side2FieldName, side2Desc,
                            side2FieldName,
                            side2val.toString() + DcrConstants.IN_METER,
                            max + DcrConstants.IN_METER,
                            Result.Not_Accepted,
                            null));

        }
        }else
        {
            processSideYardOtherThanResidentialCases(planDetail, plot, min, max, mostRestrictiveOccupancy, subRule, rule,
                    side1Desc, side2Desc, side1FieldName, side2FieldName, valid1, valid2, buildingHeight, side1val, side2val,BigDecimal.ZERO,false);

        }
    }

public void rule24_3(PlanDetail planDetail, String type) {
        
        if (planDetail.getPlot() == null)
            return;

        validateRule24_3(planDetail);

        // MOVE BASEMENT LOGIC TO SEPARATE METHOD.
        Plot plot = planDetail.getPlot();
        String frontYardFieldName = DcrConstants.FRONT_YARD_DESC;
        String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;

        Boolean valid = false;
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal mean = BigDecimal.ZERO;
        SetBack setback = null;
        if (plot != null && !planDetail.getPlot().getSetBacks().isEmpty()) {
            setback = planDetail.getPlot().getLevelZeroSetBack();
            if (setback != null && setback.getFrontYard() != null) {
                min = setback.getFrontYard().getMinimumDistance();
                mean = setback.getFrontYard().getMean();
            }

        }
        BigDecimal buildingHeight = planDetail.getVirtualBuilding().getBuildingHeight();
        if (setback == null || planDetail.getBuilding() == null || plot.getArea() == null || (planDetail.getBlocks().isEmpty()))
            return;
        
   //     Building building = planDetail.getBlocks().get(0).getBuilding(); //multiple block case, this assumption wrong.
   //     OccupancyType mostRestrictiveOccupancy=planDetail.getVirtualBuilding().getMostRestrictiveOccupancy();
      for(OccupancyType  mostRestrictiveOccupancy : planDetail.getVirtualBuilding().getOccupancies() )  {
            if (buildingHeight != null) {
                if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0) {
                    valid = checkFrontYardLessThanTenMts(planDetail, plot, frontYardFieldName, valid, min, mean,
                            mostRestrictiveOccupancy);

                } else if (buildingHeight.compareTo(BigDecimal.valueOf(10))> 0
                        && buildingHeight.compareTo(BigDecimal.valueOf(16)) <= 0) {
                    valid = checkFrontYardBetweenTenToSixteenMts(planDetail, plot, frontYardFieldName, subRuleDesc, min, mean,
                            mostRestrictiveOccupancy);

                } else if (buildingHeight.compareTo(BigDecimal.valueOf(16)) > 0) {
                    valid = checkFrontYardMoreThanSixteenMts(planDetail, plot, frontYardFieldName, valid, min, mean,
                            mostRestrictiveOccupancy);

                }

            }
    
      }
    }


 private Boolean checkFrontYardMoreThanSixteenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName, Boolean valid,
         BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy) {
     String subRule = RULE_62_1_A;
     String rule = RULE_62;
     String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;

     BigDecimal minval = BigDecimal.valueOf(1.2);
     BigDecimal meanval = BigDecimal.valueOf(1.8);
     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {

         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
              subRule = SUB_RULE_24_3;
              rule = DcrConstants.RULE24;
             minval = BigDecimal.valueOf(1.8);
             meanval = BigDecimal.valueOf(3);
         }

         for (SetBack setbacks : planDetail.getPlot().getSetBacks()) {
              if (setbacks.getHeight() != null && setbacks.getHeight().compareTo(BigDecimal.TEN) >= 0) {
                  //Using height defined in levels
                 BigDecimal minValue = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))))
                                         .add(minval);
                 BigDecimal meanValue = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))))
                                         .add(meanval);
                 minValue = minValue.compareTo(FIVE) <= 0 ? FIVE : minValue;
                 meanValue = meanValue.compareTo(FIVE) <= 0 ? FIVE : meanValue;
//compare with each setback frontyard, level wise minimum and mean distaince.
                 if (setbacks.getFrontYard().getMinimumDistance().compareTo(minValue) >= 0
                         && setbacks.getFrontYard().getMean().compareTo(meanValue) >= 0) {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + setbacks.getFrontYard().getMinimumDistance() + "," + setbacks.getFrontYard().getMean() + ")" + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + setbacks.getFrontYard().getMinimumDistance() + "," + setbacks.getFrontYard().getMean() + ")" + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));
             }

         }
     } else {

         BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                 .multiply(BigDecimal
                         .valueOf(Math.ceil((planDetail.getVirtualBuilding().getBuildingHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));


         valid = processFrontYardOtherThanResidentials(planDetail, plot, frontYardFieldName, subRuleDesc, min, mean,
                mostRestrictiveOccupancy, subRule, rule, valid, minval, meanval, distanceIncrementBasedOnHeight,true);

     }
     return valid;
 }

 private Boolean checkFrontYardLessThanTenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName, Boolean valid,
         BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy) {
     String subRule = SUB_RULE_24_3;
     String rule = DcrConstants.RULE24;
     String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;
     BigDecimal minval = BigDecimal.valueOf(0);
     BigDecimal meanval = BigDecimal.valueOf(0);
     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {
         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
             rule = RULE_62;
             subRule = RULE_62_1_A;
             minval = FRONTYARDMINIMUM_DISTANCE_1_2;
             meanval = FRONTYARDMEAN_DISTANCE_1_8;
           
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         } else {
             rule = DcrConstants.RULE24;
             subRule = SUB_RULE_24_3;
             minval = FRONTYARDMINIMUM_DISTANCE_1_8;
             meanval = FRONTYARDMEAN_DISTANCE_3;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         }
         if (valid) {
             planDetail.reportOutput
                     .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, frontYardFieldName,
                             meanMinumumLabel + "(" + minval + "," + meanval + ")" + DcrConstants.IN_METER,
                             "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                             Result.Accepted, null));
         } else
             planDetail.reportOutput
                     .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, frontYardFieldName,
                             meanMinumumLabel + "(" + minval + "," + meanval + ")" + DcrConstants.IN_METER,
                             "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                             Result.Not_Accepted, null));

     } else {
         valid = processFrontYardOtherThanResidentials(planDetail, plot, frontYardFieldName, subRuleDesc, min, mean,
                 mostRestrictiveOccupancy, subRule, rule, valid, minval, meanval, BigDecimal.ZERO,false);

     }
     return valid;
 }

 private Boolean checkFrontYardBetweenTenToSixteenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName,
         String subRuleDesc, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy) {
     String subRule = SUB_RULE_24_3;
     String rule = DcrConstants.RULE24;
     
     Boolean valid = false;

     BigDecimal minval = BigDecimal.valueOf(1.8);
     BigDecimal meanval = BigDecimal.valueOf(3);

     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {

         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
             subRule = SUB_RULE_24_3;
             rule = DcrConstants.RULE24;
             minval = BigDecimal.valueOf(1.8);
             meanval = BigDecimal.valueOf(3);
         }else
         {
             rule = RULE_62;
             subRule = RULE_62_1_A;
             minval = BigDecimal.valueOf(1.2);
             meanval = BigDecimal.valueOf(1.8);
         }

         for (SetBack setbacks : planDetail.getPlot().getSetBacks()) {
             if (setbacks.getHeight() != null && setbacks.getHeight().compareTo(BigDecimal.TEN) >= 0) {
                 rule = RULE_24;
                 subRule = RULE_24_3;
                 BigDecimal minValue = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))))
                                         .add(minval);

                 BigDecimal meanValue = (BigDecimal.valueOf(VALUE_0_5)
                         .multiply(BigDecimal.valueOf(Math.ceil((setbacks.getHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))))
                                         .add(meanval);
//compare with each setback frontyard, level wise minimum and mean distaince.
                 if (setbacks.getFrontYard().getMinimumDistance().compareTo(minValue) >= 0
                         && setbacks.getFrontYard().getMean().compareTo(meanValue) >= 0) {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + setbacks.getFrontYard().getMinimumDistance() + "," + setbacks.getFrontYard().getMean() + ")" + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + setbacks.getFrontYard().getMinimumDistance() + "," + setbacks.getFrontYard().getMean() + ")" + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));
             }

         }
     } else {

         BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                 .multiply(BigDecimal
                         .valueOf(Math.ceil((planDetail.getVirtualBuilding().getBuildingHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));

         valid = processFrontYardOtherThanResidentials(planDetail, plot, frontYardFieldName, subRuleDesc, min, mean,
                mostRestrictiveOccupancy, subRule, rule, valid, minval, meanval, distanceIncrementBasedOnHeight,false);

     }
     return valid;
 }

private Boolean processFrontYardOtherThanResidentials(PlanDetail planDetail, Plot plot, String frontYardFieldName,
        String subRuleDesc, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy, String subRule, String rule,
            Boolean valid, BigDecimal minval, BigDecimal meanval, BigDecimal distanceIncrementBasedOnHeight,
            Boolean checkMinimum5mtsCondition) {
        if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B1) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B2) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_C) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_E) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_H)) {

            if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) > 0) {
                rule = RULE_54;
                subRule = RULE_54_3_I;
                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
            } else {
                rule = DcrConstants.RULE24;
                subRule = SUB_RULE_24_3;
                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
            }

        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D) ||
                mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
            if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
                rule = Rule_55;
                subRule = RULE_55_2_3;
                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_6);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_10_5);

            } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                    planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
                rule = Rule_55;
                subRule = RULE_55_2_2;
                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
            } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                    planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
                rule = Rule_55;
                subRule = RULE_55_2_1;

                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
            } else {
                if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                    rule = Rule_55;
                    subRule = RULE_55_2_PROV;
                    minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                    meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                } else {
                    rule = Rule_55;
                    subRule = RULE_55_2_PROV;
                    minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                    meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);

                }

            }
        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {
            if (planDetail.getFloorUnits().isEmpty()) {
                if (planDetail.getPlanInformation().getParkingToMainBuilding()) {
                    if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                        rule = RULE_56;
                        subRule = RULE563D;
                        minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_2);
                        meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_1_8);
                    } else {
                        rule = RULE_56;
                        subRule = RULE563D;
                        minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                        meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                    }

                } else {
                    rule = RULE_56;
                    subRule = RULE563D;
                    minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                    meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
                }

            } else {
                rule = RULE_24;
                subRule = RULE_24_3;
                minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);

            }
        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G1)) {
            rule = RULE_57;
            subRule = RULE_57_4;
            minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
            meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G2)) {
            rule = RULE_57;
            subRule = RULE_57_4;
            minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
            meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I1)) {
            rule = RULE_59;
            subRule = RULE_59_3;
            minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
            meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
        } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I2)) {
            rule = RULE_59;
            subRule = RULE_59_3;
            minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_7_5);
            meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
        }

        if (checkMinimum5mtsCondition) {
            minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
            meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
        }

        valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

        if (valid) {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, frontYardFieldName,
                            meanMinumumLabel + "(" + minval + "," + meanval + ")" + DcrConstants.IN_METER,
                            "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                            Result.Accepted, null));
        } else
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc, frontYardFieldName,
                            meanMinumumLabel + "(" + minval + "," + meanval + ")" + DcrConstants.IN_METER,
                            "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                            Result.Not_Accepted, null));
        return valid;
    }

 private Boolean validateMinimumAndMeanValue(Boolean valid, BigDecimal min, BigDecimal mean, BigDecimal minval,
         BigDecimal meanval) {
     if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0) {
         valid = true;
     }
     return valid;
 }

 private void validateRule24_5(PlanDetail planDetail) {
    SetBack setback=planDetail.getPlot().getLevelZeroSetBack(); 
     if (planDetail.getPlot() != null && (setback==null ||
             (setback.getSideYard1()==null || !setback.getSideYard1().getPresentInDxf() &&
                     setback.getSideYard2()==null ||  !setback.getSideYard2().getPresentInDxf() ))) {

         errors.put(DcrConstants.SIDE_YARD_DESC,
                 prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD_DESC));
         planDetail.addErrors(errors);
     }
     
 }
 private void validateRule24_3(PlanDetail planDetail) {
     SetBack setback=planDetail.getPlot().getLevelZeroSetBack(); 
     if (planDetail.getPlot() != null && (setback==null ||
            (setback.getFrontYard()==null || !setback.getFrontYard().getPresentInDxf()))) {

         errors.put(DcrConstants.FRONT_YARD_DESC,
                 prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FRONT_YARD_DESC));
         planDetail.addErrors(errors);
     }
     if (planDetail.getBasement() != null &&
         planDetail.getPlot() != null && (planDetail.getPlot().getBsmtFrontYard() == null ||
                 !planDetail.getPlot().getBsmtFrontYard().getPresentInDxf())) {
             errors.put(DcrConstants.BSMT_FRONT_YARD_DESC,
                     prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BSMT_FRONT_YARD_DESC));
             planDetail.addErrors(errors);
         
     }

 }
 private String prepareMessage(String code, String args) {
     return edcrMessageSource.getMessage(code,
             new String[] { args }, LocaleContextHolder.getLocale());
 }
}
