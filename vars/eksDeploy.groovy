#!/usr/bin/env groovy
void call() {
    String registry = "913820192915.dkr.ecr.ap-southeast-1.amazonaws.com"
    String ecrCredential = 'ecr:ap-southeast-1:aws-cred'
    String k8sCredential = 'eks-token'
    String namespace = "node"
    String frontendApp = "frontend"
    String backendApp = "backend"
    String project = "nashtech-hieptran-sd4871"
    String frontendTag = "latest"
    String backendTag = "latest"

//========================================================================
//========================================================================

//========================================================================
//========================================================================

    stage ('Prepare Package') {
        script {
            writeFile file: '.cd/backend.yml', text: libraryResource('deploy/eks/node/backend.yaml')
            writeFile file: '.cd/frontend.yml', text: libraryResource('deploy/eks/node/frontend.yaml')
            writeFile file: '.cd/ingress.yml', text: libraryResource('deploy/eks/node/ingress.yaml')
            writeFile file: '.cd/mongodb.yml', text: libraryResource('deploy/eks/node/mongodb.yaml')
        }
    }

    stage ("Deploy To EKS") {
        docker.withRegistry("https://${registry}", ecrCredential) {
            withKubeConfig([credentialsId: 'eks-dev', serverUrl: '']) {
                sh "export registry=${registry}; export frontendApp=${frontendApp}; export backendApp=${backendApp}; export project=${project}; export frontendTag=${frontendTag}; export backendTag=${backendTag};\
                envsubst < .cd/frontend.yml > frontend.yml; envsubst < .cd/backend.yml > backend.yml; envsubst < .cd/ingress.yml > ingress.yml; envsubst < .cd/mongodb.yml > mongodb.yml"
                sh "kubectl apply -f frontend.yml -n ${namespace}"
                sh "kubectl apply -f backend.yml -n ${namespace}"
                sh "kubectl apply -f ingress.yml -n ${namespace}"
                sh "kubectl apply -f mongodb.yml -n ${namespace}"
            }
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