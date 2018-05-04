CREATE TABLE edcr_room
(
  id bigint not null,
	floor bigint,
  createdby bigint ,
  createddate TIMESTAMP without TIME ZONE,
  lastmodifieddate TIMESTAMP without TIME ZONE,
  lastmodifiedby bigint,
	version numeric DEFAULT 0,
	CONSTRAINT pk_edcr_room PRIMARY KEY (id),
  CONSTRAINT fk_edcr_room_floor FOREIGN KEY (floor)
  REFERENCES EDCR_FLOOR (id),
  CONSTRAINT fk_edcr_room_mdfdby FOREIGN KEY (lastmodifiedby)
  REFERENCES EG_USER (id),
  CONSTRAINT fk_edcr_room_crtedby FOREIGN KEY (createdby)
  REFERENCES EG_USER (id)
) ;

