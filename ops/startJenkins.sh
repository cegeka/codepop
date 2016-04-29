#!/usr/bin/env bash
JENKINS_HOME="/var/jenkins_home"

if [ ! -d $JENKINS_HOME ]
then
	echo "Need password to create $JENKINS_HOME as mount point for JENKINS_HOME"
    sudo mkdir -p $JENKINS_HOME
    sudo chown 1000 $JENKINS_HOME
fi

sudo mkdir -p $JENKINS_HOME/.gradle
sudo cp /home/sandboxcd/.ssh/id_rsa $JENKINS_HOME/ssh_prdserver_key
sudo mkdir -p $JENKINS_HOME/jobs/Codepop
sudo cp /home/sandboxcd/install/jenkins/job-config.xml $JENKINS_HOME/jobs/Codepop/config.xml
sudo cp /home/sandboxcd/install/docker /etc/default/docker

sudo service docker stop
sudo service docker start

docker build -t cdsandbox/gradle gradle/
docker build -t cdsandbox/jenkins jenkins/

docker network create -d bridge ci
docker-compose -f docker-compose-ci.yaml up -d 