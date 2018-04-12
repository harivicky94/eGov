package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class ParkingService implements RuleService {
    private Logger logger = Logger.getLogger(SetBackService.class);
    final Ray rayCasting = new Ray(new Point(-1.123456789, -1.987654321, 0d));

    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;
    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> residentialUnit = new LinkedList<>();
        List<DXFLWPolyline> residentialUnitDeduction = new ArrayList<>();
        List<DXFLWPolyline> removeDeduction = new ArrayList<>();
        boolean layerPresent = true;

        layerPresent = doc.containsDXFLayer(DxfFileConstants.RESI_UNIT);

        if (layerPresent) {
            List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, DxfFileConstants.RESI_UNIT);

            if (!bldgext.isEmpty())
                for (DXFLWPolyline pline : bldgext) {
                    residentialUnit.add(pline);
                }
        }
        layerPresent = doc.containsDXFLayer(DxfFileConstants.RESI_UNIT_DEDUCT);
        if (layerPresent) {
            List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, DxfFileConstants.RESI_UNIT_DEDUCT);
            if (!bldDeduct.isEmpty())
                for (DXFLWPolyline pline : bldDeduct) {
                    residentialUnitDeduction.add(pline);
                }
        }

       

        int i = 0;
        for (DXFLWPolyline resUnit : residentialUnit) {
            FloorUnit floorUnit = new FloorUnit();
            floorUnit.setPolyLine(resUnit);
            i++;
            Polygon polygon = Util.getPolygon(resUnit);
            Iterator vertexIterator = resUnit.getVertexIterator();
            List<Point> points = new ArrayList<>();
            while (vertexIterator.hasNext()) {
                DXFVertex next = (DXFVertex) vertexIterator.next();
                Point p = new Point(next.getX(), next.getY(), 0d);
                points.add(p);
            }

            BigDecimal deduction = BigDecimal.ZERO;
            for (DXFLWPolyline residentialDeduct : residentialUnitDeduction) {
                boolean contains = false;
                Iterator buildingIterator = residentialDeduct.getVertexIterator();
                while (buildingIterator.hasNext()) {
                    DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
                    Point point = dxfVertex.getPoint();
                    if (rayCasting.contains(point, polygon)) {
                        contains = true;
                        Measurement measurement = new Measurement();
                        measurement.setPolyLine(residentialDeduct);
                        floorUnit.getDeductions().add(measurement);
                    }

                }
                if (contains) {
                    logger.info("current deduct " + deduction + "  :add deduct for rest unit " + i + " area added "
                            + Util.getPolyLineArea(residentialDeduct));
                    deduction = deduction.add(Util.getPolyLineArea(residentialDeduct));
                }

            }

            floorUnit.setTotalUnitDeduction(deduction);
            pl.getFloorUnits().add(floorUnit);

        }

        layerPresent = doc.containsDXFLayer(DxfFileConstants.PARKING_SLOT);

        if (layerPresent) {
            List<DXFLWPolyline> bldparking = Util.getPolyLinesByLayer(doc, DxfFileConstants.PARKING_SLOT);
            if (!bldparking.isEmpty()) {
                if(logger.isDebugEnabled()) logger.debug("Parking slot ");
                for (DXFLWPolyline pline : bldparking) {
                    if(logger.isDebugEnabled()) logger.debug("Width:"+pline.getBounds().getWidth());
                    if(logger.isDebugEnabled()) logger.debug("Height"+pline.getBounds().getHeight());
                    Measurement measurement = new Measurement();
                    measurement.setWidth(BigDecimal.valueOf(pline.getBounds().getWidth()));
                    measurement.setHeight(BigDecimal.valueOf(pline.getBounds().getHeight()));
                    measurement.setPolyLine(pline);
                    pl.getParkingSlots().add(measurement);
                }
            }
        }
        return pl;

    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return null;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        return null;
    }

}
