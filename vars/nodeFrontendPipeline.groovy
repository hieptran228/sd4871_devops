#!/usr/bin/env groovy
void call(Map pipelineParams){
    pipeline {
        agent any

        options {
            disableConcurrentBuilds()
            disableResume()
            timeout(time: 1, unit: 'HOURS')
        }

        stages {
            stage ('Load Pipeline') {
                when {
                    allOf {
                        // Condition Check
                        anyOf{
                            // Branch Event: Nornal Flow
                            anyOf {
                                branch 'main'
                                branch 'jenkins'
                                branch 'PR-*'
                            }
                            // Manual Run: Only if checked.
                            allOf {
                                triggeredBy 'UserIdCause'
                            }
                            // Changes from frontend
                            allOf {
                                changeset '**/frontend/**'
                            }
                        }
                    }
                }
                steps {
                    script {
                        nodeFrontend()
                    }
                }
            }
        }

        post {
            cleanup {
                cleanWs()
            }
        }
    }
}
//========================================================================
// Node CI
// Version: v1.0
// Updated:
//========================================================================
//========================================================================
// Notes:
//
//
//========================================================================
