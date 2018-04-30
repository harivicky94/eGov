package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.VirtualBuilding;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFMText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.domain.builders.FastReportBuilder;

@Service
public class GeneralRule implements RuleService {

    private String regex = "[^\\d.]";
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;

    public PlanDetail validate(PlanDetail planDetail) {

        return planDetail;
    }
    
   /* public String getLocaleMessage(String code, String args) {
        return edcrMessageSource.getMessage(code, new String[] { args }, LocaleContextHolder.getLocale());

    }
*/
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
        pl.setPlanInformation(extractPlanInfo(pl, doc));
        /*
         * TEMPORARY ADDED FOR TESTING.
         */
        VirtualBuilding virtualBuilding = new VirtualBuilding();
        Building building = new Building();

        pl.setVirtualBuilding(virtualBuilding);
        pl.setBuilding(building);

        extractPlotDetails(pl, doc);
        extractBuildingDetails(pl, doc);
        extractFloorDetails(pl, doc);
        return pl;

    }
    
    public String getLocaleMessage(String code, String ...args) {
        return edcrMessageSource.getMessage(code, args, LocaleContextHolder.getLocale());

    }

    private void extractFloorDetails(PlanDetail pl, DXFDocument doc) {

        DXFLayer floorLayer = new DXFLayer();
        DXFLayer blockLayer = new DXFLayer();

        int blockNumber = 1;
        while (blockLayer != null) {
            int floorNo = -1;
            // Block already extracted, use the same to get floor details.
            Block block = pl.getBlockByName(DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber);

            if (block != null) {
                Building building = block.getBuilding();

                while (floorLayer != null) {
                    floorNo++;
                    String floorName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber + "_" + DxfFileConstants.FLOOR_NAME_PREFIX
                            + floorNo;
                    floorLayer = doc.getDXFLayer(floorName);
                    blockLayer = floorLayer;
                    if (!floorLayer.getName().equalsIgnoreCase(floorName)) {
                        break;
                    }
                    Floor floor = new Floor();
                    floor.setName(floorName);
                    floor.setNumber(String.valueOf(floorNo));
                    building.getFloors().add(floor);
                }

                int negetivFloorNo = 0;
                while (block != null && floorLayer != null) {
                    negetivFloorNo--;
                    String floorName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber + "_" + DxfFileConstants.FLOOR_NAME_PREFIX
                            + negetivFloorNo;
                    floorLayer = doc.getDXFLayer(floorName);
                    if (!floorLayer.getName().equalsIgnoreCase("FLOOR_" + negetivFloorNo)) {
                        break;
                    }
                    Floor floor = new Floor();
                    floor.setName(floorName);
                    floor.setNumber(String.valueOf(negetivFloorNo));
                    building.getFloors().add(floor);

                }

                if (building.getFloors() != null && !building.getFloors().isEmpty()) {
                    building.setMaxFloor(BigDecimal.valueOf(building.getFloors().size()));
                    building.setFloorsAboveGround(BigDecimal.valueOf(building.getFloors().size()));
                    building.setTotalFloors(BigDecimal.valueOf(building.getFloors().size()));
                }
                blockNumber++;
            } else
                break;
        }

    }

    private void extractPlotDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.PLOT_BOUNDARY);
        if (!polyLinesByLayer.isEmpty()) {
            pl.getPlot().setPolyLine(polyLinesByLayer.get(0));
        } else
            pl.addError(DxfFileConstants.PLOT_BOUNDARY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.PLOT_BOUNDARY }, null));
    }

    private void extractBuildingDetails(PlanDetail pl, DXFDocument doc) {

        List<DXFLWPolyline> polyLinesByLayer;

        DXFLayer blockLayer = new DXFLayer();
        BigDecimal maximumHeight=BigDecimal.ZERO;
        int blockNumber = 0;
        while (blockLayer != null) {
            blockNumber++;
            String blockName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber + "_" + DxfFileConstants.BUILDING_FOOT_PRINT;
            blockLayer = doc.getDXFLayer(blockName);
            if (!blockLayer.getName().equalsIgnoreCase(blockName)) {
                break;
            }
            
            BigDecimal ht = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber + "_" +DxfFileConstants.HEIGHT_OF_BUILDING, pl);
            maximumHeight= maximumHeight.compareTo(ht)>0?maximumHeight:ht;
            
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, blockName);

            Block block = new Block();
            block.setName(DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber);
            block.setNumber(String.valueOf(blockNumber));

            Building building = new Building();
            building.setPolyLine(polyLinesByLayer.get(0));
            
            building.setHeight(ht);// add height of building
            
            polyLinesByLayer = Util.getPolyLinesByLayer(doc,
                    DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber + "_" + DxfFileConstants.SHADE_OVERHANG);
            if (!polyLinesByLayer.isEmpty()) {
                Measurement shade = new Measurement();
                shade.setPolyLine(polyLinesByLayer.get(0));
                building.setShade(shade);
            }
            extractOpenStairs(doc, building);
            building.setPresentInDxf(true);
            block.setBuilding(building);
            pl.getBlocks().add(block);
        }

        if (pl.getBlocks().isEmpty()) {
            pl.addError(DxfFileConstants.BUILDING_FOOT_PRINT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.BUILDING_FOOT_PRINT }, null));
        }
            pl.getVirtualBuilding().setBuildingHeight(maximumHeight);
    }

    private void extractOpenStairs(DXFDocument doc, Building building) {
        List<DXFDimension> lines = Util.getDimensionsByLayer(doc, DxfFileConstants.OPEN_STAIR);
        if (lines != null)
            for (Object dxfEntity : lines) {
                BigDecimal value;
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
                        } else {

                            text2 = text2.replaceAll(regex, "");
                        }
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

    private PlanInformation extractPlanInfo(PlanDetail pl, DXFDocument doc) {
        PlanInformation pi = new PlanInformation();
        Map<String, String> planInfoProperties = Util.getPlanInfoProperties(doc);
        if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
            pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));
        String plotArea = planInfoProperties.get(DxfFileConstants.PLOT_AREA);

        if (plotArea == null) {
            Plot plot = new Plot();
            pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " is not defined in the Plan Information Layer");
            plot.setPresentInDxf(false);
            pl.setPlot(plot);
        } else
            try {
                Plot plot = new Plot();
                plotArea = plotArea.replaceAll(regex, "");
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
        if (!planInfoProperties.isEmpty()) {
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
                accessWidth = accessWidth.replaceAll(regex, "");
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

}
