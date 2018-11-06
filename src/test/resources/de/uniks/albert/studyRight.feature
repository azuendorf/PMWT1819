
Feature: StudyRight

  Example for PM.

  Scenario: Karli enters math room

    Given The StudyRightUniversity is cool.
    Given The math room with 23 credits
    Given The entrance hall with 0 credits
    Given The entrance hall has a door to the math_room
    Given Karli is in the entrance_hall
    Given Karli has 0 credits and a motiviation of 42

    When Karli moves to the math room

    Then Karli is in the math_room
    Then Karli has 23 credits and a motivation of 19
