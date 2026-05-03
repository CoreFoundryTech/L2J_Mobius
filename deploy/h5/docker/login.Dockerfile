FROM eclipse-temurin:17-jdk AS builder

RUN apt-get update \
    && apt-get install -y --no-install-recommends ant \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/L2J_Mobius_CT_2.6_HighFive
COPY L2J_Mobius_CT_2.6_HighFive ./
RUN ant jar

FROM eclipse-temurin:17-jre

WORKDIR /opt/l2
COPY L2J_Mobius_CT_2.6_HighFive/dist/libs ./libs
COPY L2J_Mobius_CT_2.6_HighFive/dist/login ./login
COPY --from=builder /workspace/build/dist/libs/LoginServer.jar ./libs/LoginServer.jar
COPY deploy/h5/docker/l2h5-entrypoint.sh /usr/local/bin/l2h5-entrypoint.sh

WORKDIR /opt/l2/login
RUN chmod +x ./*.sh /usr/local/bin/l2h5-entrypoint.sh && mkdir -p log

EXPOSE 2106
EXPOSE 9014

ENTRYPOINT ["/usr/local/bin/l2h5-entrypoint.sh", "login"]
CMD ["/bin/bash", "./LoginServerTask.sh"]
