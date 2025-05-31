pipeline {
    agent any

    tools {
        jdk 'jdk17'           // configured JDK name in Jenkins
        maven 'maven3'        // configured Maven name in Jenkins
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
                    wget https://github.com/aquasecurity/trivy/releases/latest/download/trivy_0.50.1_Linux-64bit.tar.gz
                    tar zxvf trivy_0.50.1_Linux-64bit.tar.gz
                    sudo mv trivy /usr/local/bin/
                    trivy image kubesarforaj/crypto-web:latest > trivy-report.txt
                '''
                archiveArtifacts artifacts: 'trivy-report.txt', fingerprint: true
            }
        }

        stage('Docker Scout Scan') {
            steps {
                sh 'docker scout quickview kubesarforaj/crypto-web:latest > scout-report.txt || echo "Scout scan skipped (Docker Scout not available)"'
                archiveArtifacts artifacts: 'scout-report.txt', fingerprint: true
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    sh 'docker rm -f crypto-web || true'
                    sh "docker run -d --name crypto-web -p 9090:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build and Docker push succeeded!"
            archiveArtifacts artifacts: 'target/dependency-check-report.html', fingerprint: true
        }
        failure {
            echo "❌ Build failed."
        }
    }
}
