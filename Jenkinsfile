pipeline {
    agent any

    triggers {
        // Ejecuta el pipeline periodicamente
        cron('0 2 * * *') //Todos los días a las 2:00 AM
    }

    tools {
        // Configuración de herramientas
        maven 'maven' 
    }

    stages {
        stage('Checkout') {
            steps {
                // Descarga el código del repositorio
                checkout scm
            }
        }

        stage('Compilación') {
            steps {
                echo 'Compilando la aplicación Spring Boot...'
                sh 'mvn clean compile'
            }
        }

        stage('Tests Unitarios') {
            steps {
                echo 'Ejecutando tests con JUnit...'
                sh 'mvn test -P unit-tests'
            }
        }

        stage('Empaquetado (JAR)') {
            steps {
                echo 'Generando el archivo .jar ejecutable...'
                sh 'mvn package -DskipTests'
            }
        }
    }
    
    post {
        always {
            echo 'Limpiando el espacio de trabajo...'
            cleanWs()
        }
        success {
            echo '¡El Pipeline ha finalizado con éxito!'
        }
        failure {
            echo 'Algo ha fallado en el proceso.'
        }
    }
}