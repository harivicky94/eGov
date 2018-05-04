CREATE TABLE edcr_building
(
  id bigint not null,
	buildingHeight bigint,
	buildingTopMostHeight bigint,
	totalFloorArea bigint,
	exteriorWall bigint,
	shade bigint,
	far bigint,
	coverage bigint,
	maxFloor bigint,
	totalFloors bigint,
	floorsAboveGround bigint,
	distanceFromBuildingFootPrintToRoadEnd bigint,
	totalBuitUpArea bigint,
  createdby bigint,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_bldng PRIMARY KEY (id),
  CONSTRAINT fk_edcr_bldng_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_bldng_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;


