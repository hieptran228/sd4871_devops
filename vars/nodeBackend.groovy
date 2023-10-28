#!/usr/bin/env groovy
void call() {
    String name = "backend-nashtech-hieptran-sd4871"
    String baseImage     = "node"
    String baseTag       = "lts-buster-slim"
    String registry = "913820192915.dkr.ecr.ap-southeast-1.amazonaws.com"
    String sonarToken = "sonar-token"
    String ecrCredential = 'ecr:ap-southeast-1:aws-cred'
    String k8sCredential = 'eks-token'
    String namespace = "node"

//========================================================================
//========================================================================

//========================================================================
//========================================================================

    stage ('Prepare Package') {
        script {
            writeFile file: '.ci/Dockerfile', text: libraryResource('dev/node/Dockerfile.backend')
        }
    }

    stage ("Build Image") {
        docker.build("${registry}/${name}:${BUILD_NUMBER}", "-f ./.ci/Dockerfile \
        --build-arg BASEIMG=${baseImage} --build-arg IMG_VERSION=${baseTag} \
        ${WORKSPACE}/src/backend")
    }

    stage ("Trivy scan") {
        // Install trivy
        // sh 'curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin v0.18.3'
        // sh 'curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/html.tpl > html.tpl'

        // Scan all vuln levels
        sh 'mkdir -p reports'
        sh 'trivy filesystem --ignore-unfixed --vuln-type os,library --format template --template "@html.tpl" -o reports/backend-scan.html ./src/backend'
        publishHTML target : [
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: 'reports',
            reportFiles: 'backend-scan.html',
            reportName: 'Trivy Scan',
            reportTitles: 'Trivy Scan'
        ]

        // Scan again and fail on CRITICAL vulns
        sh 'trivy filesystem --ignore-unfixed --vuln-type os,library --exit-code 1 --severity CRITICAL ./src/backend'
    }

    stage ("Push Docker Images") {
        docker.withRegistry("https://${registry}", ecrCredential) {
            docker.image("${registry}/${name}:${BUILD_NUMBER}").push()
        }
    }
}

//========================================================================
// Demo CI
// Version: v1.0
// Updated:
//========================================================================
//========================================================================
// Notes:
//
//
//========================================================================