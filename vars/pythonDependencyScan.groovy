
def call(Map config = [:]) {
    def scanPath = config.scanPath ?: '.'
    def severity = config.severity ?: 'HIGH,CRITICAL'

    stage('Python Dependency Scanning') {
        echo 'Scanning supported dependencies using Trivy'

        sh """
            set -eu

            if command -v trivy >/dev/null 2>&1; then
                TRIVY=trivy
            else
                echo 'Trivy is not installed on this Jenkins agent.'
                echo 'Trivy setup will be added in the next step.'
                exit 1
            fi

            "\$TRIVY" fs \
                --scanners vuln \
                --severity '${severity}' \
                --format json \
                --output trivy-dependency-report.json \
                '${scanPath}'

            "\$TRIVY" fs \
                --scanners vuln \
                --severity '${severity}' \
                --format table \
                --output trivy-dependency-report.txt \
                '${scanPath}'

            cat trivy-dependency-report.txt
        """

        archiveArtifacts(
            artifacts: 'trivy-dependency-report.json,trivy-dependency-report.txt',
            fingerprint: true
        )
    }
}
