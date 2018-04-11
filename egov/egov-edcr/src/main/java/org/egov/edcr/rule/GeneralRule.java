package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.service.DcrService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.helpers.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.domain.builders.FastReportBuilder;

@Service
public class GeneralRule implements RuleService {

    private Logger LOG = Logger.getLogger(DcrService.class);
    
    protected final Ray RAY_CASTING = new Ray(new Point(-1.123456789, -1.987654321, 0d));
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;

    
    
    public PlanDetail validate(PlanDetail planDetail) {
       
        
        
        return planDetail;
    }

    public PlanDetail process(PlanDetail planDetail) {
        return planDetail;

    }
    
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb, Map map, boolean status) {
        return true;
    }

    protected RuleOutput buildRuleOutputWithSubRule(String mainRule, String subRule, String ruleDescription, String fieldVerified,
            String expectedResult,
            String actualResult, Result status, String message) {
        RuleOutput ruleOutput = new RuleOutput();

        if (mainRule != null) {
            ruleOutput.key = mainRule;
            // ruleOutput.result = status;

            if (subRule != null || fieldVerified != null) {
                SubRuleOutput subRuleOutput = new SubRuleOutput();
                subRuleOutput.key = subRule != null ? subRule : fieldVerified;
                subRuleOutput.result = status;
                subRuleOutput.message = message;
                subRuleOutput.ruleDescription = ruleDescription;

                if (expectedResult != null) {
                    RuleReportOutput ruleReportOutput = new RuleReportOutput();
                    ruleReportOutput.setActualResult(actualResult);
                    ruleReportOutput.setExpectedResult(expectedResult);
                    ruleReportOutput.setFieldVerified(fieldVerified);
                    ruleReportOutput.setStatus(status.toString());
                    subRuleOutput.add(ruleReportOutput);
                }
                ruleOutput.subRuleOutputs.add(subRuleOutput);
            }
        }

        return ruleOutput;
    }

    protected RuleOutput buildRuleOutputWithMainRule(String mainRule, String ruleDescription, Result status, String message) {
        RuleOutput ruleOutput = new RuleOutput();
        ruleOutput.key = mainRule;
        ruleOutput.result = status;
        ruleOutput.setMessage(message);
        ruleOutput.ruleDescription = ruleDescription;

        return ruleOutput;
    }

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
          pl.setPlanInformation(extractPlanInfo(pl,doc));
          extractPlotDetails(pl, doc);
          extractBuildingDetails(pl, doc);
          extractTotalFloorArea(doc, pl);
          return pl;
          
    
    }
    
    
    private void extractPlotDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;
        // Plot plot = new Plot();
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.PLOT_BOUNDARY);
        if (polyLinesByLayer.size() > 0) {
            pl.getPlot().setPolyLine(polyLinesByLayer.get(0));
            // plot.setArea(Util.getPolyLineArea(plot.getPolyLine())); //The actual area of plot boundary not used. Plot area
            // decide by plan information table.
            // plot.setPresentInDxf(true);
        } else
            pl.addError(DxfFileConstants.PLOT_BOUNDARY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.PLOT_BOUNDARY }, null));
        // pl.setPlot(plot);
    }
    
    
    private void extractBuildingDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;
        Building building = new Building();
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.BUILDING_FOOT_PRINT);

        if (polyLinesByLayer.size() > 0) {
            building.setPolyLine(polyLinesByLayer.get(0));
            building.setPresentInDxf(true);
        } else
            pl.addError(DxfFileConstants.BUILDING_FOOT_PRINT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.BUILDING_FOOT_PRINT }, null));

        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.SHADE_OVERHANG);

        Measurement shade = new Measurement();
        if (polyLinesByLayer.size() > 0) {
            shade.setPolyLine(polyLinesByLayer.get(0));
        }
        building.setShade(shade);

        extractOpenStairs(doc, building);

        pl.setBuilding(building);

    }
    
    private void extractOpenStairs(DXFDocument doc, Building building) {
        List<DXFDimension> lines = Util.getDimensionsByLayer(doc, DxfFileConstants.OPEN_STAIR);
        if (lines != null)
            for (Object dxfEntity : lines) {
                BigDecimal value = BigDecimal.ZERO;
                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();
                        if (text2.contains(";")) {
                            text2 = text2.split(";")[1];
                        } else

                            text2 = text2.replaceAll("[^\\d.]", "");
                        ;
                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            Measurement openPlot = new Measurement();
                            openPlot
                                    .setMinimumDistance(value);
                            building.getOpenStairs().add(openPlot);
                        }

                    }
                }

            }
    }


    
        private PlanInformation extractPlanInfo( PlanDetail pl,DXFDocument doc) {
            PlanInformation pi = new PlanInformation();
            Map<String, String> planInfoProperties = Util.getPlanInfoProperties(doc);
            if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
                pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));
            String plotArea = planInfoProperties.get(DxfFileConstants.PLOT_AREA);

            if (plotArea == null){
                Plot plot = new Plot();
                pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " is not defined in the Plan Information Layer");
                plot.setPresentInDxf(false);
                pl.setPlot(plot);
            }
            else
                try {
                    Plot plot = new Plot();
                    plotArea = plotArea.replaceAll("[^\\d.]", "");
                    pi.setPlotArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
                    plot.setArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
                    plot.setPresentInDxf(true);
                    pl.setPlot(plot);
                } catch (Exception e) {
                    pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " contains non invalid values.");
                }
            if (planInfoProperties.get(DxfFileConstants.CRZ_ZONE) != null) {
                String value = planInfoProperties.get(DxfFileConstants.CRZ_ZONE);
                if (value.equalsIgnoreCase(DcrConstants.YES))
                    pi.setCrzZoneArea(true);
                else
                    pi.setCrzZoneArea(false);
            }

            if (planInfoProperties.get(DxfFileConstants.SECURITY_ZONE) != null) {
                String securityZone = planInfoProperties.get(DxfFileConstants.SECURITY_ZONE);
                if (securityZone.equalsIgnoreCase(DcrConstants.YES))
                    pi.setSecurityZone(true);
                else
                    pi.setSecurityZone(false);
            }
            if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M) != null) {
                String openingBelow2mside = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M);
                if (openingBelow2mside.equalsIgnoreCase(DcrConstants.YES))
                    pi.setOpeningOnSide(true);
                else
                    pi.setOpeningOnSide(false);
            }
            if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M) != null) {
                String openingBelow2mrear = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M);
                if (openingBelow2mrear.equalsIgnoreCase(DcrConstants.YES))
                    pi.setOpeningOnRear(true);
                else
                    pi.setOpeningOnRear(false);
            }
            if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE) != null) {
                String nocAbutSide = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE);
                if (nocAbutSide.equalsIgnoreCase(DcrConstants.YES))
                    pi.setNocToAbutSide(true);
                else
                    pi.setNocToAbutSide(false);
            }
            if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR) != null) {
                String nocAbutRear = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR);
                if (nocAbutRear.equalsIgnoreCase(DcrConstants.YES))
                    pi.setNocToAbutRear(true);
                else
                    pi.setNocToAbutRear(false);
            }

            if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
                pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));

            String accwidth = "";
            if (planInfoProperties.size() > 0) {
                String accessWidth = planInfoProperties.get(DxfFileConstants.ACCESS_WIDTH);
                accwidth = accessWidth;
                if (accessWidth == null) {

                    Set<String> keySet = planInfoProperties.keySet();
                    for (String s : keySet)
                        if (s.contains(DxfFileConstants.ACCESS_WIDTH)) {
                            accessWidth = planInfoProperties.get(s);
                            pl.addError(DxfFileConstants.ACCESS_WIDTH,
                                    DxfFileConstants.ACCESS_WIDTH + " is invalid .Text in dxf file is " + s);
                        }

                }

                if (accessWidth == null)
                    pl.addError(DxfFileConstants.ACCESS_WIDTH, DxfFileConstants.ACCESS_WIDTH + "  Is not defined");
                else {
                    accessWidth = accessWidth.replaceAll("[^\\d.]", "");
                    if (!accessWidth.isEmpty())
                        pi.setAccessWidth(BigDecimal.valueOf(Double.parseDouble(accessWidth)));
                    else
                        pl.addError(DxfFileConstants.ACCESS_WIDTH,
                                "The value for " + DxfFileConstants.ACCESS_WIDTH + " '" + accwidth + "' Is Invalid");

                }
            } else
                pi.setAccessWidth(BigDecimal.ZERO);

            return pi;
        }
        
        /**
         * @param doc
         * @param pl
         * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
         * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
         */
        private PlanDetail extractTotalFloorArea(DXFDocument doc, PlanDetail pl) {

            BigDecimal floorArea = BigDecimal.ZERO;
            List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, DxfFileConstants.BLDG_EXTERIOR_WALL);
            if (!bldgext.isEmpty())
                for (DXFLWPolyline pline : bldgext)
                    floorArea = floorArea.add(Util.getPolyLineArea(pline));

            pl.getBuilding().setTotalBuitUpArea(floorArea);

            List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, DxfFileConstants.FAR_DEDUCT);
            if (!bldDeduct.isEmpty())
                for (DXFLWPolyline pline : bldDeduct)
                    floorArea = floorArea.subtract(Util.getPolyLineArea(pline));

            if(LOG.isDebugEnabled()) LOG.debug("floorArea:" + floorArea);
            pl.getBuilding().setTotalFloorArea(floorArea);

            if (pl.getPlot().getArea() != null) {
                BigDecimal far = floorArea.divide(pl.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS);
                pl.getBuilding().setFar(far);
            }

            if (pl.getBuilding().getPolyLine() != null) {

                BigDecimal cvDeduct = BigDecimal.ZERO;
                BigDecimal buildingFootPrintArea = Util.getPolyLineArea(pl.getBuilding().getPolyLine());
                List<DXFLWPolyline> cvDeductPlines = Util.getPolyLinesByLayer(doc, DxfFileConstants.COVERGAE_DEDUCT);
                if (!cvDeductPlines.isEmpty()) {
                    for (DXFLWPolyline pline : cvDeductPlines)
                        cvDeduct.add(Util.getPolyLineArea(pline));
                }
                BigDecimal coverage = BigDecimal.valueOf(100);

                if (buildingFootPrintArea != null && pl.getPlanInformation().getPlotArea() != null
                        && pl.getPlanInformation().getPlotArea().intValue() > 0) {
                    coverage = buildingFootPrintArea.subtract(cvDeduct).multiply(BigDecimal.valueOf(100)).divide(
                            pl.getPlanInformation().getPlotArea(),
                            DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
                    pl.getBuilding().setCoverage(coverage);
                    if(LOG.isDebugEnabled()) LOG.debug("coverage:" + coverage);
                } else {
                    pl.addError(DxfFileConstants.COVERGAE_DEDUCT,
                            "Cannot calculate coverage as " + DxfFileConstants.BUILDING_FOOT_PRINT
                                    + " or " + DxfFileConstants.PLOT_AREA + " is not defined");

                }

            }

            return pl;

        }




    

    
}
