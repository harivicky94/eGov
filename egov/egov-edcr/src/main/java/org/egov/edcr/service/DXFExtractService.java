package org.egov.edcr.service;

import org.egov.edcr.entity.*;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLine;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class DXFExtractService {

    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    
    @Autowired
    private MinDistance minDistance;

    public PlanDetail extract(File dxfFile, EdcrApplication dcrApplication) {
        List<DXFLWPolyline> polyLinesByLayer;
        PlanDetail pl=new PlanDetail();

        try {
            Parser parser = ParserBuilder.createDefaultParser();
            parser.parse(dxfFile.getPath(), DXFParser.DEFAULT_ENCODING);
            // Extract DXF Data
            DXFDocument doc = parser.getDocument();
       
            pl.setPlanInformation(extractPlanInfo(doc));
            
            Plot plot=new Plot();
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, Plot.PLOT_BOUNDARY);
            if(polyLinesByLayer.size()>0)
            {
                plot.setPolyLine(polyLinesByLayer.get(0));

            }else
            {
                pl.addError("",edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,  new String[] { DcrConstants.PLOT_BOUNDARY },null));
            }
            pl.setPlot(plot);
            Building building=new Building();
            if(polyLinesByLayer.size()>0)
            {
                building.setPolyLine(polyLinesByLayer.get(0));

            }else
            {
                pl.addError("",edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,  new String[] { DcrConstants.BUILDING_FOOT_PRINT },null));
            }
            pl.setBuilding(building);
            
           
            
           pl.getPlot().setFrontYard(getYard(pl, doc,DcrConstants.FRONT_YARD));
           pl.getPlot().setRearYard(getYard(pl, doc,DcrConstants.REAR_YARD));
           pl.getPlot().setSideYard1(getYard(pl, doc,DcrConstants.SIDE_YARD_1));
           pl.getPlot().setSideYard2(getYard(pl, doc,DcrConstants.SIDE_YARD_2));
           
           pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.FRONT_YARD));
           pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.SIDE_YARD_1));
           pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.SIDE_YARD_2));
           pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.REAR_YARD));
          if(pl.getErrors().size()>0)
          {
              return pl;
          }
           
         pl= extractRoadDetails(doc,pl);
         pl.setNotifiedRoads(new ArrayList<>()); 
         pl.setNonNotifiedRoads(new ArrayList<>());



        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 


        return pl;
    }

    private Yard getYard(PlanDetail pl, DXFDocument doc,String yardName) {
        Yard yard=new Yard();
        List<DXFLWPolyline> frontYardLines = Util.getPolyLinesByLayer(doc, yardName);
        if (frontYardLines.size() > 0) {
            yard.setPolyLine(frontYardLines.get(0));
            yard.setArea(Util.getPolyLineArea(yard.getPolyLine()));
            yard.setMean(yard.getArea().divide(BigDecimal.valueOf(yard.getPolyLine().getBounds().getWidth()), 5,
                    RoundingMode.HALF_UP));

        }else
        {
            pl.addError("",edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,  new String[] { yardName },null));
        }
        
        return yard;
        
    }
    /**
     * 
     * @param doc
     * @return
     * add condition for what are mandatory
     */
    private PlanInformation extractPlanInfo(DXFDocument doc)
    {
        PlanInformation pi=new PlanInformation();
        Map<String, String> planInfoProperties = Util.getPlanInfoProperties(doc);
        pi.setArchitectInformation(planInfoProperties.get(DcrConstants.ARCHITECTNAME));
        return pi;
    }
    
    
    private PlanDetail  extractRoadDetails(DXFDocument doc,PlanDetail pl)
    {
        List<DXFLWPolyline> nonNotifiedRoad = Util.getPolyLinesByLayer(doc, DcrConstants.NON_NOTIFIED_ROAD);
        List<DXFLWPolyline> notifiedRoad = Util.getPolyLinesByLayer(doc,DcrConstants.NOTIFIED_ROADS);

        DXFLine line = Util.getSingleLineByLayer(doc, "Shortest Distance to road");
        if(line==null)
        {
            pl.addError("xxx", "Line Not found");
        }else{
        List<NotifiedRoad> notifiedRoads=new ArrayList<>();
        NotifiedRoad road=new NotifiedRoad();
        road.setShortestDistanceToRoad(BigDecimal.valueOf(line.getLength()));
        notifiedRoads.add(road);
        pl.setNotifiedRoads(notifiedRoads);
        }
                                                                                                                                                 // IS
                                                                                                                                                                      // IT
                                                                                                                                                                        // MANDATORY.
    
    return pl;
        
    }
    

}
