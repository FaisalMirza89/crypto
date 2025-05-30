pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "kubesarforaj/crypto-web"
        DOCKER_TAG = "latest"
        SONARQUBE_ENV = "SonarQube" // Update if your SonarQube server is named differently
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/sarforajs/crypto.git'
            }
        }

        stage('Set up JDK') {
            tools {
                jdk 'jdk17' // Make sure this is configured in Jenkins tools
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests=false'
            }
        }

        stage('SonarQube Analysis') {
            environment {
                scannerHome = tool 'SonarScanner' // Update if you named it differently in Jenkins
            }
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=CryptoWebApp -Dsonar.sources=src -Dsonar.java.binaries=target/classes"
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

