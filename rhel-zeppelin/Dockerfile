FROM rhel:7.2

MAINTAINER Andrew Block <andy.block@gmail.com>

ENV HOME /opt/zeppelin
ENV MAVEN_VERSION 3.3.9
ENV JAVA_HOME /usr/lib/jvm/java
ENV ZEPPELIN_VERSION 0.6.1

RUN yum clean all && \
    export INSTALL_PKGS="java-1.8.0-openjdk-devel java-1.8.0-openjdk-headless gettext tar git which unzip" && \
    yum install -y --setopt=tsflags=nodocs install $INSTALL_PKGS && \
    rpm -V $INSTALL_PKGS && \
    yum clean all && \
    mkdir -p /opt/zeppelin && \
    curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
      && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
      && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn && \
    curl -fsSL http://archive.apache.org/dist/zeppelin/zeppelin-$ZEPPELIN_VERSION/zeppelin-${ZEPPELIN_VERSION}-bin-all.tgz | tar xzf - --strip 1 -C $HOME && \
    chown -R 1001:0 /opt/zeppelin && \
    chmod -R "g+rwX" /opt/zeppelin

EXPOSE 8080

WORKDIR /opt/zeppelin

USER 1001

ENTRYPOINT ["/opt/zeppelin/bin/zeppelin.sh"]