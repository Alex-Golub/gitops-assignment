pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = 'mrdrprof'
        APP_NAME = 'gitops-assignment'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        IMAGE_NAME = "${DOCKERHUB_USERNAME}/${APP_NAME}"
        REGISTRY_CREDINTIALS = 'dockerhub'
        DEPLOYMENT_LOCATION = 'helm-k8s-test-app/templates/deployment.yaml'
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

        stage('Updating deployment file') {
            steps {
                sh "cat ${DEPLOYMENT_LOCATION}"
                sh "sed -i 's/${APP_NAME}.*/${APP_NAME}:${IMAGE_TAG}/g' ${DEPLOYMENT_LOCATION}"
                sh "cat ${DEPLOYMENT_LOCATION}"
            }
        }

        stage('Update deployment and merge back to Master') {
            steps {
                script {
                    withCredentials([gitUsernamePassword(credentialsId: 'github', gitToolName: 'Default')]) {
                        sh """
                            git config --global user.name "jenkins"
                            git config --global user.email "jenkins@jenkins.com"
                            git switch -c development
                            git add .
                            git commit -m 'Updated deployment file by Jenkins'
                            git switch -
                            git push -u origin --all
                            """
                    }
                }
            }
        }

        // stage('MERGE to master branch') {
        //     cleanWs()
        //     checkout scm
        //     sh 'cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml'
        //     sh "sed '/imagetag/d' /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml > /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml  "
        //     sh "echo imagetag: ${BUILD_NUMBER} >> /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml"
        //     sh 'cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml > /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml'
        //     sh 'cat /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/values.yaml'
        //     sh 'rm /var/jenkins_home/workspace/gitops-assignment-pipeline/helm-k8s-test-app/temp.yaml'

        //     script {
        //         sshagent(credentials: ['git']) {
        //             sh """
        //                     git config user.email "jenkins@email.com"
        //                     git config user.name "MahMan Jenkins"
        //                     git checkout development
        //                     git add .
        //                     git commit -m 'development'
        //                     git checkout master
        //                     git merge development
        //                     git push -u origin --all
        //                 """
        //         }
        //     }
        // }
    }
}
