#!/bin/bash

exec java -jar -Xmx64m \
  -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
  /home/app/tasks-ui-server.jar