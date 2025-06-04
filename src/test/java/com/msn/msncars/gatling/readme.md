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

## Running Performance Tests against multiple spring boot containers

Our application is stateless which means that you can run multiple containers with our application
and configure nginx to send request to whichever container.

In order to do that, stop running application and all containers (to avoid port conflicts) and after that run:
```bash
docker compose -p msn-car-gatling -f src/test/java/com/msn/msncars/gatling/prerequisites/optional/docker-compose-with-deployment.yaml up -d
```

This will run 3 containers that will independently serve http requests and configure nginx
to send request to them using Round Robin algorithm.

By default, docker compose will use MSNcars docker image from ghcr.io/msncars/msncars from master branch,
you can change that by editing docker-compose.with-deployment.yaml.

This will create new keycloak instance, which means that you need to run PerformanceTestUserManager to create users again.

If you already have MSNcars docker image downloaded locally, you will need to manually pull the image to see the latest changes:
```bash
docker pull ghcr.io/msncars/msncars:master
```