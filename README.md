# GitOps Assignment

## Functional Specification

![](attachments/use-case.png)

![](attachments/gitops-seperate-repos.png)

## Technical Specification

### EC2 Instances Scan Scheduler

- Python script that will scan AWS account for EC2 instances with the attributes:
    - `Name=tag:k8s.io/role/master,Values=1`
    - `Name=instance-state-code,Values=16`
- Utilizing [boto3](https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/ec2.html) library
- Instances that are found printed to `stdout` utilizing json format library ([python-json-logger](https://github.com/madzak/python-json-logger))

### EC2 Instance Setup

Create EC2 Instances for `Jenkins` and `Minikube Cluster`:

1. Follow official [Jenkins on AWS](https://www.jenkins.io/doc/tutorials/tutorial-for-installing-jenkins-on-AWS/) guide to set up `Jenkins`.

    ```commandline
    sudo su
    yum update –y
    amazon-linux-extras install java-openjdk11 -y
    
    yum install docker -y
    wget -O /etc/yum.repos.d/jenkins.repo \
        https://pkg.jenkins.io/redhat-stable/jenkins.repo
    rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key
    yum upgrade
    yum install jenkins -y
    systemctl enable jenkins
    systemctl start jenkins
    systemctl status jenkins
    ```

2. Follow official [minikube start](https://minikube.sigs.k8s.io/docs/start/) guide to start Minikube cluster

    ```commandline
    sudo su
    yum update –y
    yum install docker -y
    ```

### References

- [boto3](https://boto3.amazonaws.com/v1/documentation/api/latest/reference/services/ec2.html)
- [Python Json Logger Library](https://github.com/madzak/python-json-logger)
- [Jenkins on AWS](https://www.jenkins.io/doc/tutorials/tutorial-for-installing-jenkins-on-AWS/)
- [Minikube docs](https://minikube.sigs.k8s.io/docs/start/)
