#!/bin/bash

./cleanCodepopNetwork.sh

docker-compose -f docker-compose-db.yaml build
docker-compose -f docker-compose-db.yaml up -d

./cleanUpImages.sh