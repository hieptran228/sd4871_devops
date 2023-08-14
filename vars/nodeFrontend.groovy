#!/usr/bin/env groovy
void call() {
    String name = "frontend-nashtech-hieptran-sd4871"
    String baseImage     = "node"
    String baseTag       = "lts-buster-slim"
    String nodeRegistry = "913820192915.dkr.ecr.ap-southeast-1.amazonaws.com"
    String sonarToken = "sonar-token"
    String ecrCredential = 'ecr-token'
    String k8sCredential = 'eks-token'
    String namespace = "node"

//========================================================================
//========================================================================

//========================================================================
//========================================================================

    stage ('Prepare Package') {
        script {
            writeFile file: '.ci/Dockerfile', text: libraryResource('dev/node/Dockerfile.frontend')
            writeFile file: '.ci/deployment.yml', text: libraryResource('deploy/eks/node/frontend.yaml')
        }
    }

    stage ("Build Image") {
        docker.build("${nodeRegistry}/${name}:${BUILD_NUMBER}", "-f ./.ci/Dockerfile \
        --build-arg BASEIMG=${baseImage} --build-arg IMG_VERSION=${BUILD_NUMBER} \
        ${WORKSPACE}/src/frontend")
    }

    stage ("Push Docker Images") {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: ecrCredential, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            docker.withRegistry("https://${nodeRegistry}", ecrCredential ) {
                sh "docker push ${nodeRegistry}/${name}:${BUILD_NUMBER}"
            }
        }
    }
    stage ("Deploy To K8S") {
        withKubeConfig([credentialsId: 'eks-dev', serverUrl: '']) {
            sh "export registry=${demoRegistry}; export appname=${name}; export tag=${BUILD_NUMBER}; \
            envsubst < .ci/deployment.yml > deployment.yml"
            sh "kubectl apply -f deployment.yml -n ${namespace}"
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