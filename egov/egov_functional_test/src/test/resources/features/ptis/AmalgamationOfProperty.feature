Feature: Amalgamation Of Property

  As a registered user of system
  I should be able to create a amalgamation of property

  # AMALGAMATION OF PROPERTY #

  @Sanity @PropertyTax
  Scenario: Registered user choose to do amalgamation of property

  Given PTISCommissioner logs in
  And user will select the required screen as "Data entry screen" with condition as "ptis"
  And he creates a new assessment for a private residential property
  Then dataEntry Details saved successfully
  And he choose to add edit DCB
  And he choose to close the dataentry acknowledgement screen
  And current user logs out

  Given PTISJuniorAssistant logs in
  And user will select the required screen as "collect tax"
  And he searches for assessment with number
  And he chooses to pay tax
  And he pay tax using Cash
  And current user logs out

  Given CSCUser logs in
  And user will select the required screen as "Amalgamation of Property"
  And he searches for assessment with number
  And he search for the Amalgamated Properties
  And he click on Forward Button
  And current user closes tax exemption acknowledgement
  And current user logs out

  When PTISJuniorAssistant logs in
  And he chooses to act upon above assessment number
  And he forwards for PTIS approver to PTISBillCollector
  And current user closes tax exemption acknowledgement
  And current user logs out

  When PTISBillCollector logs in
  And he chooses to act upon above assessment number
  And he forwards for PTIS approver to PTISRevenueInspector
  And current user closes tax exemption acknowledgement
  And current user logs out

  When PTISRevenueInspector logs in
  And he chooses to act upon above assessment number
  And he forwards for PTIS approver to PTISRevenueOfficer
  And current user closes tax exemption acknowledgement
  And current user logs out

  When PTISRevenueOfficer logs in
  And he chooses to act upon above assessment number
  And he forwards for PTIS approver to PTISCommissioner
  And current user closes tax exemption acknowledgement
  And current user logs out

  When PTISCommissioner logs in
  And he chooses to act upon above assessment number
  And he approved the property with remarks "amalgamation-approve"
  And current user closes tax exemption acknowledgement
  And he chooses to act upon above assessment number
  And he does a digital signature
  When commissioner closes acknowledgement
  And current user logs out


