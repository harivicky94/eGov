CREATE TABLE edcr_parkingslot
(
id bigint not null,
plandetail bigint,
version numeric DEFAULT 0,
CONSTRAINT pk_edcr_parkingslot PRIMARY KEY (id)
) ;