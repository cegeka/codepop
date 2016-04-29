FROM java:8-jdk

ENV GRADLE_VERSION 2.11

RUN apt-get update && apt-get install -y lxc

WORKDIR /usr/bin
RUN curl -sLO https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-all.zip && \
  unzip -q gradle-${GRADLE_VERSION}-all.zip && \
  ln -s gradle-${GRADLE_VERSION} gradle && \
  rm gradle-${GRADLE_VERSION}-all.zip

ENV GRADLE_HOME /usr/bin/gradle
ENV PATH $PATH:$GRADLE_HOME/bin
ENV GRADLE_OPTS -Xmx512m

RUN mkdir -p /usr/src
WORKDIR /usr/src

ENTRYPOINT ["cat"]