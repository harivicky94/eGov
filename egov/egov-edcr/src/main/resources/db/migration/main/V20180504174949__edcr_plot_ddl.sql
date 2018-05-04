CREATE TABLE edcr_plot
(
  id bigint not null,
	frontYard bigint,
	rearYard bigint,
	sideYard1 bigint,
	sideYard2 bigint,
	bsmtFrontYard bigint,
	bsmtRearYard bigint,
	bsmtSideYard1 bigint,
	bsmtSideYard2 bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_plot PRIMARY KEY (id),
  CONSTRAINT fk_edcr_plot_frntyrd FOREIGN KEY (frontYard)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_rearyrd FOREIGN KEY (rearYard)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_sideyrd1 FOREIGN KEY (sideYard1)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_sideyrd2 FOREIGN KEY (sideYard2)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_bsmtfrntyrd FOREIGN KEY (bsmtFrontYard)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_bsmtrearyrd FOREIGN KEY (bsmtRearYard)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_bsmtsideyrd1 FOREIGN KEY (bsmtSideYard1)
  REFERENCES EDCR_YARD (id),
  CONSTRAINT fk_edcr_plot_bsmtsideyrd2 FOREIGN KEY (bsmtSideYard2)
  REFERENCES EDCR_YARD (id)
) ;

