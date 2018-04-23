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
    private static final int SITEAREA_125 = 125;
    private static final int BUILDUPAREA_300 = 300;
    private static final int FLOORAREA_800 = 800;
    private static final int FLOORAREA_500 = 500;
    private static final int FLOORAREA_300 =300;

    private static HashMap<String, String> errors = new HashMap<>();

    private Logger logger = Logger.getLogger(SetBackService.class);
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
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
                        pl.getPlot().setFrontYard(yard);
                    }
            
                    yard = getYard(pl, doc, DxfFileConstants.REAR_YARD+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.REAR_YARD+ "_L_"+ yardLevel));
                        setBack.setRearYard(yard);
                        pl.getPlot().setRearYard(yard);
                    }
            
                    yard = getYard(pl, doc, DxfFileConstants.SIDE_YARD_1+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_1+ "_L_"+ yardLevel));
                        setBack.setSideYard1(yard);
                        pl.getPlot().setSideYard1(yard);
                    }
                    yard = getYard(pl, doc, DxfFileConstants.SIDE_YARD_2+ "_L_"+ yardLevel);
                    if (yard != null) {
                        yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_2+ "_L_"+ yardLevel));
            
                        setBack.setSideYard2(yard);
                        pl.getPlot().setSideYard2(yard);
                    }
                    yardLevel++;
                    pl.getPlot().getSetBacks().add(setBack);

        }

          
     
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
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {
            if (plot != null && plot.getBsmtFrontYard() != null &&
                    plot.getBsmtFrontYard().getPresentInDxf()
                    && plot.getBsmtFrontYard().getMinimumDistance() != null
                    && plot.getBsmtFrontYard().getMean() != null) {

                min = plot.getBsmtFrontYard().getMinimumDistance();
                mean = plot.getBsmtFrontYard().getMean();
                frontYardFieldName = DcrConstants.BSMT_FRONT_YARD_DESC;
            }
        } else if (plot != null && !planDetail.getPlot().getSetBacks().isEmpty()) {
            setback = planDetail.getPlot().getGrondLevelSetBack();
            if (setback != null && setback.getFrontYard() != null) {
                min = setback.getFrontYard().getMinimumDistance();
                mean = setback.getFrontYard().getMean();
            }

        }

        if (setback == null || planDetail.getBuilding() == null || plot.getArea() == null || (planDetail.getBlocks().isEmpty()))
            return;
        
   //     Building building = planDetail.getBlocks().get(0).getBuilding(); //multiple block case, this assumption wrong.
   //     OccupancyType mostRestrictiveOccupancy=planDetail.getVirtualBuilding().getMostRestrictiveOccupancy();
      for(OccupancyType  mostRestrictiveOccupancy : planDetail.getVirtualBuilding().getOccupancies() )  
            if (planDetail.getVirtualBuilding().getBuildingHeight() != null) {
                if (planDetail.getVirtualBuilding().getBuildingHeight().intValue() <= 10) {
                    valid = checkSetBackLessThanTenMts(planDetail, plot, frontYardFieldName, valid, min, mean,
                            mostRestrictiveOccupancy);

                } else if (planDetail.getVirtualBuilding().getBuildingHeight().intValue() > 10
                        && planDetail.getVirtualBuilding().getBuildingHeight().intValue() <= 16) {
                    valid = checkSetBackBetweenTenToSixteenMts(planDetail, plot, frontYardFieldName, subRuleDesc, min, mean,
                            mostRestrictiveOccupancy);

                } else if (planDetail.getVirtualBuilding().getBuildingHeight().intValue() > 16) {
                    valid = checkSetBackMoreThanSixteenMts(planDetail, plot, frontYardFieldName, valid, min, mean,
                            mostRestrictiveOccupancy);

                }

            }
    
    
    }


 private Boolean checkSetBackMoreThanSixteenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName, Boolean valid,
         BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy) {
     String subRule = SUB_RULE_24_3;
     String rule = DcrConstants.RULE24;
     String subRuleDesc = SUB_RULE_24_3_DESCRIPTION;

     BigDecimal minval = BigDecimal.valueOf(1.2);
     BigDecimal meanval = BigDecimal.valueOf(1.8);
     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {

         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
             rule = RULE_62;
             subRule = RULE_62_1_A;
             minval = BigDecimal.valueOf(1.8);
             meanval = BigDecimal.valueOf(3);
         }

         for (SetBack setbacks : planDetail.getPlot().getSetBacks()) {
             if (setbacks.getHeight() != null && setbacks.getHeight().compareTo(BigDecimal.TEN) >= 0) {
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

                 if (min.compareTo(minValue) >= 0
                         && mean.compareTo(meanValue) >= 0) {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));
             }

         }
     } else {

         BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                 .multiply(BigDecimal
                         .valueOf(Math.ceil((planDetail.getVirtualBuilding().getBuildingHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));

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

                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             } else {
                 rule = DcrConstants.RULE24;
                 subRule = SUB_RULE_24_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             }

         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D) ||
                 mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
             if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_6);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_10_5);
                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                     planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_2;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                     planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_1;

                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             } else {
                 if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                     rule = Rule_55;
                     subRule = RULE_55_2_PROV;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                     minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                     meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                 } else {
                     rule = Rule_55;
                     subRule = RULE_55_2_PROV;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                     minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                     meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

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
                         minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                         meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                     } else {
                         rule = RULE_56;
                         subRule = RULE563D;
                         minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                         meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                         minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                         meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                     }

                 } else {
                     rule = RULE_56;
                     subRule = RULE563D;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
                     minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                     meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

                 }

             } else {
                 rule = RULE_24;
                 subRule = RULE_24_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                 minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
                 meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             }
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G1)) {
             rule = RULE_57;
             subRule = RULE_57_4;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
             minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
             meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G2)) {
             rule = RULE_57;
             subRule = RULE_57_4;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
             minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
             meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I1)) {
             rule = RULE_59;
             subRule = RULE_59_3;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
             minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
             meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I2)) {
             rule = RULE_59;
             subRule = RULE_59_3;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_7_5);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
             minval = minval.compareTo(FIVE) <= 0 ? FIVE : minval;
             meanval = meanval.compareTo(FIVE) <= 0 ? FIVE : meanval;
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

     }
     return valid;
 }

 private Boolean checkSetBackLessThanTenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName, Boolean valid,
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
             minval = FRONTYARDMINIMUM_DISTANCE_1_8;
             meanval = FRONTYARDMEAN_DISTANCE_3;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         } else {
             rule = DcrConstants.RULE24;
             subRule = SUB_RULE_24_3;
             minval = FRONTYARDMINIMUM_DISTANCE_1_2;
             meanval = FRONTYARDMEAN_DISTANCE_1_8;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         }

     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_B2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_C) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_E) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_H)) {
         if (planDetail.getVirtualBuilding().getTotalBuitUpArea().compareTo(BigDecimal.valueOf(BUILDUPAREA_300)) > 0) {
             rule = RULE_54;
             subRule = RULE_54_3_I;
             minval = FRONTYARDMINIMUM_DISTANCE_4_5;
             meanval = FRONTYARDMEAN_DISTANCE_6;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         } else {
             rule = DcrConstants.RULE24;
             subRule = SUB_RULE_24_3;
             minval = FRONTYARDMINIMUM_DISTANCE_1_8;
             meanval = FRONTYARDMEAN_DISTANCE_3;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         }

     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
         if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
             rule = Rule_55;
             subRule = RULE_55_2_3;
             minval = FRONTYARDMINIMUM_DISTANCE_6;
             meanval = FRONTYARDMEAN_DISTANCE_10_5;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                 planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
             rule = Rule_55;
             subRule = RULE_55_2_2;
             minval = FRONTYARDMINIMUM_DISTANCE_5;
             meanval = FRONTYARDMEAN_DISTANCE_7_5;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                 planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
             rule = Rule_55;
             subRule = RULE_55_2_1;
             minval = FRONTYARDMINIMUM_DISTANCE_4_5;
             meanval = FRONTYARDMEAN_DISTANCE_6;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else {
             if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                 rule = Rule_55;
                 subRule = RULE_55_2_PROV;

                 minval = FRONTYARDMINIMUM_DISTANCE_3;
                 meanval = FRONTYARDMEAN_DISTANCE_3;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             } else {
                 rule = Rule_55;
                 subRule = RULE_55_2_PROV;
                 minval = FRONTYARDMINIMUM_DISTANCE_1_8;
                 meanval = FRONTYARDMEAN_DISTANCE_3;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             }

         }

     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {
         if (planDetail.getFloorUnits().isEmpty()) {
             if (planDetail.getPlanInformation().getParkingToMainBuilding()) {
                 if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) <= 0) {
                     rule = RULE_56;
                     subRule = RULE563D;
                     minval = FRONTYARDMINIMUM_DISTANCE_1_2;
                     meanval = FRONTYARDMEAN_DISTANCE_1_8;
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                 } else {
                     rule = RULE_56;
                     subRule = RULE563D;
                     minval = FRONTYARDMINIMUM_DISTANCE_3;
                     meanval = FRONTYARDMEAN_DISTANCE_3;
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                 }

             } else {
                 rule = RULE_56;
                 subRule = RULE563D;
                 minval = FRONTYARDMINIMUM_DISTANCE_5;
                 meanval = FRONTYARDMEAN_DISTANCE_5;
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             }

         } else {
             rule = RULE_24;
             subRule = RULE_24_3;
             minval = FRONTYARDMINIMUM_DISTANCE_1_8;
             meanval = FRONTYARDMEAN_DISTANCE_3;
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

         }
     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G1)) {
         rule = RULE_57;
         subRule = RULE_57_4;
         minval = FRONTYARDMINIMUM_DISTANCE_5;
         meanval = FRONTYARDMEAN_DISTANCE_5;
         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G2)) {
         rule = RULE_57;
         subRule = RULE_57_4;
         minval = FRONTYARDMINIMUM_DISTANCE_3;
         meanval = FRONTYARDMEAN_DISTANCE_3;
         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I1)) {
         rule = RULE_59;
         subRule = RULE_59_3;
         minval = FRONTYARDMINIMUM_DISTANCE_3;
         meanval = FRONTYARDMEAN_DISTANCE_3;
         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
     } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I2)) {
         rule = RULE_59;
         subRule = RULE_59_3;
         minval = FRONTYARDMINIMUM_DISTANCE_7_5;
         meanval = FRONTYARDMEAN_DISTANCE_7_5;
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
     return valid;
 }

 private Boolean checkSetBackBetweenTenToSixteenMts(PlanDetail planDetail, Plot plot, String frontYardFieldName,
         String subRuleDesc, BigDecimal min, BigDecimal mean, OccupancyType mostRestrictiveOccupancy) {
     String subRule = SUB_RULE_24_3;
     String rule = DcrConstants.RULE24;
     Boolean valid = false;

     BigDecimal minval = BigDecimal.valueOf(1.2);
     BigDecimal meanval = BigDecimal.valueOf(1.8);

     if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A1) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_A2) ||
             mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_F)) {

         if (plot.getArea().compareTo(BigDecimal.valueOf(SITEAREA_125)) > 0) {
             minval = BigDecimal.valueOf(1.8);
             meanval = BigDecimal.valueOf(3);
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

                 if (min.compareTo(minValue) >= 0
                         && mean.compareTo(meanValue) >= 0) {
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                                     Result.Accepted, null));
                 } else
                     planDetail.reportOutput
                             .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                     frontYardFieldName + LEVEL + setbacks.getLevel(),
                                     meanMinumumLabel + "(" + minValue + "," + meanValue + ")" + DcrConstants.IN_METER,
                                     "(" + min + "," + mean + ")" + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));
             }

         }
     } else {

         BigDecimal distanceIncrementBasedOnHeight = (BigDecimal.valueOf(VALUE_0_5)
                 .multiply(BigDecimal
                         .valueOf(Math.ceil((planDetail.getVirtualBuilding().getBuildingHeight().subtract(BigDecimal.TEN)
                                 .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP)).doubleValue()))));

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
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             } else {
                 rule = DcrConstants.RULE24;
                 subRule = SUB_RULE_24_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             }

         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D) ||
                 mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
             if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) > 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_6);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_10_5);
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) > 0 &&
                     planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_800)) <= 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_2;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             } else if (planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_300)) > 0 &&
                     planDetail.getVirtualBuilding().getTotalFloorArea().compareTo(BigDecimal.valueOf(FLOORAREA_500)) <= 0) {
                 rule = Rule_55;
                 subRule = RULE_55_2_1;

                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_4_5);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_6);
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
             } else {
                 if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_D1)) {
                     rule = Rule_55;
                     subRule = RULE_55_2_PROV;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                 } else {
                     rule = Rule_55;
                     subRule = RULE_55_2_PROV;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

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
                         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                     } else {
                         rule = RULE_56;
                         subRule = RULE563D;
                         minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
                         meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                         valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
                     }

                 } else {
                     rule = RULE_56;
                     subRule = RULE563D;
                     minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
                     meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
                     valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

                 }

             } else {
                 rule = RULE_24;
                 subRule = RULE_24_3;
                 minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_1_8);
                 meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
                 valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);

             }
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G1)) {
             rule = RULE_57;
             subRule = RULE_57_4;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_5);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_5);
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_G2)) {
             rule = RULE_57;
             subRule = RULE_57_4;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I1)) {
             rule = RULE_59;
             subRule = RULE_59_3;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_3);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_3);
             valid = validateMinimumAndMeanValue(valid, min, mean, minval, meanval);
         } else if (mostRestrictiveOccupancy.equals(OccupancyType.OCCUPANCY_I2)) {
             rule = RULE_59;
             subRule = RULE_59_3;
             minval = distanceIncrementBasedOnHeight.add(FRONTYARDMINIMUM_DISTANCE_7_5);
             meanval = distanceIncrementBasedOnHeight.add(FRONTYARDMEAN_DISTANCE_7_5);
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

     }
     return valid;
 }

 private Boolean validateMinimumAndMeanValue(Boolean valid, BigDecimal min, BigDecimal mean, BigDecimal minval,
         BigDecimal meanval) {
     if (min.compareTo(minval) >= 0 && mean.compareTo(meanval) >= 0) {
         valid = true;
     }
     return valid;
 }

 private void validateRule24_3(PlanDetail planDetail) {
     if (planDetail.getPlot() != null && (planDetail.getPlot().getGrondLevelSetBack()==null ||
             !planDetail.getPlot().getGrondLevelSetBack().getFrontYard().getPresentInDxf())) {

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
