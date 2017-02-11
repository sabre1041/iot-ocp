FROM rhel:7.2

MAINTAINER Andrew Block <andy.block@gmail.com>

ENV HOME=/opt/zeppelin \
    MAVEN_VERSION=3.3.9 \
    JAVA_HOME=/usr/lib/jvm/java \
    ZEPPELIN_VERSION=0.6.2 \
    ZEPPELIN_SERVER_HOME=/opt/zeppelin/server \
    ZEPPELIN_STORAGE_DIR=/opt/zeppelin/storage \
    ZEPPELIN_CONF_DIR=/opt/zeppelin/storage/conf \
    ZEPPELIN_LOG_DIR=/opt/zeppelin/storage/logs \
    ZEPPELIN_NOTEBOOK_DIR=/opt/zeppelin/storage/notebook \
    ZEPPELIN_INTERPRETER_DIR=/opt/zeppelin/storage/interpreter


RUN yum clean all && \
    export INSTALL_PKGS="java-1.8.0-openjdk-devel java-1.8.0-openjdk-headless gettext tar git which unzip" && \
    yum install -y --setopt=tsflags=nodocs $INSTALL_PKGS && \
    rpm -V $INSTALL_PKGS && \
    yum clean all && \
    mkdir -p $HOME/server $HOME/bin $HOME/storage && \
    curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
      && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
      && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn && \
    curl -fSL http://archive.apache.org/dist/zeppelin/zeppelin-$ZEPPELIN_VERSION/zeppelin-$ZEPPELIN_VERSION-bin-netinst.tgz | tar xzf - --strip 1 -C $ZEPPELIN_SERVER_HOME/ && \
    chown -R 1001:0 $HOME/server $HOME/bin $HOME/storage && \
    chmod -R "g+rwX" $HOME/server $HOME/bin $HOME/storage
    
ADD bin/start.sh /$HOME/bin/

EXPOSE 8080

WORKDIR /opt/zeppelin

VOLUME /opt/zeppelin/storage

USER 1001

ENTRYPOINT ["/opt/zeppelin/bin/start.sh"]
