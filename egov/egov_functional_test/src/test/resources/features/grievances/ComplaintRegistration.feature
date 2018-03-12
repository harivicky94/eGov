Feature: Register Complaint

  As a citizen register complaint directly in website

  @Sanity @Grievance
  Scenario Outline: Register a Complaint with Citizen Login

    Given citizen logs in
    When he choose to register complaint with his login
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And he choose to search for above complaint
    And he selects the user for which the above complaint has routed
    And citizen sign out

    And employee logs in
    And he chooses to act upon above application number
    And he mark status as COMPLETED
    And user will be notified by "successfully"
    And he verifies that application not in his inbox
    And current user logs out

    Examples:
      | grievanceDetails |
      | grievanceDetails |

  @Sanity @Grievance
  Scenario Outline:  Official Register Grievance

    Given LightingSuperintendent logs in
    And user will select the required screen as "Officials Register Grievance"
    And he choose to enter contact information as <contactDetails>
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And current user logs out

    Examples:
      | contactDetails | grievanceDetails |
      | contactInfo    | grievanceDetails |

  @Sanity @Grievance
  Scenario Outline: Official Register Grievance and forwards

    Given LightingSuperintendent logs in
    And user will select the required screen as "Officials Register Grievance"
    And he choose to enter contact information as <contactDetails>
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And user will select the required screen as "Search Grievance"
    And he selects the user for which the above complaint has routed
    And current user logs out

    And employee logs in
    And he chooses to act upon above application number
    And he forwards for approver sanitaryInspector1
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he mark status as COMPLETED
    And user will be notified by "successfully"
    And he verifies that application not in his inbox
    And current user logs out


    Examples:
      | contactDetails | grievanceDetails |
      | contactInfo    | grievanceDetails |


  @Sanity @Grievance
  Scenario Outline: Citizen register a complaint and official forwards it to next level

    Given citizen logs in
    When he choose to register complaint with his login
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And he choose to search for above complaint
    And he selects the user for which the above complaint has routed
    And citizen sign out

    And employee logs in
    And he chooses to act upon above application number
    And he forwards for approver sanitaryInspector1
    And he verifies that application not in his inbox
    And current user logs out

    When TL_PHS_SI logs in
    And he chooses to act upon above application number
    And he mark status as COMPLETED
    And user will be notified by "successfully"
    And he verifies that application not in his inbox
    And current user logs out

    Examples:
      | grievanceDetails |
      | grievanceDetails |


  @Sanity @Grievance

  Scenario Outline: Citizen register a complaint and withdraw it

    Given citizen logs in
    When he choose to register complaint with his login
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And he choose to search for above complaint
    And he selects the user for which the above complaint has routed
    And he search complaint in his Inbox
    And he WITHDRAWN the complaint
    And citizen sign out

    When employee logs in
    And he verifies that application not in his inbox
    And current user logs out

    Examples:
      | grievanceDetails |
      | grievanceDetails |


  @Sanity @Grievance
  Scenario Outline: Citizen register a complaint, officer resolves it and citizen reopens the complaint

    Given citizen logs in
    When he choose to register complaint with his login
    And he choose to enter grievance details as <grievanceDetails>
    And user will be notified by "successfully"
    And he copies CRN and closes the acknowledgement
    And he choose to search for above complaint
    And he selects the user for which the above complaint has routed
    And citizen sign out

    And employee logs in
    And he chooses to act upon above application number
    And he mark status as REJECTED
    And user will be notified by "successfully"
    And he verifies that application not in his inbox
    And current user logs out

    When citizen logs in
    And he search complaint in his Inbox
    And he REOPENED the complaint
    And citizen sign out

    Examples:
      | grievanceDetails |
      | grievanceDetails |