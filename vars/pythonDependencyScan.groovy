
def call(Map config = [:]) {
    def scanPath = config.scanPath ?: '.'
    def severity = config.severity ?: 'HIGH,CRITICAL'

    stage('Install Trivy') {
        sh '''
            if ! command -v trivy >/dev/null 2>&1; then
                echo "Trivy not found. Installing Trivy..."

                sudo apt-get update
                sudo apt-get install -y wget gnupg

                wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key \
                    | sudo gpg --dearmor -o /usr/share/keyrings/trivy.gpg

                echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" \
                    | sudo tee /etc/apt/sources.list.d/trivy.list

                sudo apt-get update
                sudo apt-get install -y trivy
            else
                echo "Trivy is already installed."
            fi

            trivy --version
        '''
    }

    stage('Python Dependency Scanning') {
        sh """
            trivy fs \
                --scanners vuln \
                --severity '${severity}' \
                --format json \
                --output trivy-dependency-report.json \
                '${scanPath}'
        """

        archiveArtifacts(
            artifacts: 'trivy-dependency-report.json',
            fingerprint: true
        )
    }
}
