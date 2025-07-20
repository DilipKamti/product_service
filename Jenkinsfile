pipeline {
    agent any

    environment {
        IMAGE_NAME = "dilipkamti/product_service"
        IMAGE_TAG = "latest"
    }

    parameters {
        choice(name: 'PROFILE', choices: ['dev', 'prod'], description: 'Choose Spring Boot profile')

        booleanParam(name: 'DELETE_OLD_BUILDS', defaultValue: false, description: 'Delete old Docker images before building?')
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/DilipKamti/product_service.git'
            }
        }

        stage('Clean Old Docker Images') {
            when {
                expression { params.DELETE_OLD_BUILDS }
            }
            steps {
                script {
                    def oldImagesCmd = "docker images ${IMAGE_NAME} --format \"{{.Repository}}:{{.Tag}}\" | grep -v ${params.PROFILE}-${BUILD_NUMBER} | xargs -r docker rmi -f"
                    if (isUnix()) {
                        sh oldImagesCmd
                    } else {
                        bat "FOR /F \"tokens=*\" %%i IN ('docker images ${IMAGE_NAME} --format \"{{.Repository}}:{{.Tag}}\" ^| findstr /V ${params.PROFILE}-${BUILD_NUMBER}') DO docker rmi -f %%i"
                    }
                }
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
                // Add docker-compose or SSH deploy commands here
            }
        }
    }
// Post actions to clean up workspace
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Build and deployment successful!'
        }
        failure {
            echo 'Build or deployment failed!'
        }
    }
}