CREATE TABLE edcr_measurement
(
  id bigint not null,
  minimumDistance bigint,
  length bigint,
  width bigint,
  height bigint,
  mean bigint,
  area bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
  version numeric DEFAULT 0,
  CONSTRAINT pk_edcr_msrmt PRIMARY KEY (id),
  CONSTRAINT fk_edcr_msrmt_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_msrmt_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;

CREATE SEQUENCE SEQ_EDCR_MEASUREMENT;

