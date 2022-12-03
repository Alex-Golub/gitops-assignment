node {
   stage('Checkout SCM') {
     checkout scm
     sh "git rev-parse --abbrev-ref HEAD > GIT_BRANCH"
     git_branch = readFile('GIT_BRANCH').trim()
     echo git_branch
   }

   stage('Docker build/push ') {
     docker.withRegistry('https://index.docker.io/v1/','dockerhub') {
       def app = docker.build("mrdrprof/gitops-assignment:${BUILD_NUMBER}", '.').push()
     }
   }

   stage('MERGE to master branch') {
      cleanWs()
      checkout scm
      sh "cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml"
      sh "sed '/imagetag/d' /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml > /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml  "
      sh "echo imagetag: ${BUILD_NUMBER} >> /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml"
      sh "cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml > /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml"
      sh "cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml"
      sh "rm /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml"

      script {
                    sshagent(credentials:['git']) {
                        sh """
                            git config user.email "jenkins@email.com"
                            git config user.name "Jenkins"
                            git checkout development
                            git add .
                            git commit -m 'development'
                            git checkout master
                            git merge development
                            git push -u origin --all
                        """
                    }
                }
     }
 }
