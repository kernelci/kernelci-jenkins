ARG jenkins_docker_image=jenkins/jenkins:latest
FROM $jenkins_docker_image

ARG config=/config
COPY plugins*.txt /usr/share/jenkins/ref/
RUN cat /usr/share/jenkins/ref/plugins*.txt > /usr/share/jenkins/ref/plugins-all.txt
RUN /bin/jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins-all.txt
COPY $config /config/
