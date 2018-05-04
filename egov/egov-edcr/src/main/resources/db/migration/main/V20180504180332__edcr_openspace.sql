CREATE TABLE edcr_openspace
(
  id bigint not null,
  floor bigint,
  version numeric DEFAULT 0,
  CONSTRAINT pk_edcr_openspace PRIMARY KEY (id)
) ;