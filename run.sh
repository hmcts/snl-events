#!/bin/sh
docker-compose down && ./gradlew bootRepackage && docker-compose build && docker-compose up
