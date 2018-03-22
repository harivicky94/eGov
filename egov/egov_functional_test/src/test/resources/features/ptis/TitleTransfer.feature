Feature: Title transfer of a Property

  As a registered user of the system
  I should able to transfer title of a property

  # TRANSFER OF OWNERSHIP SCREEN #

  @Sanity @PropertyTax
  Scenario Outline: Register Choose to do title Transfer

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
    And user will select the required screen as "Transfer of title"
    And he searches for assessment with number
    And he enters Claimant Transferee details
    And he enters registration details for the property <registrationDetails>
    And he enters enclosure details
    And he click on Forward Button
    And he will copy the acknowledgement message with assessment number title
    And current user logs out

    And PTISJuniorAssistant logs in
    And he chooses to act upon above assessment number
    And he forwards for approval to PTISBillCollector
    And current user closes acknowledgement
    And current user logs out

    When PTISBillCollector logs in
    And he chooses to act upon above assessment number
    And he forwards for approval to PTISRevenueInspector
    And current user closes acknowledgement
    And current user logs out

    When PTISRevenueInspector logs in
    And he chooses to act upon above assessment number
    And he forwards for approval to PTISRevenueOfficer
    And current user closes acknowledgement
    And current user logs out

    Given PTISJuniorAssistant logs in
    And user will select the required screen as "Property Mutation Fee"
    And he searches for the assessment with mutation assessment number
    And he pay tax using Cash
    And current user logs out

    When PTISRevenueOfficer logs in
    And he chooses to act upon above assessment number
    And he forwards for approval to PTISCommissioner
    And current user closes acknowledgement
    And current user logs out

    When PTISCommissioner logs in
    And he chooses to act upon above assessment number
    And he approved the property with remarks "property approved" for transfer of ownership
    And current user closes acknowledgement

    And he chooses to act upon above assessment number
    And he does a digital signature

    When commissioner closes acknowledgement
    And current user logs out

    Examples:
      | registrationDetails |
      | register            |


