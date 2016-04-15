FROM quay.io/redsift/sandbox:latest
MAINTAINER Deepak Prabhakara email: deepak@redsift.io version: 1.1.101

# Install JDK without things like fuse
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y --no-install-recommends openjdk-8-jdk maven && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

LABEL io.redsift.dagger.init="/usr/bin/redsift/install" io.redsift.dagger.run="/usr/bin/redsift/bootstrap"

COPY root /

COPY src /tmp/sandbox/src
COPY external /tmp/sandbox/external
COPY assembly.xml /tmp/sandbox
COPY pom.xml /tmp/sandbox

WORKDIR /tmp/sandbox

# This is needed to fix the maven plugins error "trustAnchors parameter must be non-empty"
RUN update-ca-certificates -f

# Run maven build
RUN mvn package

RUN cd /tmp/sandbox/target/classes && jar cvf compute.jar com/redsift/Compute*.class && cp compute.jar /usr/bin/redsift/compute.jar

RUN cp /tmp/sandbox/target/sandbox-*-fat.jar /usr/bin/redsift/sandbox.jar

RUN rm -rf /tmp/sandbox

RUN mvn install:install-file -Dfile=/usr/bin/redsift/compute.jar -DgroupId=com.redsift -DartifactId=compute -Dversion=1.0 -Dpackaging=jar

WORKDIR /run/dagger/sift

ENTRYPOINT ["/bin/bash"]
