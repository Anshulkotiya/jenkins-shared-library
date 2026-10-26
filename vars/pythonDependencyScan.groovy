def call(Map config = [:]) {
    // Default scan settings
    def scanPath = config.scanPath ?: '.'
    def severity = config.severity ?: 'HIGH,CRITICAL'

    stage('Python Dependency Scanning') {
        echo 'Starting Trivy dependency scan...'

        withEnv([
            "SCAN_PATH=${scanPath}",
            "SEVERITY=${severity}"
        ]) {
            sh '''
                set -eu

                # Download Trivy only if it is not available
                if command -v trivy >/dev/null 2>&1; then
                    TRIVY="trivy"
                else
                    mkdir -p .tools/trivy

                    # Reuse the workspace copy if available
                    if [ ! -x .tools/trivy/trivy ]; then
                        ARCH="$(uname -m)"

                        case "$ARCH" in
                            x86_64) TRIVY_ARCH="64bit" ;;
                            aarch64|arm64) TRIVY_ARCH="ARM64" ;;
                            *)
                                echo "Unsupported architecture: $ARCH"
                                exit 1
                                ;;
                        esac

                        VERSION="$(curl -fsSL \
                            https://api.github.com/repos/aquasecurity/trivy/releases/latest |
                            sed -n 's/.*"tag_name": "v\\([^"]*\\)".*/\\1/p' |
                            head -n 1)"

                        test -n "$VERSION"

                        curl -fsSL \
                            "https://github.com/aquasecurity/trivy/releases/download/v${VERSION}/trivy_${VERSION}_Linux-${TRIVY_ARCH}.tar.gz" \
                            -o .tools/trivy/trivy.tar.gz

                        tar -xzf .tools/trivy/trivy.tar.gz \
                            -C .tools/trivy trivy

                        chmod +x .tools/trivy/trivy
                        rm .tools/trivy/trivy.tar.gz
                    fi

                    TRIVY=".tools/trivy/trivy"
                fi

                # Generate JSON report
                "$TRIVY" fs \
                    --scanners vuln \
                    --severity "$SEVERITY" \
                    --format json \
                    --output trivy-dependency-report.json \
                    "$SCAN_PATH"

                # Generate readable table report
                "$TRIVY" fs \
                    --scanners vuln \
                    --severity "$SEVERITY" \
                    --format table \
                    --output trivy-dependency-report.txt \
                    "$SCAN_PATH"

                # Display table in Jenkins console
                cat trivy-dependency-report.txt
            '''
        }

        // Archive both reports in Jenkins
        archiveArtifacts(
            artifacts: 'trivy-dependency-report.json,trivy-dependency-report.txt',
            fingerprint: true
        )

        echo 'Dependency scan reports archived successfully.'
    }
}
