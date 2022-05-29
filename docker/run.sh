#!/bin/bash

exec java -jar -Xmx256m \
  -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
  /home/app/tasks-ui-server.jar