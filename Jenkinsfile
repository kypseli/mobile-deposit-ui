library 'kypseli'
pipeline {
    options { 
        buildDiscarder(logRotator(numToKeepStr: '5')) 
        skipDefaultCheckout() 
    }
    agent none
    stages {
        stage('Build') {
            steps {
                mavenCacheBuild("mobile-deposit-api")
            }
        }
    }
}
