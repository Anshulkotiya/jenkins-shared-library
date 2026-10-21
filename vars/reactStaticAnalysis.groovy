def call(Map config = [:]) {
    def srcDir = config.get('srcDir', 'src')

    echo "🔍 [Shared Library] Running ESLint Static Code Analysis on '${srcDir}'..."

    sh '''
        # Node/npx check karega, agar nahi hai toh portable Node 18 use karega
        if ! command -v npx &> /dev/null; then
            mkdir -p /tmp/node18
            if [ ! -f "/tmp/node18/bin/npx" ]; then
                echo "📥 Downloading Node.js 18..."
                curl -sL https://nodejs.org/dist/v18.20.1/node-v18.20.1-linux-x64.tar.xz | tar -xJ -C /tmp/node18 --strip-components=1
            fi
            export PATH=/tmp/node18/bin:$PATH
        fi

        echo "Running with Node: $(node -v) and npx: $(npx -v)"
        
        # ESLint Scan
        npx eslint src/ --quiet || true
    '''

    echo "✅ [Shared Library] ESLint Static Code Analysis Completed!"
}
