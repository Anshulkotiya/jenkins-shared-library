
def call(Map config = [:]) {

    def scanPath = config.scanPath ?: '.'
    def eslintConfig = config.eslintConfig ?: 'eslint.config.mjs'
    def scanStatus = 0

    stage('ESLint Static Analysis') {

        scanStatus = sh(
            script: """
                set -eu

                if [ ! -x .tools/eslint/node_modules/.bin/eslint ]; then
                    echo "ERROR: ESLint is not installed in the workspace."
                    exit 1
                fi

                .tools/eslint/node_modules/.bin/eslint \
                    '${scanPath}' \
                    --config '${eslintConfig}' \
                    --format html \
                    --output-file eslint-report.html
            """,
            returnStatus: true
        )

        archiveArtifacts(
            artifacts: 'eslint-report.html',
            allowEmptyArchive: true,
            fingerprint: true
        )

        if (scanStatus != 0) {
            error('ESLint scan failed. Check the build log and report.')
        }
    }
}
