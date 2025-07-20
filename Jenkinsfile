pipeline {
    agent any

    environment {
        IMAGE_NAME = "dilipkamti/product_service"
        DOCKER_TAG_PREFIX = "v"
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
                    def oldImagesCmd = "docker images ${IMAGE_NAME} --format \"{{.Repository}}:{{.Tag}}\" | grep -v ${BUILD_NUMBER} | xargs -r docker rmi -f"
                    if (isUnix()) {
                        sh oldImagesCmd
                    } else {
                        bat "FOR /F \"tokens=*\" %%i IN ('docker images ${IMAGE_NAME} --format \"{{.Repository}}:{{.Tag}}\" ^| findstr /V ${BUILD_NUMBER}') DO docker rmi -f %%i"
                    }
                }
            }
        }

        stage('Build Maven Project') {
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

        stage('Determine Docker Image Version') {
            steps {
                script {
                    // Auto-incrementing Docker tag logic based on file
                    def versionFile = '.docker-version'
                    def currentVersion = '0.0'

                    if (fileExists(versionFile)) {
                        currentVersion = readFile(versionFile).trim()
                    }

                    def (major, minor) = currentVersion.tokenize('.').collect { it.toInteger() }
                    def newVersion = "${major}.${minor + 1}"
                    def versionTag = "${DOCKER_TAG_PREFIX}${newVersion}"

                    env.DOCKER_VERSION = versionTag
                    writeFile file: versionFile, text: newVersion
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def fullVersionTag = "${IMAGE_NAME}:${DOCKER_VERSION}"
                    def latestTag = "${IMAGE_NAME}:latest"

                    def buildCmd = "docker build -t ${fullVersionTag} -t ${latestTag} ."
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
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    script {
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
                    def fullVersionTag = "${IMAGE_NAME}:${DOCKER_VERSION}"
                    def latestTag = "${IMAGE_NAME}:latest"

                    def pushCmd = "docker push ${fullVersionTag} && docker push ${latestTag}"
                    if (isUnix()) {
                        sh pushCmd
                    } else {
                        bat pushCmd
                    }
                }
            }
        }

        stage('Deploy to Production (Optional)') {
            when {
                expression { params.PROFILE == 'prod' }
            }
            steps {
                echo "Deploying product_service in production mode with tag: ${DOCKER_VERSION}"
                // Add SSH or docker-compose deploy steps here if needed
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Build and deployment successful with Docker tag: ${DOCKER_VERSION}"
        }
        failure {
            echo "❌ Build or deployment failed!"
        }
    }
}
