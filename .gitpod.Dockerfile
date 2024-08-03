FROM gitpod/workspace-full:2024-07-14-17-19-51

SHELL ["/bin/bash", "-c"]
RUN source "/home/gitpod/.sdkman/bin/sdkman-init.sh"  \
    && sdk install java 21.0.4-tem < /dev/null
