
def call(Map config = [:]) {

    def cfg = [
        buildUser       : 'Anshul Kotiya',
        slackChannel    : '#p19-slack-notification',
        // slackCredId     : 'slack-notification',
        emailRecipients : 'anshul.kotiya.snaatak@mygurukulam.co'
    ] + config

    def buildStatus = currentBuild.currentResult ?: 'SUCCESS'
    def isSuccess = buildStatus == 'SUCCESS'

    def message = """Build Status: ${buildStatus}
Triggered By: ${cfg.buildUser}
Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Console Output: ${env.BUILD_URL}console"""

    // Slack Notification
    // slackSend(
    //     channel: cfg.slackChannel,
    //     tokenCredentialId: cfg.slackCredId,
    //     color: isSuccess ? 'good' : 'danger',
    //     message: "${isSuccess ? 'SUCCESS' : 'FAILURE'}: ${message}"
    // )

    // Plain-text Email Notification
    emailext(
        to: cfg.emailRecipients,
        subject: "${isSuccess ? 'SUCCESS' : 'FAILURE'}: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
        body: message,
        mimeType: 'text/plain'
    )
}
