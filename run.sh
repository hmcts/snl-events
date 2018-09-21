#!/bin/sh
docker-compose down && ./gradlew instDist && docker-compose build && docker-compose up
