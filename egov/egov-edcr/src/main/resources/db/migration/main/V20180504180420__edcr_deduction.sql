CREATE TABLE edcr_deduction
(
id bigint not null,
floorunit bigint,
version numeric DEFAULT 0,
CONSTRAINT pk_edcr_deduction PRIMARY KEY (id)
) ;