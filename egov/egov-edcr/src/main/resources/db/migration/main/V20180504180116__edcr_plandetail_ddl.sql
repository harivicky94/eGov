create table edcr_plan_detail(
  id bigint,
  planInformation bigint,
  plot bigint,
  building bigint,
  electricLine bigint,
  basement bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_plandtl PRIMARY KEY (id),
	CONSTRAINT fk_edcr_plandtl_planinfo FOREIGN KEY (planInformation)
  REFERENCES EDCR_PLANINFO (id),
	CONSTRAINT fk_edcr_plandtl_plot FOREIGN KEY (plot)
  REFERENCES EDCR_plot (id),
  CONSTRAINT fk_edcr_plandtl_building FOREIGN KEY (building)
  REFERENCES EDCR_BUILDING (id),
  CONSTRAINT fk_edcr_plandtl_eline FOREIGN KEY (electricLine)
  REFERENCES EDCR_ELECTRICLINE (id),
  CONSTRAINT fk_edcr_plandtl_bsmnt FOREIGN KEY (basement)
  REFERENCES EDCR_BASEMENT (id),
  CONSTRAINT fk_edcr_plandtl_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_plandtl_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
);

CREATE SEQUENCE SEQ_EDCR_PLAN_DETAIL;