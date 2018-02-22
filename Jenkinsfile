pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh '.pipeline/build/build.sh'
            }
        }
    }
} 
