
def call(Map config = [:]) {

  def scanPath = config.get('scanPath', 'src')

            stage('Check ESLint') {
                steps {
                    sh 'npx eslint --version'
                }
            }

            stage('Static Code Analysis') {
                steps {
                    sh '''
                        set +e

                        npx eslint src --ext .js,.jsx \
                            --format json \
                            --output-file eslint-report.json

                        ESLINT_STATUS=$?

                        npx eslint src --ext .js,.jsx \
                            --format stylish \
                            > eslint-report.txt 2>&1

                        echo "ESLint exit code: ${ESLINT_STATUS}"
                        cat eslint-report.txt || true

                        exit 0
                    '''
                }
                post {
                    always {
                        archiveArtifacts(
                            artifacts: 'eslint-report.json,eslint-report.txt',
                            allowEmptyArchive: true,
                            fingerprint: false
                        )
                    }
                }
            }
        }
    }
}
