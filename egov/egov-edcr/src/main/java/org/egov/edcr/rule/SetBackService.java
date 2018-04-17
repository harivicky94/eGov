package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.entity.utility.SetBack;
import org.egov.edcr.service.MinDistance;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class SetBackService implements RuleService {
    private Logger logger = Logger.getLogger(SetBackService.class);
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
       
        SetBack setBack = new SetBack();
          Yard yard= getYard(pl, doc, DxfFileConstants.FRONT_YARD);
          yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.FRONT_YARD));
          setBack.setFrontYard(yard);
          pl.getPlot().setFrontYard(yard);
          
          yard= getYard(pl, doc, DxfFileConstants.REAR_YARD);
          yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.REAR_YARD));
          setBack.setRearYard(yard);
          pl.getPlot().setRearYard(yard);
          
          yard= getYard(pl, doc, DxfFileConstants.SIDE_YARD_1);
          yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_1));
          setBack.setSideYard1(yard);
          pl.getPlot().setSideYard1(yard);
          yard= getYard(pl, doc, DxfFileConstants.SIDE_YARD_2);
          yard.setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_2));

          setBack.setSideYard2(yard);
          pl.getPlot().setSideYard2(yard);
          pl.getPlot().getSetBacks().add(setBack);

     
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
        return pl;
    }

}
