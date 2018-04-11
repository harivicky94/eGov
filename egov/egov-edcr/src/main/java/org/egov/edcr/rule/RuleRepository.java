package org.egov.edcr.rule;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.service.OverheadLineService;

public class RuleRepository {
    
    public static List<Class> rules =new ArrayList<>();
    static {
      rules.add(GeneralRule.class);
      rules.add(SetBackService.class);
      rules.add(MeanOfAccess.class);
      rules.add(OverheadLineService.class);
      rules.add(ParkingService.class);
              
    }

}
