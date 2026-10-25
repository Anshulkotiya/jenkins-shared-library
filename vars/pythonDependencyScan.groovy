
def call(Map config = [:]) {
    def scanPath = config.scanPath ?: '.'
    def severity = config.severity ?: 'HIGH,CRITICAL'

    stage('Python Dependency Scanning') {
        echo 'Starting Trivy dependency scan...'

        sh 'trivy --version'

        sh """
            trivy fs \
                --scanners vuln \
                --severity ${severity} \
                --format json \
                --output trivy-dependency-report.json \
                ${scanPath}
        """

        archiveArtifacts(
            artifacts: 'trivy-dependency-report.json',
            fingerprint: true
        )

        echo 'Trivy dependency report archived successfully.'
    }
}
