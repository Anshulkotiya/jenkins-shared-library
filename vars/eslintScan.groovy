
def call(Map config = [:]) {

    def scanPath = config.scanPath ?: '.'
    def eslintVersion = config.eslintVersion ?: '9.0.0'
    def scanStatus = 0

    stage('Install ESLint') {
        sh """
            set -eu

            command -v npm >/dev/null 2>&1 || {
                echo "ERROR: Node.js/npm is not installed."
                exit 1
            }

            mkdir -p .tools/eslint

            npm install \
                --prefix .tools/eslint \
                --no-save \
                --no-package-lock \
                eslint@${eslintVersion}
        """
    }

    stage('ESLint Static Analysis') {
        scanStatus = sh(
            script: """
                .tools/eslint/node_modules/.bin/eslint \
                    '${scanPath}' \
                    --format html \
                    --output-file eslint-report.html
            """,
            returnStatus: true
        )

        archiveArtifacts(
            artifacts: 'eslint-report.html',
            fingerprint: true
        )

        if (scanStatus != 0) {
            error("ESLint scan failed. Check eslint-report.html in build artifacts.")
        }
    }
}
