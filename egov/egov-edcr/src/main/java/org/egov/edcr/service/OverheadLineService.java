package org.egov.edcr.service;

import java.math.BigDecimal;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.rule.RuleService;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;

@Service
public class OverheadLineService implements RuleService {

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        
        ElectricLine line = new ElectricLine();
         BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2, pl);
        if (dimension != null && dimension.compareTo(BigDecimal.ZERO) > 0) {
            line.setHorizontalDistance(dimension);
            line.setPresentInDxf(true);
        }
          BigDecimal dimensionVerticle = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE, pl);
        if (dimensionVerticle != null && dimensionVerticle.compareTo(BigDecimal.ZERO) > 0) {
            line.setVerticalDistance(dimensionVerticle);
            line.setPresentInDxf(true);
        }

           String voltage = Util.getMtextByLayerName(doc, "VOLTAGE");
        if (voltage != null)
            try {
                voltage = voltage.replaceAll("[^\\d.]", "");
                BigDecimal volt = BigDecimal.valueOf(Double.parseDouble(voltage));
                line.setVoltage(volt);
                line.setPresentInDxf(true);
            } catch (NumberFormatException e) {

                pl.addError("VOLTAGE",
                        "Voltage value contains non numeric character.Voltage must be Number specified in  KW unit, without the text KW");

            }
        else {
            if (dimension != null && dimension.compareTo(BigDecimal.ZERO) > 0)
                pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.HORIZ_CLEAR_OHE2);
            if (dimensionVerticle != null && dimensionVerticle.compareTo(BigDecimal.ZERO) > 0)
                pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.VERT_CLEAR_OHE);
        }
        pl.setElectricLine(line);

        return pl;
        
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        return pl;
    }

}
