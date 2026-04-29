pipeline {
    agent any

    tools {
        // Usa el nombre exacto que le pusiste en 'Global Tool Configuration'
        maven 'maven' 
    }
    environment {
        // Forzamos el idioma a nivel de sistema operativo del contenedor
        LANG = 'es_ES.UTF-8'
        LC_ALL = 'es_ES.UTF-8'
        MAVEN_OPTS = '-Duser.language=es -Duser.country=ES'
    }

    stages {
        stage('Checkout') {
            steps {
                // Esto descarga el código de tu repositorio
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
        }
        success {
            echo '¡El Pipeline ha finalizado con éxito!'
        }
        failure {
            echo 'Algo ha fallado en el proceso.'
        }
    }
}