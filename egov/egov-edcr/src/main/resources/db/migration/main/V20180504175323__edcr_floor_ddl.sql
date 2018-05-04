CREATE TABLE edcr_floor
(
  id bigint not null,
	name character varying (256),
	exterior bigint,
	building bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_floor PRIMARY KEY (id),
  CONSTRAINT fk_edcr_floor_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_floor_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;
