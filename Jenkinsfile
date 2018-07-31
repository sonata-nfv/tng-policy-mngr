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
            sh 'docker build -t registry.sonata-nfv.eu:5000/tng-policy-mngr:v4.0 .'
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
            sh 'docker push registry.sonata-nfv.eu:5000/tng-policy-mngr:v4.0'
          }
        }
      }
    }
    stage('Deployment in Integration') {
      parallel {
        stage('Deployment in Integration') {
          steps {
            echo 'Deploying in integration...'
          }
        }
        stage('Deploying') {
          steps {
            sh 'rm -rf tng-devops || true'
            sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
            dir(path: 'tng-devops') {
              sh 'ansible-playbook roles/sp.yml -i environments -e "target=sta-sp-v4.0  component=policy-manager"'
            }
            
          }
        }
      }
    }
    stage('Promoting containers to integration env') {
      when {
        branch 'master'
      }
      parallel {
        stage('Publishing containers to int') {
          steps {
            echo 'Promoting containers to integration'
          }
        }
        stage('tng-policy-mngr') {
          steps {
            sh 'docker tag registry.sonata-nfv.eu:5000/tng-policy-mngr:v4.0 registry.sonata-nfv.eu:5000/tng-policy-mngr:v4.0'
            sh 'docker push  registry.sonata-nfv.eu:5000/tng-policy-mngr:v4.0'
            sh 'rm -rf tng-devops || true'
            sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
            dir(path: 'tng-devops') {sh 'ansible-playbook roles/sp.yml -i environments -e "target=int-sp component=policy-manager"'}
          }
        }
      }
    }
  }
}
