def dockerBuildTag = 'latest'
def mobileDepositUiImage = null
def buildVersion = null
def short_commit = null
def dockerTag = null

env.DOCKER_HUB_USER = 'beedemo'
env.DOCKER_CREDENTIAL_ID = 'docker-hub-beedemo'
properties([$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '1', daysToKeepStr: '', numToKeepStr: '5']])

node('default') {
    checkout scm
    stage('build') {
        gitShortCommit(7)
        container('maven-jdk8') {
            sh "mvn clean install -DskipTests -DGIT_COMMIT='${SHORT_COMMIT}' -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_URL=${BUILD_URL}"
        }

        //get new version of application from pom
        def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
        if (matcher) {
            buildVersion = matcher[0][1]
            echo "Released version ${buildVersion}"
        }
        matcher = null
        stash name: 'jar-dockerfile', includes: '**/target/*.jar,**/target/Dockerfile'
    }
    stage('functional-test') {
        try {
            parallel(
                "firefox": {
                    container(maven-jdk8) {
                        sh 'mvn -Dmaven.repo.local=/data/mvn/repo verify -DargLine="-Dtest.browser.name=firefox -Dtest.browser.version=58.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8081"'
                    }
                },

                "firefox-old": {
                    container(maven-jdk8) {
                        sh 'mvn -Dmaven.repo.local=/data/mvn/repo verify -DargLine="-Dtest.browser.name=firefox -Dtest.browser.version=47.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8082"'
                    }
                },
                "chrome": {
                    container(maven-jdk8) {
                        sh 'mvn -Dmaven.repo.local=/data/mvn/repo verify -DargLine="-Dtest.browser.name=chrome -Dtest.browser.version=65.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8083"'
                    }
                }, failFast: true
            )
        } catch(x) {
            //error
            throw x
        } finally {
            //capture results regardless of outcome
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
            archive "screenshot*.png"
            sh 'rm -f screenshot*'
        }
    }
}

if(env.BRANCH_NAME!="master") {//must check firefox manually
    stage('awaiting approval for staging') {
        //put input step outside of node so it doesn't tie up a slave
        timeout(time: 10, unit: 'MINUTES') {
            input 'Do the screenshots look good?'
        }
    }
}
    //docker tag to be used for build, push and run
    dockerTag = "${env.BUILD_NUMBER}-${SHORT_COMMIT}"
    //build image and deploy to staging
    stage('build docker image') {
        dockerBuildPush('beedemo/mobile-deposit-ui', 'kaniko-3', 'target/.') {
              unstash 'jar-dockerfile'
        }
    }

    stage('deploy to staging') {
        echo "TODO"
        //kubeDeploy('mobile-deposit-ui-stage', 'beedemo', "$dockerTag", "stage")
    }



if(env.BRANCH_NAME=="master") {//only deploy master branch to prod

    stage('awaiting approval for prod deployment') {
        //put input step outside of node so it doesn't tie up a slave
        timeout(time: 10, unit: 'MINUTES') {
            input 'UI Staged at http://bank.beedemo.net:82/deposit - Proceed with Production Deployment?'
        }
    }

   /* TODO - figure this out 
     stage('deploy to production') {
        node('docker-cloud') {
            sh "docker tag beedemo/mobile-deposit-ui-stage:${dockerTag} beedemo/mobile-deposit-ui:${dockerTag}"
            //use withDockerRegistry to make sure we are logged in to docker hub registry
            withDockerRegistry(registry: [credentialsId: 'docker-hub-beedemo']) {
              sh "docker push beedemo/mobile-deposit-ui:${dockerTag}"
            }
            kubeDeploy('mobile-deposit-ui', "beedemo", "$dockerTag", 80, 8080)
        }
    } */
}
