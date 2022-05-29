FROM openjdk:10.0.1-jre
LABEL maintainer="ian.j.morgan@gmail.com"

EXPOSE 7001

RUN mkdir -p /home/app/
RUN mkdir -p /home/app/src/test/resources/examples

COPY ./docker/run.sh /home/app/run.sh
RUN chmod +x /home/app/run.sh

COPY ./src/test/resources/examples/* /home/app/src/test/resources/examples/
COPY ./build/libs/event-store*.jar /home/app/event-store.jar

WORKDIR /home/app

ENTRYPOINT ["./run.sh"]