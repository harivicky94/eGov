Feature: Create Test Employees for Advertisement Tax Module Testing

  Scenario Outline: : Create Positions

    Given admin logs in
    And user will select the required screen as "Create Position" with condition as "/position"
    And user selects the position <department> position <designation> and the position name as <position>
    Then user clicks on create position button, and closes the page
    And current user logs out

    Examples:
      | department     | designation      | position |
      | ADMINISTRATION | Junior Assistant | JA 1 Adv Tax   |
      | ADMINISTRATION | Junior Assistant | JA 2 Adv Tax   |
      | ADMINISTRATION | Commissioner     | ADComm   |

  Scenario Outline: Create Employees as Test Data

    Given admin logs in
    And user will select the required screen as "Create Employee" with condition as "/employee"
    And user enters the employee details as <employeeDetails>
    And user will enter the assignment details as <assignmentDetails>
    And user will enter the jurisdiction details as <jurisdictionDetails>
    Then user clicks on submit button
    And user will select the required screen as "Search User Role" with condition as "userrole/search"

    Examples:
      | employeeDetails       | assignmentDetails     | jurisdictionDetails |
      | AdTaxJuniorAssistant1 | AdTaxJuniorAssistant1 | JurisdictionList1   |
      | AdTaxJuniorAssistant2 | AdTaxJuniorAssistant2 | JurisdictionList2   |
      | AdTaxCommissioner     | AdTaxCommissioner     | JurisdictionList3   |