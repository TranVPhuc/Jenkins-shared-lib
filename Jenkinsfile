@Library('Jenkins-shared-lib') _

autoScalingWrapper(
    region: 'ap-southeast-1',
    asgName: 'test-asg',
    agentLabel: 'agent1'
) {
    echo "Cloning repo and running Python script on agent: ${env.AGENT_LABEL}"
    sh 'git clone https://github.com/TranVPhuc/Packer.git'
    sh 'cd Packer && python3 main.py'
}
