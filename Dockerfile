FROM eclipse-temurin:17-focal AS build
RUN apt-get update && apt-get -y install gradle binutils

WORKDIR /build
COPY src/ ./src
COPY gradle/ ./gradle
COPY build.gradle.kts gradle.properties gradlew settings.gradle.kts ./
RUN ./gradlew shadowJar
RUN grep '^version =' build.gradle.kts | sed -e 's/version = "\(.\+\)"/\1/' > /torrentino_version

RUN ${JAVA_HOME}/bin/jdeps --ignore-missing-deps --print-module-deps build/libs/torrentino-$(cat /torrentino_version)-all.jar > /jar_deps
RUN ${JAVA_HOME}/bin/jlink \
        --add-modules $(cat /jar_deps),jdk.crypto.ec \
        --strip-debug \
        --no-man-pages \
        --no-header-files \
        --compress=2 \
        --output /jre

FROM debian:bullseye-slim
ARG UID=1000

VOLUME /downloads
VOLUME /data    

ENV JAVA_HOME=/jre
ENV PATH "${JAVA_HOME}/bin:${PATH}"

RUN apt-get update && \
    apt-get -y install transmission-daemon openssh-server&& \
    apt-get clean && \
    rm -r /var/lib/apt/lists/*
RUN adduser --disabled-password --uid $UID torrentino --shell /shell.sh
RUN sed -e 's/^torrentino:!/torrentino:*/g' /etc/shadow > /tmp/shadow && cat /tmp/shadow > /etc/shadow && rm /tmp/shadow
RUN mkdir /home/torrentino/.ssh && \
    ln -s /authorized_keys /home/torrentino/.ssh/authorized_keys && \
    chown -R torrentino:torrentino /home/torrentino/.ssh

COPY --from=build /build/build/libs/torrentino-*-all.jar /torrentino.jar
COPY --from=build /jre /jre
COPY config.toml /config.toml
COPY startup.sh /startup.sh
COPY sshd_config /etc/ssh/sshd_config
COPY shell.sh /shell.sh
RUN mkdir /torrents && \
    chmod 755 /shell.sh && \
    echo /shell.sh >> /etc/shells && \
    rm /etc/motd && \
    ln -s ${JAVA_HOME}/bin/java /usr/bin/java

WORKDIR /home/torrentino

ENV WEB_USER=
ENV WEB_PASSWORD=
ENV UID=$UID
EXPOSE 6881
EXPOSE 80
EXPOSE 22

CMD [ "/bin/sh", "/startup.sh" ]
