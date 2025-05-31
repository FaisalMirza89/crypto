pipeline {
    agent any

    tools {
        jdk 'jdk17'           // configured JDK name in Jenkins
        maven 'maven3'        // configured Maven name in Jenkins
    }

    environment {
        DOCKER_IMAGE = "kubesarforaj/crypto-web"
        DOCKER_TAG = "latest"
        SONARQUBE_ENV = "SonarQube"        // Name of SonarQube server in Configure System
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

        stage('SonarQube Analysis') {
            environment {
                scannerHome = tool 'SonarScanner'  // your SonarScanner tool name
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
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                        sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build and Docker push succeeded!"
        }
        failure {
            echo "❌ Build failed."
        }
    }
}
