#!/bin/sh
docker-compose down && gradle instDist && docker-compose build && docker-compose up
