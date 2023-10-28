#!/usr/bin/env groovy
void call() {
    String name = "frontend-nashtech-hieptran-sd4871"
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
            writeFile file: '.ci/Dockerfile', text: libraryResource('dev/node/Dockerfile.frontend')
        }
    }

    stage ("Build Image") {
        docker.build("${registry}/${name}:${BUILD_NUMBER}", "-f ./.ci/Dockerfile \
        --build-arg BASEIMG=${baseImage} --build-arg IMG_VERSION=${baseTag} \
        ${WORKSPACE}/src/frontend")
    }

    stage ("Trivy scan") {
        def formatOption = "--format template --template \"@/usr/local/share/trivy/templates/html.tpl\""

        // Scan all vuln levels
        sh 'mkdir -p reports'
        sh "trivy filesystem --ignore-unfixed --vuln-type os,library ${formatOption} -o reports/frontend-scan.html ./src/frontend"
        publishHTML(target: [
          allowMissing: true,
          alwaysLinkToLastBuild: false,
          keepAll: true,
          reportDir: "reports",
          reportFiles: "frontend-scan.html",
          reportName: "Trivy Report",
        ])

        // Scan again and fail on CRITICAL vulns
        // sh 'trivy filesystem --ignore-unfixed --vuln-type os,library --exit-code 1 --severity CRITICAL ./src/frontend'
    }

    stage ("Push Docker Images") {
        sh 'rm -f ~/.dockercfg ~/.docker/config.json || true'
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