Feature: Portfolio dashboard API
  The Greatest Banking Orchestrator exposes secure demo flows for the portfolio dashboard.

  Scenario: Admin creates an account and a debit transaction
    Given I am logged in as "admin" with password "approve-flow"
    When I create an account with document number "12345678900"
    And I create a transaction for that account with operation type 1 and amount 50.00
    Then the last response status should be 201
    And the transaction amount should be -50.00

  Scenario: User cannot create an account
    Given I am logged in as "user" with password "submit-flow"
    When I create an account with document number "23456789012"
    Then the last response status should be 403

  Scenario: User updates profile and lists avatar choices
    Given I am logged in as "user" with password "submit-flow"
    When I update my profile to display name "Portfolio User Updated" with avatar "robot-operator"
    Then the last response status should be 200
    And my profile display name should be "Portfolio User Updated"
    When I request the profile avatar catalog
    Then the last response status should be 200
    And the avatar catalog should include "robot-operator"

  Scenario: Missing JWT is rejected
    When I request my current profile without a token
    Then the last response status should be 403
