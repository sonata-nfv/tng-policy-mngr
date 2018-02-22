pipeline {
  agent any
  stages {
    stage('Container Build') {
      parallel {
        stage('Container Build') {
          steps {
            echo 'Building..'
          }
        }
        stage('Building tng-policy-mngr') {
          steps {
            sh 'mvn clean install'
            sh 'docker build -t registry.sonata-nfv.eu:5000/tng-policy-mngr .'
          }
        }
      }
    }
    stage('Containers Publication') {
      parallel {
        stage('Containers Publication') {
          steps {
            echo 'Publication of containers in local registry....'
          }
        }
        stage('Publishing tng-policy-mngr') {
          steps {
            sh 'docker push registry.sonata-nfv.eu:5000/tng-policy-mngr'
          }
        }
      }
    }
  }
}