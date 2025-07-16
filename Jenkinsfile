pipeline {
    agent any

    environment {
        IMAGE_NAME = "dilipkamti/product_service"
        IMAGE_TAG = "latest"
    }

    parameters {
        choice(name: 'PROFILE', choices: ['dev', 'prod'], description: 'Choose Spring Boot profile')
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/DilipKamti/product_service.git'
            }
        }

        stage('Build') {
            steps {
                script {
                    def mvnCmd = "mvn clean package -DskipTests -Dspring.profiles.active=${params.PROFILE}"
                    if (isUnix()) {
                        sh mvnCmd
                    } else {
                        bat mvnCmd
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def fullTag = "${IMAGE_NAME}:${params.PROFILE}-${BUILD_NUMBER}"
                    def latestTag = "${IMAGE_NAME}:${IMAGE_TAG}"

                    def buildCmd = "docker build -t ${fullTag} -t ${latestTag} ."
                    if (isUnix()) {
                        sh buildCmd
                    } else {
                        bat buildCmd
                    }
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        def loginCmd = "echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin"
                        if (isUnix()) {
                            sh loginCmd
                        } else {
                            bat """echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin"""
                        }
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    def fullTag = "${IMAGE_NAME}:${params.PROFILE}-${BUILD_NUMBER}"
                    def latestTag = "${IMAGE_NAME}:${IMAGE_TAG}"

                    def pushCmd = "docker push ${fullTag} && docker push ${latestTag}"
                    if (isUnix()) {
                        sh pushCmd
                    } else {
                        bat pushCmd
                    }
                }
            }
        }

        stage('Optional: Deploy using Docker Compose') {
            when {
                expression { params.PROFILE == 'prod' }
            }
            steps {
                echo 'Deploying product_service in production mode...'
                // You can add docker-compose or remote SSH steps here
            }
        }
    }
}
