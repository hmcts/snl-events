# Scheduling and listing Events

[![Build Status](https://travis-ci.org/hmcts/snl-events.svg?branch=master)](https://travis-ci.org/hmcts/snl-events)

## Purpose

Scheduling and Listing project provides application for managing sessions, listing requests and hearings.
The purpose of this service is to provide domain logic and data storage for the application. 
It also controls the [rules engine](https://github.com/hmcts/snl-rules) and sends andy facts and gathers consequences if needed.

## What's inside

It contains:
 * application
 * database schema change-sets using [Liquibase](https://www.liquibase.org/)
 * docker setup
 * swagger configuration for api documentation
 * MIT license and contribution information

The application exposes health endpoint (http://localhost:8092/health) and metrics endpoint
(http://localhost:8092/metrics).

## Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

## Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew bootRepackage
```

Create docker image:

```bash
  docker-compose build
```

### Running Locally (Recommended)

For this approach, the database must still be served via docker:
```bash
  docker-compose up snl-events-db
```

The application can be run locally using IntelliJ or by executing the following command (in another terminal window):
```bash
./gradlew bootRun
```

### Running in Docker

Run the distribution (created in `build/libs` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8092` in this app) and PostgreSql database.

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8092/health
```

You should get a response similar to this:

```
  {"status":"UP"}
```

### Alternative script to run application in Docker

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Testing and Preparing for Pull Requests

Before creating a PR, ensure that all of the code styling checks and tests have been done locally (they will be caught on Jenkins if there are any discrepancies)

### 1. Code Style

```bash
./gradlew checkStyleMain

./gradlew checkStyleIntegration

./gradlew checkStyleTest
```

### 2. Testing

```bash
./gradlew test
```

### Authentication

If you want to call a service locally then you may need to disable the authentication on the service.
The best way is to set env variable MANAGEMENT_SECURITY_ENABLED to false, you can do it in IntelliJ Idea in the build configuration, i.e. DEBUG, 
or simply just on your local computer
```
MANAGEMENT_SECURITY_ENABLED=false
```
Now on your localhost you will be able to connect from postman without authentication.

## Other
### Check dependencies updates

Task to determine which dependencies have updates. Usage:

    ```bash
      ./gradlew dependencyUpdates -Drevision=release
    ```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
