def call(Map config = [:]) {
    def srcDir = config.get('srcDir', 'src')

    echo "🔍 [Shared Library] Running ESLint Static Code Analysis on '${srcDir}'..."

    // Sirf ESLint scan chalayega
    sh "npx eslint ${srcDir}/ --quiet || true"

    echo "✅ [Shared Library] ESLint Static Code Analysis Completed!"
}
