﻿alter table egtl_feematrix drop column uniqueno;
alter table egtl_feematrix add constraint unq_egtl_feematrix unique(natureofbusiness,licenseapptype,licensecategory,subcategory,feetype,unitofmeasurement,financialyear);