CREATE TABLE edcr_electricline
(
  id bigint not null,
	verticalDistance bigint,
	horizontalDistance bigint,
	voltage bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_eline PRIMARY KEY (id),
  CONSTRAINT fk_edcr_eline_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_eline_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;
