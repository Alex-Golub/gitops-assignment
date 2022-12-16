pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'mrdrprof'
        APP_NAME = 'gitops-assignment'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        IMAGE_NAME = "${DOCKERHUB_USERNAME}/${APP_NAME}"
        REGISTRY_CREDINTIALS = 'dockerhub'
        VALUES_LOCATION = 'helm-k8s-test-app/values.yaml'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                script {
                    cleanWs()
                }
            }
        }

        stage('Checkout SCM') {
            steps {
                git(credentialsId: 'github', url: 'https://github.com/Alex-Golub/gitops-assignment', branch: 'master')
            }
        }

        stage('Docker Image Build and Push') {
            steps {
                script {
                    dockerImage = docker.build("${IMAGE_NAME}:${BUILD_NUMBER}")
                    withDockerRegistry(credentialsId: "${REGISTRY_CREDINTIALS}") {
                        dockerImage.push("${env.BUILD_NUMBER}")
                        dockerImage.push('latest')
                    }
                }
            }
        }

        stage('Delete Docker Images') {
            steps {
                sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG}"
                sh "docker rmi ${IMAGE_NAME}:latest"
            }
        }

        stage('Update docker image tag') {
            steps {
                sh "sed -i 's/tag: .*/tag: ${IMAGE_TAG}/g' ${VALUES_LOCATION}"
            }
        }
        
        stage('Commit to development branch') {
            steps {
                script {
                    withCredentials([gitUsernamePassword(credentialsId: 'github', gitToolName: 'Default')]) {
                        sh """
                            git config --global user.name "jenkins"
                            git config --global user.email "jenkins@jenkins.com"
                            git stash
                            git switch development
                            git stash pop
                            git add .
                            git commit -m 'Updated image tag by Jenkins'
                            git push -u origin --all
                            """
                    }
                }
            }
        }
    }
}
 
