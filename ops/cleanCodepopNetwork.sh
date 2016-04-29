#!/bin/bash

docker-compose -f docker-compose-db.yaml -f docker-compose-codepop.yaml stop
docker network rm codepopnw
docker network create codepopnw