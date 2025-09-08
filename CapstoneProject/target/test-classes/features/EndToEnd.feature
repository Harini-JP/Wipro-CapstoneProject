Feature: End-to-End Checkout Flow on bstackdemo

  Scenario: Complete purchase journey
    Given user launches browser
    And user logs in with "demouser" and "testingisfun99"
    When user adds "iPhone 12 Mini" to the cart
    And user removes the product from the cart
    And user adds "iPhone 12 Mini" again to the cart
    And user proceeds to checkout
    And user fills shipping details
    Then user should reach the confirmation page
    And user logs out successfully
