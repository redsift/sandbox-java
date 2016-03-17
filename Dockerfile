FROM ubuntu:15.10
MAINTAINER Deepak Prabhakara email: deepak@redsift.io version: 1.1.101

# Install JDK without things like fuse
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y --no-install-recommends openjdk-8-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

ENV SIFT_ROOT="/run/dagger/sift" IPC_ROOT="/run/dagger/ipc"
LABEL io.redsift.dagger.init="-cp /usr/bin/redsift/install.jar Install" io.redsift.dagger.run="/usr/bin/redsift/bootstrap.jar"

VOLUME /run/dagger/sift

WORKDIR /run/dagger/sift

ENTRYPOINT ["/usr/bin/java"]
