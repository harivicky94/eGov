Feature: Create Trade License

  As a register user of the system
  I want to be able to Create New Trade License
  So that the TL records are up to date.

  # CREATE NEW LICENSE #

  @Sanity @TradeLicense
  Scenario Outline: Registered user creating a new license in the system
    Given creator logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he copy trade application number
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

  # Create Trade License with work flow #
  @Sanity @TradeLicense
  Scenario Outline: Register User create trade license with work flow

    Given creator logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he copy trade application number

    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for approver sanitaryInspector
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When sanitaryInspector logs in
    And he chooses to act upon above application number
    And he forwards for approver commissioner
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When commissioner logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When creator logs in
    And he chooses to act upon above application number
    And he generates the license certificate
    And user will be notified by "License"
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |


# Create Trade License with work flow #
  @Sanity @TradeLicense
  Scenario Outline: Register User create trade license with second level collection with work flow

#    Given CSCUser logs in
    Given creator logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he copy trade application number
#    And current user logs out

#    When PublicHealthJA logs in
#    And he choose to search trade license
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for approver sanitaryInspector
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When sanitaryInspector logs in
    And he chooses to act upon above application number
    And he changes trade area as "1200"
    And he forwards for approver commissioner
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When commissioner logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When creator logs in
#    And he chooses to act upon above application number
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number
    And he generates the license certificate
    And user will be notified by "License"
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |