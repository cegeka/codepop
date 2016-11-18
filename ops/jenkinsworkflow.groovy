/********** Global variables **********/
VERSION_NUMBER = ""
VERSION_SUFFIX = ""
NO_OPTIONS = ""
GIT_REPOSITORY = "git@github.com:cegeka/codepop.git"
BRANCH = "demo"

/******* Github stuff *******/
GIT_REPOSITORY = "git@github.com:cegeka/codepop.git"
GITHUB_USER = "imanuelr"
GITHUB_EMAIL = "imanuel.r@gmail.com"

/***** Networks *****/
JENKINS_NETWORK = "ci"
PRD_NETWORK = "codepop"

/***** Images *****/
GRADLE_IMAGE = ""
DB_IMAGE = ""
APP_IMAGE = ""

/***** Containers *****/
GRADLE_CONTAINER = ""
DB_CONTAINER = ""
APP_CONTAINER = ""

/****** SSH Stuff *****/
PRD_HOST = "10.0.7.13"
PRD_USER = "vagrant"
SSH_IDENTITY_FILE = "/var/jenkins_home/ssh_prdserver_key"

/*********** Build workflow ***********/
stage name: "CompileAndUnitTest"
    node("linux && docker") {
        checkoutDevelop()
        determineVersionNumber()
        determineDockerContainerNames()
        determineDockerImageNames()
        compileAndUnitTest()
        stopWhenFailures()
        stashWorkspace()
    }

stage name: "IntegrationTest"
    node("linux && docker") {
        unstashWorkspace()
        buildDBImage()
        integrationTest()
        stopWhenFailures()
        stashWorkspace()
    }

stage name: "BuildApp"
    node("linux && docker") {
        unstashWorkspace()
        buildApp()
        buildAppImage()
    }

//stage name: "End2End"
//    node("linux && docker"){
//        unstashWorkspace()
//        end2EndTest()
//    }

stage name: "GitTag"
    node {
        unstashWorkspace()
        gitTag()
    }

stage name: "PrePushToPRD"
    input "Do you want to deploy to prd?"

stage name: "PushToPRD"
    node {
        tarImages()
        copyImagesToPRD()
        stopPRD()
        removeImagesAndContainersOnPRD()
        importImagesOnPRD()
        cleanUpTarsOnPRD()
        startPRDNetwork()
        runPRD()
    }

stage name: "SmokeTestPRD"
    node("linux && docker") {
        unstashWorkspace()
        smokeTest()
    }

/********** functions **********/
def checkoutDevelop() {
    cleanWorkspace()
    git branch: "${BRANCH}", url: "${GIT_REPOSITORY}"
}

def cleanWorkspace() {
    sh "rm -rf *"
}

def determineVersionNumber() {
    VERSION_SUFFIX = executeCommand("git rev-list --count HEAD") + "-" + env.BUILD_NUMBER
    VERSION_NUMBER = "0.0.${VERSION_SUFFIX}"
    println "Building with version number: ${VERSION_NUMBER}"
}

def determineDockerContainerNames() {
    GRADLE_CONTAINER = "gradle${VERSION_SUFFIX}"
    APP_CONTAINER = "codepop${VERSION_SUFFIX}"
    DB_CONTAINER = "db${VERSION_SUFFIX}"
}

def determineDockerImageNames() {
    GRADLE_IMAGE = "cdsandbox/gradle:latest"
    DB_IMAGE = "cdsandbox/db:${VERSION_SUFFIX}"
    APP_IMAGE = "cdsandbox/codepop:${VERSION_SUFFIX}"
}

def compileAndUnitTest() {
    try {
        gradle("clean test -Pversion=${VERSION_NUMBER}", NO_OPTIONS)
    } catch (error) {
        printErrorAndMarkAsFailed(error)
    } finally {
        archiveTestResults()
    }
}

def integrationTest() {
    try {
        startContainer(DB_IMAGE, DB_CONTAINER, JENKINS_NETWORK, NO_OPTIONS)
        gradle("integrationTest -Pversion=${VERSION_NUMBER}", "-e spring.data.mongodb.host=db${VERSION_SUFFIX}.${JENKINS_NETWORK}")
    } catch (error) {
        cleanImages()
        printErrorAndMarkAsFailed(error)
    } finally {
        archiveIntegrationTestResults()
        removeContainer(DB_CONTAINER)
    }
}

def buildApp() {
    try {
        gradle("build -Pversion=${VERSION_NUMBER}", NO_OPTIONS)
    } catch (error) {
        printErrorAndMarkAsFailed(error)
    }
}

def end2EndTest() {
    try {
        startContainer(DB_IMAGE, DB_CONTAINER, JENKINS_NETWORK, "-p 27017:27017")
        startContainer(APP_IMAGE, APP_CONTAINER, JENKINS_NETWORK, "-e spring.data.mongodb.host=db${VERSION_SUFFIX}.${JENKINS_NETWORK} -p 8080:8080")
        gradle("-DdbHost=db${VERSION_SUFFIX}.${JENKINS_NETWORK} -DserverHost=codepop${VERSION_SUFFIX}.${JENKINS_NETWORK} e2eTest", NO_OPTIONS)
    } catch (error) {
        cleanImages()
        printErrorAndMarkAsFailed(error)
    } finally {
        archiveEndToEndTestResults()
        removeContainer(DB_CONTAINER)
        removeContainer(APP_CONTAINER)
    }
}

def gitTag() {
    sh "git config user.name '${GITHUB_USER}'"
    sh "git config user.email '${GITHUB_EMAIL}'"
    sh "git config push.default 'simple'"

    sh "git tag -a ${VERSION_NUMBER} -m 'Codepop version ${VERSION_NUMBER}'"
    sh "git push origin ${VERSION_NUMBER}"
}

def smokeTest(){
    gradle("smokeTest -PhealthUrl=http://${PRD_HOST}:8080/health", NO_OPTIONS)
}

def gradle(task, containerOptions) {
    try {
        startGradleContainer(GRADLE_CONTAINER, containerOptions)
        execGradle(GRADLE_CONTAINER, task)
    } catch (error) {
        printErrorAndMarkAsFailed(error)
    } finally {
        removeContainer(GRADLE_CONTAINER)
    }
}

def archiveTestResults() {
    step([$class: "JUnitResultArchiver", testResults: "**/build/test-results/TEST-*.xml"])
}

def archiveIntegrationTestResults() {
    step([$class: "JUnitResultArchiver", testResults: "**/build/integrationTest-results/TEST-*.xml"])
}

def archiveEndToEndTestResults() {
    step([$class: "JUnitResultArchiver", testResults: "**/build/e2eTest-results/TEST-*.xml"])
}

def stopWhenFailures() {
    if (currentBuild.result != null && currentBuild.result != "STABLE" && currentBuild.result != "stable") {
        error "There are failures in the current stage. The current build will be stopped."
    }
}

def stashWorkspace() {
    stash includes: '**/*', name: 'workspace', useDefaultExcludes: false
}

def unstashWorkspace() {
    unstash "workspace"
}

/********** Basic Docker functions **********/
def buildDBImage() {
    sh "docker build -t ${DB_IMAGE} codepop/ops/db/"
}

def buildAppImage() {
    sh "cp ./codepop/dev/build/libs/codepop-${VERSION_NUMBER}.jar ./codepop/ops/codepop/codepop.jar"
    sh "docker build -t ${APP_IMAGE} codepop/ops/codepop/"
}

def startContainer(imageName, containerName, network, options) {
    ensureContainerIsNotRunning(containerName)
    sh "docker run -d --net ${network} --name ${containerName} ${options} ${imageName}"
}

def startGradleContainer(gradleContainerName, options) {
    def currentDir = pwd()
    ensureContainerIsNotRunning(gradleContainerName)
    sh "docker run -td \
			--net ${JENKINS_NETWORK} \
			-v $currentDir:/usr/src \
			-v /var/jenkins_home/.gradle:/root/.gradle \
			-w /usr/src/codepop/dev \
			${options} \
			--name ${gradleContainerName} ${GRADLE_IMAGE}"
}

def execGradle(gradleContainerName, task) {
    sh "docker exec ${gradleContainerName} gradle ${task}"
}

def ensureContainerIsNotRunning(name) {
    try {
        def containerIsRunning = executeCommand("docker ps -f 'name=${name}' -q")
        println "container is running: ${containerIsRunning}"
        if (containerIsRunning) {
            removeContainer(name)
        }
    } catch (err) {
        printErrorAndMarkAsFailed(err)
    }
}

def removeContainer(name) {
    try {
        sh "docker rm -f -v ${name}"
    } catch (err) {
        printErrorAndMarkAsFailed(err)
    }
}

def cleanImages() {
    cleanImageIfExists(DB_IMAGE)
    cleanImageIfExists(APP_IMAGE)
}

def cleanImageIfExists(imageName) {
    if (executeCommand("docker images -q $imageName")) {
        sh "docker rmi $imageName"
    }
}

def tarImages() {
    sh "docker save -o codepop-${VERSION_SUFFIX}.tar ${APP_IMAGE}"
    sh "docker save -o codepop-db-${VERSION_SUFFIX}.tar ${DB_IMAGE}"
}

def copyImagesToPRD() {
    scpToPRD("codepop-${VERSION_SUFFIX}.tar", "/tmp/codepop-${VERSION_SUFFIX}.tar")
    scpToPRD("codepop-db-${VERSION_SUFFIX}.tar", "/tmp/codepop-db-${VERSION_SUFFIX}.tar")
    cleanImages()
}

def importImagesOnPRD() {
    sshToPRD("docker load -i /tmp/codepop-${VERSION_SUFFIX}.tar")
    sshToPRD("docker load -i /tmp/codepop-db-${VERSION_SUFFIX}.tar")
    echo "Images on PRD:"
    sshToPRD("docker images")
}

def cleanUpTarsOnPRD(){
    sshToPRD("rm /tmp/codepop*.tar")
}

def stopPRD() {
    def containerIsRunning = executeCommandOnPRD("docker ps -f 'name=codepop' -q")
    if (containerIsRunning) {
        sshToPRD("docker rm -f -v codepop")
    }
    containerIsRunning = executeCommandOnPRD("docker ps -f 'name=codepop_db' -q")
    if (containerIsRunning) {
        sshToPRD("docker rm -f -v codepop_db")
    }
}

def removeImagesAndContainersOnPRD(){
    try {sshToPRD("docker rm -f \$(docker ps -a -q)")}catch(err){}
    try {sshToPRD("docker rmi -f \$(docker images -q)")}catch(err){}
}

def startPRDNetwork(){
    try { sshToPRD("docker network rm ${PRD_NETWORK}") } catch(err){}
    sshToPRD("docker network create -d bridge ${PRD_NETWORK}")
}

def runPRD() {
    sshToPRD("docker run -td \
				--net ${PRD_NETWORK} \
				-p 27017:27017 \
                --name codepop_db ${DB_IMAGE}")

    sshToPRD("docker run -td \
				--net ${PRD_NETWORK} \
				-p 8080:8080 \
				-e spring.data.mongodb.host=codepop_db.${PRD_NETWORK} \
                --name codepop ${APP_IMAGE}")
}

/********** Util functions **********/
def sshToPRD(String command) {
    //sh "ssh -i ${SSH_IDENTITY_FILE} ${PRD_USER}@${PRD_HOST} '${command}'"
    sh "ssh ${PRD_USER}@${PRD_HOST} '${command}'"
}

def scpToPRD(String localFile, String destFile) {
    //sh "scp -i ${SSH_IDENTITY_FILE} ${localFile} ${PRD_USER}@${PRD_HOST}:${destFile}"
    sh "scp ${localFile} ${PRD_USER}@${PRD_HOST}:${destFile}"
}

def executeCommandOnPRD(command) {
    //sh "ssh -i ${SSH_IDENTITY_FILE} ${PRD_USER}@${PRD_HOST} '${command}' > commandOutput.txt"
    sh "ssh ${PRD_USER}@${PRD_HOST} '${command}' > commandOutput.txt"
    def commandOutput = readFile("commandOutput.txt").trim()
    sh "rm commandOutput.txt"
    commandOutput
}

def executeCommand(command) {
    sh "${command} > commandOutput.txt"
    def commandOutput = readFile("commandOutput.txt").trim()
    sh "rm commandOutput.txt"
    commandOutput
}

def printErrorAndMarkAsFailed(error) {
    println "******************************* WORKFLOW ERROR *******************************"
    println "${error}"
    println "******************************************************************************"
    currentBuild.result = 'FAILURE'
    throw error
}
