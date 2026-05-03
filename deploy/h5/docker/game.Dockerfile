FROM eclipse-temurin:17-jdk AS builder

RUN apt-get update \
    && apt-get install -y --no-install-recommends ant \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/L2J_Mobius_CT_2.6_HighFive
COPY L2J_Mobius_CT_2.6_HighFive ./
RUN ant

FROM eclipse-temurin:17-jre

WORKDIR /opt/l2
COPY L2J_Mobius_CT_2.6_HighFive/dist/libs ./libs
COPY L2J_Mobius_CT_2.6_HighFive/dist/game ./game
COPY --from=builder /workspace/build/dist/libs/GameServer.jar ./libs/GameServer.jar
COPY deploy/h5/docker/l2h5-entrypoint.sh /usr/local/bin/l2h5-entrypoint.sh

WORKDIR /opt/l2/game
RUN chmod +x ./*.sh /usr/local/bin/l2h5-entrypoint.sh && mkdir -p log

EXPOSE 7777

ENTRYPOINT ["/usr/local/bin/l2h5-entrypoint.sh", "game"]
CMD ["/bin/bash", "./GameServerTask.sh"]
