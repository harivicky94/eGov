Feature: Create Trade License

  As a register user of the system
  I want to be able to Create New Trade License
  So that the TL records are up to date.

  # CREATE NEW LICENSE #

#  @Sanity @TradeLicense @NewLicense
  Scenario Outline: Registered user creating a new license in the system
    Given CSCUser logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

  # Create Trade License with work flow #
  @Sanity @TradeLicense @NewLicense @New
  Scenario Outline: Register User create trade license with work flows

#    Given TL_PHS_JA logs in
    Given CSCUser logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Acknowledged"
    And he verifies the License active
    And user will be notified by "No"
    And he closes search screen
    And current user logs out

    When TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "UnderWorkflow"
    And he verifies the License active
    And user will be notified by "No"
    And he closes search screen
    And he chooses to act upon above application number

    And he forwards for TL approver TL_SI
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_Commissioner
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_ADM_Commissioner logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox

    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "Yes"
    And he closes search screen
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

# Create Trade License with work flow #
  @Sanity @TradeLicense @NewLicense
  Scenario Outline: Register User create trade license with second level collection with work flow

    Given CSCUser logs in
#    Given TL_PHS_JA logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And current user logs out

    When TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for TL approver TL_SI
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he changes trade area as "1200"
    And he forwards for TL approver TL_Commissioner
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_ADM_Commissioner logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_PHS_JA logs in
#    And he chooses to act upon above application number
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
#    And he chooses to act upon above application number
#    And he generates the license certificate
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "Yes"
    And he closes search screen
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

  @Sanity @TradeLicense @NewLicense
  Scenario Outline: Register User create trade license with work flows (Approval by AMOH)

#    Given TL_PHS_JA logs in
    Given CSCUser logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And current user logs out

    When TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for TL approver TL_SI
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_SS
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SS logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_AMOH
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_AMOH logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox

    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "Yes"
    And he closes search screen
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

  @Sanity @TradeLicense @NewLicense
  Scenario Outline: Register User create trade license with work flows (Approval by MHO)

#    Given TL_PHS_JA logs in
    Given CSCUser logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And current user logs out

    When TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for TL approver TL_SI
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_SS
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SS logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_MHO
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_MHO logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox

    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "Yes"
    And he closes search screen
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |

  @Sanity @TradeLicense @NewLicense
  Scenario Outline: Register User create trade license with work flows (Approval by CMOH)

#    Given TL_PHS_JA logs in
    Given CSCUser logs in
    And user will select the required screen as "Create New License"
    And he enters trade owner details of new license <tradeDetailsData>
    And he enters trade location details of new license <tradeLocationData>
    And he enters trade details of new license <tradeDetailsData1>
    And he saves the application
    And he confirms to proceed
    And he copy trade application number
    And current user logs out

    When TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he choose action "Collect Fees"
    And he choose to payTax of applicationNumber
    And he chooses to act upon above application number

    And he forwards for TL approver TL_SI
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_SS
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SS logs in
    And he chooses to act upon above application number
    And he forwards for TL approver TL_CMOH
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_CMOH logs in
    And he chooses to act upon above application number
    And he approves application
    And he confirms to proceed
    And he closes acknowledgement page
    And he verifies that application not in his inbox

    And user will select the required screen as "Search Trade License"
    And he search existing application number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "Yes"
    And he closes search screen
    And current user logs out

    Examples:
      | tradeDetailsData         | tradeLocationData           | tradeDetailsData1        |
      | ownerDetailsTradeLicense | locationDetailsTradeLicense | tradeDetailsTradeLicense |
