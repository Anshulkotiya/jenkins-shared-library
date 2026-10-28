
def call(Map config = [:]) {

    def scanPath = config.get('scanPath', '.')
    def nodeTool = config.get('nodeTool')

    nodejs(nodeTool) {

        stage('Check and install ESLint') {
            sh '''
 set -e

        if [ -x .ci-tools/node_modules/.bin/eslint ]; then
            echo "ESLint is already installed."
        else
            echo "ESLint not found. Installing ESLint tools..."

            mkdir -p .ci-tools
            cd .ci-tools

            if [ ! -f package.json ]; then
                npm init -y
            fi

           npm install --verbose --no-audit --no-fund \
    eslint@8.57.0 \
    eslint-plugin-react@7.34.1 \
    eslint-plugin-react-hooks@4.6.0
        fi

        cd "$WORKSPACE"

        .ci-tools/node_modules/.bin/eslint --version
   
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

            .ci-tools/node_modules/.bin/eslint "$SCAN_PATH" \
                --ext .js,.jsx \
                --resolve-plugins-relative-to .ci-tools \
                --format json \
                --output-file reports/eslint-report.json
            JSON_STATUS=$?

            .ci-tools/node_modules/.bin/eslint "$SCAN_PATH" \
                --ext .js,.jsx \
                --resolve-plugins-relative-to .ci-tools \
                --format stylish \
                > reports/eslint-report.txt 2>&1
            TEXT_STATUS=$?

            echo "JSON report exit code: $JSON_STATUS"
            echo "Text report exit code: $TEXT_STATUS"
            cat reports/eslint-report.txt

            echo "ESLint scan completed."
            exit 0
                '''
            }

            archiveArtifacts(
                artifacts: 'reports/eslint-report.json,reports/eslint-report.txt',
                allowEmptyArchive: true,
                fingerprint: false
            )
        }
    }
}

