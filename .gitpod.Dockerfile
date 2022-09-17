FROM gitpod/workspace-full

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 17.0.4.fx-zulu && \
    sdk default java 17.0.4.fx-zulu"

RUN bash -c "curl -L https://fly.io/install.sh | sh"
ENV PATH="${PATH}:/home/gitpod/.fly/bin"