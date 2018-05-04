CREATE TABLE edcr_openstair
(
  id bigint not null,
  building bigint,
  version numeric DEFAULT 0,
  CONSTRAINT pk_edcr_openstair PRIMARY KEY (id)
) ;