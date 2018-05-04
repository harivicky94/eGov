CREATE TABLE edcr_culdesac_road
(
  id bigint not null,
	shortestDistanceToRoad bigint,
	distanceFromCenterToPlot bigint,
	plandetail bigint,
  createdby bigint ,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_culdesac PRIMARY KEY (id),
  CONSTRAINT fk_edcr_culdesac_road_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_culdesac_road_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;

