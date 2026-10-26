
def call(Map config = [:]) {

    def scanPath = config.get('scanPath', 'src')

    stage('Check ESLint') {
        sh 'npx eslint --version'
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
        sh """
            set +e

            npx eslint '${scanPath}' --ext .js,.jsx \
                --format json \
                --output-file eslint-report.json

            ESLINT_STATUS=\$?

            npx eslint '${scanPath}' --ext .js,.jsx \
                --format stylish \
                > eslint-report.txt 2>&1

            echo "ESLint exit code: \$ESLINT_STATUS"
            cat eslint-report.txt
            exit 0
        """

        archiveArtifacts(
            artifacts: 'eslint-report.json,eslint-report.txt',
            allowEmptyArchive: true,
            fingerprint: false
        )
    }
}
