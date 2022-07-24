#!/bin/bash

JAVA_MEM_OPTS="-Xms4g -Xmx4g -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=1024m -XX:NewRatio=1 -XX:SurvivorRatio=8 "

JAVA_GC_OPTS="-XX:+ExplicitGCInvokesConcurrent -XX:MaxTenuringThreshold=6 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly "

JAVA_DUMP_OPTS="-XX:+CrashOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/logs/jo-mqtt/heap-dump.hprof "

JAVA_LOG_OPTS="-XX:+PrintCommandLineFlags -Xloggc:/home/logs/jo-mqtt/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:ErrorFile=/home/logs/jo-mqtt/hs_err_pid_%p.log "

JAVA_JMX_OPTS="-Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1899 -Dcom.sun.management.jmxremote.ssl=false "

JAVA_OTHER_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=GMT+08 -Djava.security.egd=file:/dev/./urandom -XX:+UseCompressedOops -XX:+ParallelRefProcEnabled "

JAVA_OPTS="$JAVA_MEM_OPTS $JAVA_GC_OPTS $JAVA_DUMP_OPTS $JAVA_LOG_OPTS $JAVA_JMX_OPTS $JAVA_OTHER_OPTS "

nohup java -server -jar $JAVA_OPTS -Dserver.port=7788 -Dmqtt.serverConfig.tcpPort=1883 -Dspring.profiles.active=prod -Dmqtt.nettyConfig.epoll=true mqtt-broker.jar>console.log 2>&1 &
echo "$!" > run.pid
echo "mqtt启动完成!"
