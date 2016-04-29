FROM jenkins:1.642.2

ENV JENKINS_OPTS --httpPort=8888

USER root
RUN apt-get update && apt-get install -y lxc

COPY id_rsa /root/.ssh/id_rsa
RUN chmod 400 /root/.ssh/id_rsa
RUN  echo "    IdentityFile /root/.ssh/id_rsa" >> /etc/ssh/ssh_config
RUN ssh-keyscan -t rsa github.com >> /root/.ssh/known_hosts
RUN ssh-keyscan -t rsa yourproductionserver.com >> /root/.ssh/known_hosts

COPY config.groovy.override /usr/share/jenkins/ref/init.groovy.d/config.groovy.override

COPY plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

RUN git config --global credential.helper 'cache --timeout=18000'
