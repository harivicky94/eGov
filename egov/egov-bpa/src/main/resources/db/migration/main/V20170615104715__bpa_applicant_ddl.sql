alter table egbpa_applicant drop column username;
alter table egbpa_applicant drop column gender;
alter table egbpa_applicant drop column applicantname;
alter table egbpa_applicant drop column address;
alter table egbpa_applicant drop column mobilenumber;
alter table egbpa_applicant drop column emailid;
alter table egbpa_applicant drop column createdby;
alter table egbpa_applicant drop column createddate;
alter table egbpa_applicant drop column lastmodifiedby;
alter table egbpa_applicant drop column lastmodifieddate;
alter table egbpa_applicant alter column version set default 0;
alter table egbpa_application add column citizenaccepted boolean DEFAULT false;
alter table egbpa_application add column architectaccepted boolean DEFAULT false;