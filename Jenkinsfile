def dockerBuildTag = 'latest'
def mobileDepositUiImage = null
def buildVersion = null
def short_commit = null
stage 'build'
node('docker-cloud') {
    docker.image('kmadel/maven:3.3.3-jdk-8').inside() { //use this image as the build environment
        checkout scm
        sh('git rev-parse HEAD > GIT_COMMIT')
        git_commit=readFile('GIT_COMMIT')
        short_commit=git_commit.take(7)
        sh 'mvn -Dmaven.repo.local=/data/mvn/repo clean package'

        //get new version of application from pom
        def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
        if (matcher) {
            buildVersion = matcher[0][1]
            echo "Released version ${buildVersion}"
        }
        matcher = null
    }

    //build image and deploy to staging
    stage 'build docker image'
    def dockerTag = "${env.BUILD_NUMBER}-${short_commit}"
    dir('target') {
        mobileDepositUiImage = docker.build "mobile-deposit-ui:${dockerTag}"
    }

    //use withDockerRegistry to make sure we are logged in to docker hub registry
    withDockerRegistry(registry: [credentialsId: 'docker-hub-beedemo']) {
      mobileDepositUiImage.push()
    }
    stage 'deploy to staging'
    dockerDeploy("beedemo", 'mobile-deposit-ui', 82, 8080, "$dockerTag")

    docker.image('kmadel/maven:3.3.3-jdk-8').inside() {
        stage 'functional-test'
        sh 'mvn -Dmaven.repo.local=/data/mvn/repo verify'
    }
}
stage 'awaiting approval'
//put input step outside of node so it doesn't tie up a slave
input 'UI Staged at http://52.27.249.236:82/deposit - Proceed with Production Deployment?'
stage 'deploy to production'
node('docker-cloud') {
    docker.withServer('tcp://52.27.249.236:3376', 'beedemo-swarm-cert'){
        try{
            sh "docker stop mobile-deposit-ui"
            sh "docker rm mobile-deposit-ui"
        } catch (Exception _) {
            echo "no container to stop"
        }
        mobileDepositUiImage.run("--name mobile-deposit-ui -p 80:8080 --env='constraint:node==beedemo-swarm-master'")
        //sh 'curl http://webhook:58f11cf04cecbe5633031217794eda89@jenkins.beedemo.net/mobile-team/docker-traceability/submitContainerStatus --data-urlencode inspectData="$(docker inspect mobile-deposit-ui)"'
    }

}