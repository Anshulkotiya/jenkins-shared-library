
def call(Map config = [:]) {

    def scanPath = config.get('scanPath', 'src')
    def nodeTool = config.get('nodeTool')

    nodejs(nodeTool) {

        stage('Check ESLint') {
            sh '''
                node --version
                npm --version
                ./node_modules/.bin/eslint --version
            '''
        }

        stage('Configure ESLint') {
            script {
                if (!fileExists('.eslintrc.json')) {
                    writeFile(
                        file: '.eslintrc.json',
                        text: '''{
  "env": {
    "browser": true,
    "es6": true,
    "node": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended"
  ],
  "plugins": [
    "react",
    "react-hooks"
  ],
  "rules": {
    "no-unused-vars": "warn",
    "no-undef": "error",
    "eqeqeq": "warn",
    "no-unreachable": "error",
    "react/jsx-key": "warn",
    "react-hooks/rules-of-hooks": "error"
  }
}'''
                    )
                }
            }
        }

        stage('Static Code Analysis') {
            int scanStatus = 0

            withEnv(["SCAN_PATH=${scanPath}"]) {
                try {
                    sh '''
                        set +e

                        ./node_modules/.bin/eslint "$SCAN_PATH" \
                            --ext .js,.jsx \
                            --format json \
                            --output-file eslint-report.json
                        JSON_STATUS=$?

                        ./node_modules/.bin/eslint "$SCAN_PATH" \
                            --ext .js,.jsx \
                            --format stylish \
                            > eslint-report.txt 2>&1
                        TEXT_STATUS=$?

                        echo "JSON report exit code: $JSON_STATUS"
                        echo "Text report exit code: $TEXT_STATUS"
                        cat eslint-report.txt

                        if [ "$JSON_STATUS" -ne 0 ] || \
                           [ "$TEXT_STATUS" -ne 0 ]; then
                            exit 1
                        fi
                    '''
                } catch (Exception e) {
                    scanStatus = 1
                    echo "ESLint failed: ${e.getMessage()}"
                } finally {
                    archiveArtifacts(
                        artifacts: 'eslint-report.json,eslint-report.txt',
                        allowEmptyArchive: true,
                        fingerprint: false
                    )
                }
            }

            if (scanStatus != 0) {
                error('Static Code Analysis failed. Check the ESLint reports.')
            }
        }
    }
}
