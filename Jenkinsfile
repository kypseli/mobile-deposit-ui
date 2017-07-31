def dockerBuildTag = 'latest'
def mobileDepositUiImage = null
def buildVersion = null
def short_commit = null

env.DOCKER_HUB_USER = 'beedemo'
env.DOCKER_CREDENTIAL_ID = 'docker-hub-beedemo'

if(env.BRANCH_NAME=="master"){
    properties([pipelineTriggers(triggers: [[$class: 'DockerHubTrigger', options: [[$class: 'TriggerOnSpecifiedImageNames', repoNames: ['beedemo/mobile-deposit-api'] as Set]]]]),
                [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5']]])
}


node('dind-compose') {
    checkout scm
    stage('build') {
        sh('git rev-parse HEAD > GIT_COMMIT')
        git_commit=readFile('GIT_COMMIT')
        short_commit=git_commit.take(7)
        sh 'docker run -i --rm -v "$PWD":/usr/src/mobile-deposit-ui -w /usr/src/mobile-deposit-ui maven:3.3-jdk-8 mvn -Dmaven.repo.local=/data/mvn/repo clean package -DskipTests'

        //get new version of application from pom
        def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
        if (matcher) {
            buildVersion = matcher[0][1]
            echo "Released version ${buildVersion}"
        }
        matcher = null
    }
    stage('functional-test') {
        try {
            sh "/sbin/ip route|awk \'/default/ { print \$3 }\'"
            sh 'docker-compose up -d'
            sh 'docker run -i --rm -p 8081:8081 --name deposit-ui -v "$PWD":/usr/src/mobile-deposit-ui -w /usr/src/mobile-deposit-ui maven:3.3-jdk-8 mvn -Dmaven.repo.local=/data/mvn/repo verify -DargLine="-Dserver.port=8081"'
        } catch(x) {
            //error
            throw x
        } finally {
            sh 'docker-compose down'
        }
    }
    //docker tag to be used for build, push and run
    def dockerTag = "${env.BUILD_NUMBER}-${short_commit}"
    //build image and deploy to staging
    stage('build docker image') {
        dir('target') {
            mobileDepositUiImage = docker.build "beedemo/mobile-deposit-ui:${dockerTag}"
        }
    }

    stage('deploy to staging') {
        //use withDockerRegistry to make sure we are logged in to docker hub registry
        withDockerRegistry(registry: [credentialsId: 'docker-hub-beedemo']) {
          mobileDepositUiImage.push()
        }
        dockerDeploy("docker-cloud","${DOCKER_HUB_USER}", 'mobile-deposit-ui', 82, 8080, "$dockerTag")
    }
}
stage 'awaiting approval'
//put input step outside of node so it doesn't tie up a slave
input 'UI Staged at http://52.40.148.113:82/deposit - Proceed with Production Deployment?'
stage 'deploy to production'
node('docker-cloud') {
    def dockerTag = "${env.BUILD_NUMBER}-${short_commit}"
    dockerDeploy("docker-cloud","${DOCKER_HUB_USER}", 'mobile-deposit-ui', 80, 8080, "$dockerTag")
}
