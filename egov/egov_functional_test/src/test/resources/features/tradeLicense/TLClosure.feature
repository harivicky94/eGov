Feature: Trade license closure


  # Trade License Closure #
  @Sanity @TradeLicense @LicenseClosure
  Scenario Outline: Registered user choose for trade license closure

    Given CSCUser logs in
    And user will select the required screen as "Search Trade License"
    And he choose a trade license for closure as <closureDetails>
    And he choose to close trade license
    And he closes search screen
    And current user logs out

    When TL_PHS_JA logs in
    And he chooses to act upon above application number
    And he forwards for approver TL_SI
    And he confirms to proceed
    And he closes the acknowledgement page
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he forwards for approver TL_Commissioner
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_ADM_Commissioner logs in
    And he chooses to act upon above application number
    And he approves the closure
    And he confirms to proceed
#    And he saves the application
    And he closes acknowledgement page
    And user will select the required screen as "Search Trade License"
    And he choose to search with license number
    And he verifies the application status
    And user will be notified by "Cancelled"
    And he verifies the License active
    And user will be notified by "NO"
    And he closes search screen
    And current user logs out

    Examples:
      | closureDetails    |
      | licenceForClosure |

#  @Sanity @TradeLicense @LicenseClosure
  Scenario Outline: Registered user choose for trade license closure and commissioner rejects it

    Given CSCUser logs in
    And user will select the required screen as "Search Trade License"
    And he choose a trade license for closure as <closureDetails>
    And he choose to close trade license
    And he closes search screen
    And current user logs out

    When TL_PHS_JA logs in
    And he chooses to act upon above application number
    And he forwards for approver TL_SI
    And he confirms to proceed
    And he closes the acknowledgement page
    And current user logs out

    When TL_ADM_Commissioner logs in
    And he chooses to act upon above application number
    And he rejects the application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he rejects the application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_PHS_JA logs in
    And he chooses to act upon above application number
    And he cancel the application
    And he confirms to proceed
    And he closes acknowledgement page
    And user will select the required screen as "Search Trade License"
    And he choose to search with license number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "YES"
    And he closes search screen
    And current user logs out

    Examples:
      | closureDetails    |
      | licenceForClosure |


  @Sanity @TradeLicense @LicenseClosure
  Scenario Outline: Registered user choose for trade license closure and SI rejects it

    Given TL_PHS_JA logs in
    And user will select the required screen as "Search Trade License"
    And he choose a trade license for closure as <closureDetails>
    And he forwards for approver TL_SI
    And he confirms to proceed
    And he closes the acknowledgement page
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he rejects the application
    And he confirms to proceed
    And he closes acknowledgement page
    And current user logs out

    When TL_PHS_JA logs in
    And he chooses to act upon above application number
    And he rejects the application
    And he confirms to proceed
    And he closes acknowledgement page
    And user will select the required screen as "Search Trade License"
    And he choose to search with license number
    And he verifies the application status
    And user will be notified by "Active"
    And he verifies the License active
    And user will be notified by "YES"
    And he closes search screen
    And current user logs out

    Examples:
      | closureDetails    |
      | licenceForClosure |