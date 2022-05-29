#!/bin/bash

export dreifa_app_tasks_ui_jaegerEndpoint=http://jaeger:14250
exec java -jar -Xmx64m -XX:+HeapDumpOnOutOfMemoryError \
	-XX:+PrintGC \
    /home/app/tasks-ui-server.jar