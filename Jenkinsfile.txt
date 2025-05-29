pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn -f backend/pom.xml clean package'
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    docker.build("myuser/crrypto-app:${BUILD_NUMBER}")
                }
            }
        }
    }
}
