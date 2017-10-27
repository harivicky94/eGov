Feature: Create Test Employees for Trade License Module Testing

  Scenario Outline: Create Positions for Trade License Module

    Given admin logs in
    And user will select the required screen as "Create Position" with condition as "/position"
    And user selects the position <department> position <designation> and the position name as <position>
    Then user clicks on create position button, and closes the page
    And current user logs out

    Examples:
      | department                   | designation      | position |
      | PUBLIC HEALTH AND SANITATION | Junior Assistant | JA-TL    |
      | PUBLIC HEALTH AND SANITATION | Sanity Inspector | SI-TL    |
      | ADMINISTRATION               | Commissioner     | Comm-TL  |

  Scenario Outline: Create Employees for Trade License Module

    Given admin logs in
    And user will select the required screen as "Create Employee" with condition as "/employee"
    And user enters the employee details as <employeeDetails>
    And user will enter the assignment details as <assignmentDetails>
    And user will enter the jurisdiction details as <jurisdictionDetails>
    Then user clicks on submit button
    And user will select the required screen as "Search User Role" with condition as "userrole/search"
    And user selects user name for searching role as <employeeDetails>
    And user updates particular roles for an employee as <assignmentDetails>
    And user will select the required screen as "Reset Password"
    And user on reset password screen enter the employee name as <employeeDetails>
    And current user logs out

    Examples:
      | employeeDetails      | assignmentDetails    | jurisdictionDetails |
      | TradeJuniorAssistant | TradeJuniorAssistant | JurisdictionList1   |
      | TradeSanityInspector | TradeSanityInspector | JurisdictionList2   |
      | TradeCommissioner    | TradeCommissioner    | JurisdictionList3   |