def call(Map config) {

    stage('Clone') {

        checkout scm

    }

    if(config.KEEP_APPROVAL_STAGE.toBoolean()) {

        stage('User Approval') {

            input(
                message: "Deploy to ${config.ENVIRONMENT} ?",
                ok: "Approve"
            )
        }
    }

    stage('Playbook Execution') {

        sh " ansible-playbook -i hosts.ini playbook.yml"
    }

    stage('Notification') {

        echo "${config.ACTION_MESSAGE}"
    }
}
