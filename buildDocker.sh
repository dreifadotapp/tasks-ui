#!/bin/bash

./gradlew clean jar -x test
docker build -t tasks-ui-server .
