
def call(Map config = [:]) {

    def scanPath = config.scanPath ?: '.'
    def eslintVersion = config.eslintVersion ?: '9.0.0'

    stage('Install ESLint') {
        sh '''
            set -eu

            if ! command -v npm >/dev/null 2>&1; then
                echo "ERROR: Node.js and npm are required."
                exit 1
            fi

            mkdir -p .tools/eslint
            npm install --prefix .tools/eslint \
                --no-save --no-package-lock \
                eslint@9.0.0
        '''
    }

    stage('ESLint Static Analysis') {
        withEnv([
            "SCAN_PATH=${scanPath}",
            "ESLINT_VERSION=${eslintVersion}"
        ]) {
            sh '''
                set -eu

                .tools/eslint/node_modules/.bin/eslint \
                    "$SCAN_PATH" \
                    --no-config-lookup \
                    --no-ignore \
                    --no-warn-ignored \
                    --format json \
                    --output-file eslint-report.json
            '''
        }

        archiveArtifacts(
            artifacts: 'eslint-report.json',
            allowEmptyArchive: true,
            fingerprint: true
        )
    }
}
