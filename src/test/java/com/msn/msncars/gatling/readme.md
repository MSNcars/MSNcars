# Gatling Performance Tests

---

## Prerequisites

- Ensure the application is **up and running**.
- Create test users before executing performance scenarios.

---

## Create / Delete Test Users

Use the `PerformanceTestUserManager` class from the `prerequisites` package with params:
- create <number_of_users>
- delete

This class creates users and saves information about them in test resources.

## Running Performance Tests
./mvnw gatling:test -Dusers=<number_of_users> -Dduration=<test_duration_in_seconds>

./mvnw gatling:test -Dusers=100 -Dduration=45 
- Runs the test with 100 virtual users for 45 seconds.
- The number of users defined here can be greater than the number of users created,
since Gatling cycles through the test user list.

./mvnw gatling:test
- Runs the test with 50 virtual users for 30 second (default values).

After executing the test command, you'll be prompted to choose which simulation
to run by entering its number from the list.

