
def call(Map config = [:]) {

    def scanPath = config.scanPath ?: '.'

    def scanStatus = 0

    
stage('ESLint Setup') {
    sh '''
        set -eu
        npm install --prefix .tools/eslint --no-save --no-package-lock eslint@9.0.0
    '''
}


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
    --no-config-lookup \
    --no-ignore \
    --rule 'no-unused-vars: warn' \
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

        echo "ESLint exit status: ${scanStatus}"
sh 'cat eslint-report.html || true'

        if (scanStatus != 0) {
            error('ESLint scan failed. Check the build log and report.')
        }
    }
}
