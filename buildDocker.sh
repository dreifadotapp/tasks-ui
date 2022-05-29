
docker build -t tasks-ui-server .
#docker tag event-store:latest ianmorgan/event-store:latest
#docker push ianmorgan/event-store:latest


docker run -p 8080:8080 tasks-ui-server