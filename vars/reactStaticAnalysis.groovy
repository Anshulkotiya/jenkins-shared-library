
def call(Map config = [:]) {

    def repoUrl = config.get(
        'repoUrl',
        'https://github.com/OT-MICROSERVICES/frontend.git'
    )

    def branchName = config.get('branch', 'main')
    def nodeTool = config.get('nodeTool', 'NodeJS-20')

    pipeline {
        agent any

        tools {
            nodejs "${nodeTool}"
        }

        stages {
            stage('Checkout Code') {
                steps {
                    git branch: "${branchName}",
                        url: "${repoUrl}"
                }
            }

            stage('Install Dependencies') {
                steps {
                    sh 'node --version'
                    sh 'npm --version'
                    sh 'npm install'

                    sh '''
                        npm install --no-save --legacy-peer-deps \
                        eslint@5.12.0 \
                        eslint-plugin-react@7.37.5 \
                        eslint-plugin-react-hooks@4.6.2
                    '''
                }
            }

            stage('Create ESLint Configuration') {
                steps {
                    script {
                        if (!fileExists('.eslintrc.json')) {
                            writeFile file: '.eslintrc.json',
                                text: '''{
  "env": {
    "browser": true,
    "es6": true,
    "node": true
  },
  "parserOptions": {
    "ecmaVersion": 2018,
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
  "settings": {
    "react": {
      "version": "detect"
    }
  },
  "rules": {
    "no-unused-vars": "warn",
    "no-undef": "error",
    "eqeqeq": "warn",
    "no-unreachable": "error",
    "no-duplicate-imports": "warn",
    "react/prop-types": "off",
    "react/react-in-jsx-scope": "off",
    "react/jsx-no-duplicate-props": "error",
    "react/jsx-key": "warn",
    "react-hooks/rules-of-hooks": "error",
    "react-hooks/exhaustive-deps": "warn"
  }
}'''
                        }
                    }
                }
            }

            stage('Check ESLint') {
                steps {
                    sh 'npx eslint --version'
                }
            }

            stage('Static Code Analysis') {
                steps {
                    sh '''
                        npx eslint src --ext .js,.jsx \
                        --format json \
                        --output-file eslint-report.json || true
                    '''
                }
                post {
                    always {
                        archiveArtifacts artifacts: 'eslint-report.json',
                            allowEmptyArchive: true
                    }
                }
            }
        }
    }
}
