#!/bin/bash

./cleanCodepopNetwork.sh

cp ../dev/build/libs/codepop.jar codepop/codepop.jar

docker-compose -f docker-compose-db.yaml -f docker-compose-codepop.yaml build
docker-compose -f docker-compose-db.yaml -f docker-compose-codepop.yaml up -d

./cleanUpImages.sh