def call(Map config = [:], Closure body) {
    def region = config.region ?: 'ap-southeast-1'
    def asgName = config.asgName ?: 'test-asg'
    def agentLabel = config.agentLabel ?: 'agent1'

    pipeline {
        agent any
        environment {
            AWS_REGION = region
            ASG_NAME = asgName
            AGENT_LABEL = agentLabel
        }

        stages {
            stage('Scale up ASG') {
                steps {
                    sh """
                        set -e
                        aws autoscaling update-auto-scaling-group \
                          --auto-scaling-group-name "${ASG_NAME}" \
                          --desired-capacity 1 \
                          --region "${AWS_REGION}"
                    """
                }
            }

            stage('Wait for agent to connect') {
                steps {
                    echo "Waiting for Jenkins agent to connect..."
                    sleep(time: 60, unit: 'SECONDS')
                }
            }

            stage('Run Task') {
                agent { label "${agentLabel}" }
                steps {
                    script {
                        body() 
                    }
                }
            }
        }

        post {
            always {
                sh """
                    set -e
                    aws autoscaling update-auto-scaling-group \
                      --auto-scaling-group-name "${ASG_NAME}" \
                      --desired-capacity 0 \
                      --region "${AWS_REGION}"
                """
            }
        }
    }
}
