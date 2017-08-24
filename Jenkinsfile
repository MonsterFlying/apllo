node('master') {
    checkout scm
    stage('Build') {
        docker.image('maven:3.3.3').inside {
            sh 'mvn --version'
        }
    }
    stage('Deploy') {
        if (env.BRANCH_NAME == 'dev') {
            echo 'I only execute on the master branch'
        } else {
            echo 'I execute elsewhere'
        }
    }
}