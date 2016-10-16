def dockerBuildTag = 'latest'
def buildVersion = null
def mobileDepositUiImage = null
stage 'build'
node('docker-cloud') {
    //docker.withServer('tcp://127.0.0.1:1234'){ //run the following steps on this Docker host
            docker.image('kmadel/maven:3.3.3-jdk-8').inside() { //use this image as the build environment
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/cloudbees/mobile-deposit-ui.git']]])
                sh 'mvn -Dmaven.repo.local=/data/mvn/repo clean package'

                //get new version of application from pom
                def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
                if (matcher) {
                    buildVersion = matcher[0][1]
                    echo "Released version ${buildVersion}"
                }
                matcher = null
            }
    //}

    //build image and deploy to staging
    docker.withServer('tcp://52.27.249.236:3376', 'beedemo-swarm-cert') { //run following steps on our staging server
        stage 'build docker image'
        dir('target') {
            mobileDepositUiImage = docker.build "mobile-deposit-ui:${buildVersion}"
        }
        try {
            sh "docker stop mobile-deposit-ui-stage"
            sh "docker rm mobile-deposit-ui-stage"
        } catch (Exception _) {
            echo "no container to stop"
        }
        stage 'deploy to staging'
        mobileDepositUiImage.run("--name mobile-deposit-ui-stage -p 82:8080 --env='constraint:node==beedemo-swarm-master'")
    }
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
