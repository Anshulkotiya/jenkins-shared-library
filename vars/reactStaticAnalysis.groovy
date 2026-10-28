
def call(Map config = [:]) {

    def scanPath = config.get('scanPath', '.')
    def nodeTool = config.get('nodeTool')

    nodejs(nodeTool) {

        stage('Check ESLint') {
            sh '''
                node --version
                npm --version
              
                 if [ ! -x ./node_modules/.bin/eslint ]; then
                echo "ERROR: ESLint is not installed."
                exit 1
            fi
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
  "parserOptions": {
    "ecmaVersion": 2020,
    "sourceType": "module",
    "ecmaFeatures": {
      "jsx": true
    }
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
            withEnv(["SCAN_PATH=${scanPath}"]) {
                sh '''
                    set +e

                    mkdir -p reports

                    ./node_modules/.bin/eslint "$SCAN_PATH" \
                        --ext .js,.jsx \
                        --format json \
                        --output-file reports/eslint-report.json
                    JSON_STATUS=$?

                    ./node_modules/.bin/eslint "$SCAN_PATH" \
                        --ext .js,.jsx \
                        --format stylish \
                        > reports/eslint-report.txt 2>&1
                    TEXT_STATUS=$?

                    echo "JSON report exit code: $JSON_STATUS"
                    echo "Text report exit code: $TEXT_STATUS"
                    cat eslint-report.txt

                    echo "ESLint scan completed. Reports will be archived."
                    exit 0
                '''
            }

            archiveArtifacts(
                artifacts: 'eslint-report.json,eslint-report.txt',
                allowEmptyArchive: true,
                fingerprint: false
            )
        }
    }
}

