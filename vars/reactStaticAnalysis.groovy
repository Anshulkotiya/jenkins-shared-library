def call(Map config = [:]) {
    def srcDir = config.get('srcDir', 'src')
    echo "🔍 [Shared Library] Running ESLint Static Code Analysis on '${srcDir}'..."
    // React code ko ESLint se scan karega
    sh "npx eslint ${srcDir}/ --quiet || true"
    echo " [Shared Library] React Static Code Analysis Completed!"
}
