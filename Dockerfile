FROM openjdk:11.0.15-jre
LABEL maintainer="admin@dreifa.com"

EXPOSE 8080

RUN mkdir -p /home/app/
#RUN mkdir -p /home/app/src/test/resources/main

COPY ./docker/run.sh /home/app/run.sh
RUN chmod +x /home/app/run.sh

#COPY ./impl/src/test/resources/examples/* /home/app/src/test/resources/examples/
COPY ./impl/build/libs/tasks-ui-server.jar /home/app/tasks-ui-server.jar

WORKDIR /home/app

ENTRYPOINT ["./run.sh"]