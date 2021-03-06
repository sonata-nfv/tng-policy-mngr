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
    stage('Deployment in Pre-Integration') {
      parallel {
        stage('Deployment in Pre-Integration') {
          steps {
            echo 'Deploying in Pre-integration...'
          }
        }
        stage('Deploying') {
          when{
            not{
              branch 'master'
            }        
          } 
          steps {
            sh 'rm -rf tng-devops || true'
            sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
            dir(path: 'tng-devops') {
              sh 'ansible-playbook roles/sp.yml -i environments -e "target=pre-int-sp-ath.5gtango.eu component=policy-manager"'

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
            sh 'docker tag registry.sonata-nfv.eu:5000/tng-policy-mngr:latest registry.sonata-nfv.eu:5000/tng-policy-mngr:int'
            sh 'docker push  registry.sonata-nfv.eu:5000/tng-policy-mngr:int'
            sh 'rm -rf tng-devops || true'
            sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
            dir(path: 'tng-devops') {sh 'ansible-playbook roles/sp.yml -i environments -e "target=int-sp component=policy-manager"'}
          }
        }
      }
    }
    stage('Promoting release v5.0') {
        when {
            branch 'v5.0'
        }
        stages {
            stage('Generating release') {
                steps {
                    sh 'docker tag registry.sonata-nfv.eu:5000/tng-policy-mngr:latest registry.sonata-nfv.eu:5000/tng-policy-mngr:v5.0'
                    sh 'docker tag registry.sonata-nfv.eu:5000/tng-policy-mngr:latest sonatanfv/tng-policy-mngr:v5.0'
                    sh 'docker push registry.sonata-nfv.eu:5000/tng-policy-mngr:v5.0'
                    sh 'docker push sonatanfv/tng-policy-mngr:v5.0'
                }
            }
            stage('Deploying in v5.0 servers') {
                steps {
                    sh 'rm -rf tng-devops || true'
                    sh 'git clone https://github.com/sonata-nfv/tng-devops.git'
                    dir(path: 'tng-devops') {
                    sh 'ansible-playbook roles/sp.yml -i environments -e "target=sta-sp-v5-0 component=policy-manager"'
                    sh 'ansible-playbook roles/vnv.yml -i environments -e "target=sta-vnv-v5-0 component=policy-manager"'
                    }
                }
            }
        }
    }
  }
}
