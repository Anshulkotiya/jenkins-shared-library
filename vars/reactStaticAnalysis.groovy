def call(Map config = [:]) {
    def srcDir = config.get('srcDir', 'src')
    def reportFile = config.get('reportFile', 'react-static-analysis.html')

    echo "🔍 [Shared Library] Running ESLint Static Code Analysis on '${srcDir}'..."

    // ESLint se exact scan chalayega
    sh """
        npx eslint "${srcDir}/**/*.{js,jsx}" \
          --rule "no-unused-vars: warn" \
          --rule "no-undef: off" \
          --quiet \
          --format html \
          --output-file ${reportFile} || true
    """

    // Report attach karega build page par
    archiveArtifacts artifacts: reportFile, allowEmptyArchive: true
    echo "✅ [Shared Library] React Static Analysis Completed! Report: ${reportFile}"
}
