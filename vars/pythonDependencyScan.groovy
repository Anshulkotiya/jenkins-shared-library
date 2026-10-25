
def call(Map config = [:]) {
    def scanPath = config.scanPath ?: '.'
    def severity = config.severity ?: 'HIGH,CRITICAL'

    stage('Python Dependency Scanning') {
        echo 'Starting Trivy dependency scan...'

        withEnv([
            "TRIVY_SCAN_PATH=${scanPath}",
            "TRIVY_SEVERITY=${severity}"
        ]) {
            sh '''
                set -eu

                TRIVY_DIR="$WORKSPACE/.tools/trivy"
                TRIVY_LOCAL="$TRIVY_DIR/trivy"

                if command -v trivy >/dev/null 2>&1; then
                    TRIVY_BIN="$(command -v trivy)"
                    echo "Using installed Trivy"

                elif [ -x "$TRIVY_LOCAL" ]; then
                    TRIVY_BIN="$TRIVY_LOCAL"
                    echo "Using workspace Trivy"

                else
                    echo "Trivy not found. Downloading locally..."

                    command -v curl >/dev/null
                    command -v tar >/dev/null

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

                    if [ -z "$VERSION" ]; then
                        echo "Could not determine Trivy version"
                        exit 1
                    fi

                    mkdir -p "$TRIVY_DIR"

                    ARCHIVE="trivy_${VERSION}_Linux-${TRIVY_ARCH}.tar.gz"

                    curl -fsSL \
                        "https://github.com/aquasecurity/trivy/releases/download/v${VERSION}/${ARCHIVE}" \
                        -o "$TRIVY_DIR/trivy.tar.gz"

                    tar -xzf "$TRIVY_DIR/trivy.tar.gz" \
                        -C "$TRIVY_DIR" trivy

                    chmod +x "$TRIVY_LOCAL"
                    rm -f "$TRIVY_DIR/trivy.tar.gz"

                    TRIVY_BIN="$TRIVY_LOCAL"
                fi

                "$TRIVY_BIN" --version

                "$TRIVY_BIN" fs \
                    --scanners vuln \
                    --severity "$TRIVY_SEVERITY" \
                    --format json \
                    --output trivy-dependency-report.json \
                    "$TRIVY_SCAN_PATH"
            '''
        }

      
        archiveArtifacts(
            artifacts: 'trivy-dependency-report.txt',
            fingerprint: true
        )

        echo 'Trivy table report archived successfully.'

    }
}
