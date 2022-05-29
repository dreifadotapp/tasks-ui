
docker build -t event-store .
docker tag event-store:latest ianmorgan/event-store:latest
docker push ianmorgan/event-store:latest
