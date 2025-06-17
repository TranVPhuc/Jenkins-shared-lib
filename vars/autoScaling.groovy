def call(Map config = [:], Closure body) {
    def region = config.region ?: 'ap-southeast-1'
    def asgName = config.asgName ?: 'test-asg'
    def agentLabel = config.agentLabel ?: 'agent1'

    pipeline {
        agent any

        stages {
            stage('Set env and Scale up ASG') {
                steps {
                    script {
                        env.AWS_REGION = region
                        env.ASG_NAME = asgName
                        env.AGENT_LABEL = agentLabel

                        echo "Scaling up ASG: ${env.ASG_NAME} in region: ${env.AWS_REGION}"
                        sh """
                        aws autoscaling update-auto-scaling-group \
                          --auto-scaling-group-name "${env.ASG_NAME}" \
                          --desired-capacity 1 \
                          --region "${env.AWS_REGION}"
                        """
                    }
                }
            }

            stage('Wait for agent') {
                steps {
                    sleep(time: 60, unit: 'SECONDS')
                }
            }

            stage('Run main body') {
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
                script {
                    echo "Scaling down ASG: ${env.ASG_NAME}"
                    sh """
                    aws autoscaling update-auto-scaling-group \
                      --auto-scaling-group-name "${env.ASG_NAME}" \
                      --desired-capacity 0 \
                      --region "${env.AWS_REGION}"
                    """
                }
            }
        }
    }
}
