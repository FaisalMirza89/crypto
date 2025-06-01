pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = "kubesarforaj/crypto-web"
        DOCKER_TAG = "latest"
        SONARQUBE_ENV = "SonarQube"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/sarforajs/crypto.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests=false'
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                scannerHome = tool 'SonarScanner'
            }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv("${SONARQUBE_ENV}") {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=CryptoWebApp \
                            -Dsonar.sources=src \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        def customImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                        customImage.push()
                    }
                }
            }
        }

        stage('Trivy Scan') {
            steps {
                sh '''
                    wget https://github.com/aquasecurity/trivy/releases/download/v0.63.0/trivy_0.63.0_Linux-64bit.tar.gz
                    tar zxvf trivy_0.63.0_Linux-64bit.tar.gz
                    ./trivy image ${DOCKER_IMAGE}:${DOCKER_TAG} > trivy-report.txt
                '''
                archiveArtifacts artifacts: 'trivy-report.txt', fingerprint: true
            }
        }

        stage('Docker Scout Scan') {
            steps {
                sh 'docker scout quickview ${DOCKER_IMAGE}:${DOCKER_TAG} > scout-report.txt || echo "Scout scan skipped (Docker Scout not available)"'
                archiveArtifacts artifacts: 'scout-report.txt', fingerprint: true
            }
        }

        stage('Run Docker Container') {
            steps {
                sh '''
                    # Stop and remove any container using port 9090
                    docker ps -q --filter "publish=9090" | xargs -r docker rm -f

                    # Now run new container
                    docker run -d -p 9090:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    helm upgrade --install crypto-web helm-chart/ \
                    --set image.tag=${DOCKER_TAG} \
                    --set replicaCount=2
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Build, scans, and Docker push succeeded!"
            archiveArtifacts artifacts: 'target/dependency-check-report.html', fingerprint: true
        }
        failure {
            echo "❌ Build failed."
        }
    }
}
