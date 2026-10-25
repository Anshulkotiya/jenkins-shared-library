
def call(Map config = [:]) {

    def repoUrl = config.repoUrl ?: 'https://github.com/OT-MICROSERVICES/frontend.git'
    def branch = config.branch ?: 'main'
    def scanPath = config.scanPath ?: '.'
    def sonarProjectKey = config.sonarProjectKey ?: 'react-static-analysis'
    def sonarServer = config.sonarServer ?: 'SonarQube'
    def scannerTool = config.scannerTool ?: 'sonar-scanner'
    def slackChannel = config.slackChannel ?: '#p19-slack-notification'
    def emailRecipients = config.emailRecipients ?: 'anshul.kotiya.snaatak@mygurukulam.co'
    def buildUser = config.buildUser ?: 'Anshul Kotiya'

    try {

        stage('Checkout Code') {
            git branch: branch, url: repoUrl
        }

        eslintScan(
            scanPath: scanPath,
           
        )

        stage('SonarQube Static Analysis') {
            def scannerHome = tool scannerTool

            withSonarQubeEnv(sonarServer) {
                sh """
                    set -eu

                    "${scannerHome}/bin/sonar-scanner" \
                        -Dsonar.projectKey="${sonarProjectKey}" \
                        -Dsonar.sources=. \
                        -Dsonar.exclusions="node_modules/**,.tools/**,build/**,dist/**" \
                        -Dsonar.sourceEncoding=UTF-8
                """
            }
        }

    } finally {
        try {
            sendNotification(
                slackChannel: slackChannel,
                emailRecipients: emailRecipients,
                buildUser: buildUser
            )
        } catch (Exception e) {
            echo "Notification failed: ${e.message}"
        }
    }
}
