library 'kypseli'
pipeline {
  options { 
    buildDiscarder(logRotator(numToKeepStr: '5')) 
    disableConcurrentBuilds()
  }
  agent { label 'default'}
  stages {
    stage('build') {
      steps {
        gitShortCommit(7)
        container('maven-jdk8') {
            sh "mvn clean install -DskipTests -DGIT_COMMIT='${SHORT_COMMIT}' -DBUILD_NUMBER=${BUILD_NUMBER} -DBUILD_URL=${BUILD_URL}"
        }
        stash name: 'jar-dockerfile', includes: '**/target/*.jar,**/target/Dockerfile'
      }
    }
    stage('functional-test') {
      failFast true
      parallel {
        stage('"firefox') {
          steps {
            container('maven-jdk8') {
                sh 'mvn verify -DargLine="-Dtest.connection.url=http://100.71.163.122:30306/wd/hub -Dtest.browser.name=firefox -Dtest.browser.version=58.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8081"'
            }
          }
        }

        stage('firefox-old') {
          steps {
            container('maven-jdk8') {
                sh 'mvn verify -DargLine="-Dtest.connection.url=http://100.71.163.122:30306/wd/hub -Dtest.browser.name=firefox -Dtest.browser.version=47.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8082"'
            }
          }
        }
        stage('chrome') {
          steps {
            container('maven-jdk8') {
                sh 'mvn verify -DargLine="-Dtest.connection.url=http://100.71.163.122:30306/wd/hub -Dtest.browser.name=chrome -Dtest.browser.version=65.0 -DBUILD_NUMBER=${BUILD_NUMBER} -Dserver.port=8083"'
            }
          }
        }
      }
    }

    //build image and deploy to staging
    stage('build docker image') {
      steps {
        dockerBuildPush('beedemo/mobile-deposit-ui', 'kaniko-1', 'target/.') {
              unstash 'jar-dockerfile'
        }
      }
    }

    stage('deploy to staging') {
      steps {
        echo "TODO"
        //kubeDeploy('mobile-deposit-ui-stage', 'beedemo', "$dockerTag", "stage")
      }
    }
  }
}
