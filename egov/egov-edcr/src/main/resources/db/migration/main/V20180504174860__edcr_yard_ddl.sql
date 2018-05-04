CREATE TABLE edcr_yard
(
  id bigint not null,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_yard PRIMARY KEY (id),
  CONSTRAINT fk_edcr_yard_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_yard_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;

