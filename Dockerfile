FROM jenkins/jenkins:latest
COPY plugins*.txt /usr/share/jenkins/ref/
RUN ls /usr/share/jenkins/ref/
RUN cat /usr/share/jenkins/ref/plugins*.txt | /usr/local/bin/install-plugins.sh
COPY /config /config/
