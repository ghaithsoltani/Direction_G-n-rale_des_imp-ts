pipeline {
    agent any

    environment {
        DOCKERHUB_USER  = 'your-dockerhub-username'
        BACKEND_IMAGE   = "${DOCKERHUB_USER}/dgi-backend"
        FRONTEND_IMAGE  = "${DOCKERHUB_USER}/dgi-frontend"
        DOCKERHUB_CREDS = 'dockerhub-credentials'
        IMAGE_TAG       = "${env.GIT_COMMIT?.take(7) ?: 'latest'}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                checkout scm
            }
        }

        stage('Build & Push Backend') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                        docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} \
                                     -t ${BACKEND_IMAGE}:latest \
                                     ./dgi-backend-fixed

                        docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        docker push ${BACKEND_IMAGE}:latest
                    """
                }
            }
        }

        stage('Build & Push Frontend') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} \
                                     -t ${FRONTEND_IMAGE}:latest \
                                     ./dgi-frontend-fixed

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
                    cd /home/ghaith/Desktop/MyExperinsDevV1
                    docker-compose pull
                    docker-compose up -d
                """
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline succeeded! Tag: ${IMAGE_TAG}"
        }
        failure {
            echo "❌ Pipeline FAILED on branch ${env.BRANCH_NAME}"
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}