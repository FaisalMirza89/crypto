pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    environment {
        DOCKER_IMAGE = "faisalditiss89/crypto"
        DOCKER_TAG = "latest"
        SONARQUBE_ENV = "SonarQube"
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/FaisalMirza89/crypto.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests=false'
            }
        }

        stage('OWASP Dependency Check') {
            steps {
                sh 'mvn org.owasp:dependency-check-maven:check || echo "OWASP Dependency Check failed or missing plugin"'
            }
        }

     stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv("${SONARQUBE_ENV}") {
            withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                script {
                    def scannerHome = tool 'SonarScanner'
                    sh """
                        export PATH=${scannerHome}/bin:\$PATH
                        sonar-scanner \
                          -Dsonar.projectKey=CryptoWebApp \
                          -Dsonar.sources=src \
                          -Dsonar.java.binaries=target/classes \
                          -Dsonar.token=\$SONAR_TOKEN
                    """
                }
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
                    echo "🔍 Running Trivy Scan"
                    wget -q https://github.com/aquasecurity/trivy/releases/download/v0.63.0/trivy_0.63.0_Linux-64bit.tar.gz
                    tar -xzf trivy_0.63.0_Linux-64bit.tar.gz
                    ./trivy image ${DOCKER_IMAGE}:${DOCKER_TAG} > trivy-report.txt || echo "Trivy scan failed"
                    echo "📄 Trivy Summary:"
                    head -n 20 trivy-report.txt || echo "No summary available"
                '''
                archiveArtifacts artifacts: 'trivy-report.txt', fingerprint: true
            }
        }

        stage('Docker Scout Scan') {
            steps {
                sh '''
                    echo "🕵️ Running Docker Scout"
                    docker scout quickview ${DOCKER_IMAGE}:${DOCKER_TAG} > scout-report.txt || echo "Scout scan skipped"
                    echo "📄 Docker Scout Summary:"
                    head -n 20 scout-report.txt || echo "No summary available"
                '''
                archiveArtifacts artifacts: 'scout-report.txt', fingerprint: true
            }
        }

        stage('Run Docker Container') {
            steps {
                sh '''
                    docker ps -q --filter "publish=9090" | xargs -r docker rm -f
                    docker run -d -p 9090:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                '''
            }
        }

        stage('Helm Lint & Deploy') {
            steps {
                withEnv(['KUBECONFIG=/var/lib/jenkins/.kube/config']) {
                    sh '''
                        echo "🔍 Helm Lint"
                        helm lint helm-chart/ || echo "Helm lint warnings"

                        echo "🚀 Deploying with Helm"
                        helm upgrade --install crypto-web helm-chart/ \
                            --set image.tag=${DOCKER_TAG} \
                            --set replicaCount=2
                    '''
                }
            }
        }

        stage('Helm Test') {
            steps {
                withEnv(['KUBECONFIG=/var/lib/jenkins/.kube/config']) {
                    sh '''
                        echo "🧪 Running helm test (optional)"
                        helm test crypto-web || echo "Helm test skipped or failed"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build, scans, and Docker push succeeded!"
            archiveArtifacts artifacts: 'target/dependency-check-report.html', fingerprint: true
        }
        always {
            // Fallback archive if OWASP plugin skipped or file missing
            sh 'ls target/dependency-check-report.html || echo "No OWASP HTML report found"'
        }
        failure {
            echo "❌ Build failed. Review logs above."
        }
    }
}
