FROM quay.io/redsift/sandbox:latest
MAINTAINER Deepak Prabhakara email: deepak@redsift.io version: 1.1.101

# Install JDK without things like fuse
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y --no-install-recommends openjdk-8-jdk maven && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

LABEL io.redsift.sandbox.install="/usr/bin/redsift/install" io.redsift.sandbox.run="/usr/bin/redsift/run"

COPY root /

COPY src /tmp/sandbox/src
COPY external /tmp/sandbox/external
COPY assembly.xml /tmp/sandbox
COPY pom.xml /tmp/sandbox

WORKDIR /tmp/sandbox

# This is needed to fix the maven plugins error "trustAnchors parameter must be non-empty"
RUN update-ca-certificates -f

# Run maven build
RUN mvn clean && mvn install && \
    cd /tmp/sandbox/target/classes && \
    jar cvf compute.jar io/redsift/Compute*.class io/jmap/*.class && \
    cp compute.jar /usr/bin/redsift/compute.jar && \
    cp /tmp/sandbox/target/sandbox-*-fat.jar /usr/bin/redsift/sandbox.jar && \
    rm -rf /tmp/sandbox

WORKDIR /run/sandbox/sift

ENTRYPOINT ["/bin/bash"]
