pipeline {
    agent any

    environment {
        // Docker Hub repo names — change to your Docker Hub username
        DOCKERHUB_USER      = 'your-dockerhub-username'
        BACKEND_IMAGE       = "${DOCKERHUB_USER}/dgi-backend"
        FRONTEND_IMAGE      = "${DOCKERHUB_USER}/dgi-frontend"

        // Jenkins credential IDs
        DOCKERHUB_CREDS     = 'dockerhub-credentials'
        GITHUB_CREDS        = 'github-token'

        // Image tag = Git commit short SHA
        IMAGE_TAG           = "${env.GIT_COMMIT?.take(7) ?: 'latest'}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('dgi-backend-fixed') {
                    echo 'Building Spring Boot JAR...'
                    sh '''
                        docker run --rm \
                          -v "$(pwd)":/app \
                          -w /app \
                          maven:3.9-eclipse-temurin-21 \
                          mvn clean package -DskipTests
                    '''
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('dgi-frontend-fixed') {
                    echo 'Building Angular app...'
                    sh '''
                        docker run --rm \
                          -v "$(pwd)":/app \
                          -w /app \
                          node:20-alpine \
                          sh -c "npm ci && npm run build"
                    '''
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Building Docker images...'
                sh """
                    docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} \
                                 -t ${BACKEND_IMAGE}:latest \
                                 ./dgi-backend-fixed

                    docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} \
                                 -t ${FRONTEND_IMAGE}:latest \
                                 ./dgi-frontend-fixed
                """
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                        docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push ${BACKEND_IMAGE}:latest

                        docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                        docker push ${FRONTEND_IMAGE}:latest

                        docker logout
                    """
                }
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying with docker-compose...'
                sh """
                    docker-compose down
                    IMAGE_TAG=${IMAGE_TAG} docker-compose up -d
                """
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded! Images pushed: ${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline FAILED on branch ${env.BRANCH_NAME}"
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}