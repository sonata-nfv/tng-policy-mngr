pipeline {
    agent any
    stages {
        stage('Build, Test and Package') {
            steps {
                echo 'Building..'
                sh '.pipeline/build/build.sh'
            }
        }
        stage('Publish') {
           steps {
               echo 'publish to docker local registry..'
               sh '.pipeline/build/publish.sh'
           }
       }
    }
} 
