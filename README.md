# CODEPOP

## What is CODEPOP?
CODEPOP = CDPP = Continuous Delivery Pipeline Prototype

More specifically COPEPOP is a project that consists of a dummy webapp + database, packaged as seperate docker containers. It is accompanied by a dockerized jenkins server on which a full build/test/deployment-pipeline is configured automatically for this web app.

##Before building and running the jenkins docker setup
Following parameters and settings have to be supplied to be able to use the full functionality of Codepop

###GITHUB_USER and GITHUB_EMAIL
Sets user and email to tag a build in git.

###PRD_HOST and PRD_USER
PRD_HOST and PRD_USER are constants in the jenkinsworkflow.groovy file
These constants need to be filled in before the pipeline can deploy artifacts to the production server.

###Prd Host url
same url as PRD_HOST, to be filled in, in the Dockerfile of jenkins for the ssh-keyscan functionality.

###id_rsa and id_rsa.pub
These files are used to ssh to the production server at deploy time. 
You should place these files under /codepop/ops/jenkins where the Dockerfile will pick them up.

## How to build, run and test the application
### Build and run
- Start the db (for integrationTests)
``` bash
cd ops/
./start-db.sh
```
- Build the application
``` bash
cd ../dev/
./gradlew clean build
```
- Start the db and the newly built application
``` bash
cd ../ops/
./start-all.sh
```

### Health check
```
http://localhost:8080/health
```

### Test
```
http://localhost:8080/hello/<name>
```

## Howto install a jenkins change on the INT server from local dev machine?
```bash
# git pull 
# cd codepop/ops
# scp -r . youruser@yourintegrationserver.com:/home/youruser/install
(enter password)
# ssh youruser@yourintegrationserver.com
# cd install
# ./startJenkins.sh
```

## How to grant jenkins access to your own repository?
- Replace the GIT_REPOSITORY and BRANCH variables with your own repository and branch
- Generate a new keypair and add the public key to github (https://help.github.com/articles/generating-an-ssh-key/)
- Replace id_rsa and id_rsa.pub in /codepop/ops/jenkins with your own public and private key

## License
See the LICENSE file for license rights and limitations (Apache).
