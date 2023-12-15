pipeline {
    agent {label 'master'}
    options {
        gitLabConnection('git.dos.softlab.ru')
        disableConcurrentBuilds()
    }
    stages {
        stage('build') {
            steps {
                gitlabCommitStatus(name: 'build') {
                    script {
                        if (isUnix()) {
                            sh 'mvn -B -U clean package'
                        } else {
                            bat 'mvn -B -U clean package'
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/*-reports/*.xml'
                }
            }
        }

        stage('Sonar analysis') {
            steps {
                gitlabCommitStatus(name: 'Sonar analysis') {
                    withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonar-analysis-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                        script {
                            if (env.BRANCH_NAME != 'develop') {
                                bat '''
                                    mvn -B sonar:sonar \
                                        -Dsonar.host.url=http://many.softlab.ru:9000 \
                                        -Dsonar.login=%USERNAME% \
                                        -Dsonar.password=%PASSWORD% \
                                        -Dsonar.gitlab.url=http://git.dos.softlab.ru \
                                        -Dsonar.gitlab.user_token=ajFTr2skguANuHZAsf3B \
                                        -Dsonar.gitlab.project_id=%GIT_URL% \
                                        -Dsonar.gitlab.ref_name=%BRANCH_NAME% \
                                        -Dsonar.gitlab.commit_sha=%GIT_COMMIT% \
                                        -Dsonar.gitlab.unique_issue_per_inline=false \
                                        -Dsonar.gitlab.quality_gate_fail_mode=warn \
                                        -Dsonar.gitlab.failure_notification_mode=commit-status \
                                        -Dsonar.gitlab.load_rules=true \
                                        -Dsonar.gitlab.ping_user=true \
                                        -Dsonar.gitlab.comment_no_issue=true \
                                        -Dsonar.issuesReport.console.enable=true \
                                        -Dsonar.analysis.mode=preview
                                '''
                            } else {
                                bat '''
                                    mvn -B sonar:sonar \
                                        -Dsonar.host.url=http://many.softlab.ru:9000 \
                                        -Dsonar.login=%USERNAME% \
                                        -Dsonar.password=%PASSWORD% \
                                        -Dsonar.gitlab.url=http://git.dos.softlab.ru \
                                        -Dsonar.gitlab.user_token=ajFTr2skguANuHZAsf3B \
                                        -Dsonar.gitlab.project_id=%GIT_URL% \
                                        -Dsonar.gitlab.ref_name=%BRANCH_NAME% \
                                        -Dsonar.gitlab.commit_sha=%GIT_COMMIT%
                                '''
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                // build status of null means successful
                currentBuild.result = currentBuild.result ?: 'SUCCESS'
            }
            emailext attachLog: true,
                to: "${env.BUILD_EFR_EMAIL_RECIPIENTS}",
                recipientProviders: [
                    [$class: 'DevelopersRecipientProvider'],
                    [$class: 'RequesterRecipientProvider'],
                    [$class: 'FirstFailingBuildSuspectsRecipientProvider'],
                    [$class: 'FailingTestSuspectsRecipientProvider']
                ],
                body: '${JELLY_SCRIPT, template="html-with-health-and-console-fixed-changes"}',
                subject: '$PROJECT_NAME # $BUILD_NUMBER - $BUILD_STATUS'
        }
    }
}