
def call(Map config = [:]) {

    def repoUrl = config.get(
        'repoUrl',
        'https://github.com/OT-MICROSERVICES/notification-worker.git'
    )

    def branchName = config.get('branch', 'main')
    def pythonCommand = config.get('pythonCommand', 'python3')
    def appFile = config.get('appFile', 'notification_api.py')

    stage('Clone Repository') {
        git branch: branchName, url: repoUrl
    }

    stage('Create Virtual Environment') {
        sh """
            set -e
            ${pythonCommand} -m venv .venv
            .venv/bin/python --version
        """
    }

    stage('Install Packages') {
        sh '''
            set -e
            .venv/bin/python -m pip install --upgrade pip
            .venv/bin/python -m pip install -r requirements.txt
        '''
    }

    stage('Build') {
        sh """
            set -e
            .venv/bin/python -m py_compile '${appFile}'
            echo "Build completed successfully."
        """
    }
}
