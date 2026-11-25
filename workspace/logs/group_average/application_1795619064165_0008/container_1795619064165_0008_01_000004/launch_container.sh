#!/bin/bash

set -o pipefail -e
export PRELAUNCH_OUT="/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/prelaunch.out"
exec >"${PRELAUNCH_OUT}"
export PRELAUNCH_ERR="/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/prelaunch.err"
exec 2>"${PRELAUNCH_ERR}"
echo "Setting up env variables"
export JAVA_HOME=${JAVA_HOME:-"/opt/java/openjdk"}
export HADOOP_COMMON_HOME=${HADOOP_COMMON_HOME:-"/opt/hadoop"}
export HADOOP_HDFS_HOME=${HADOOP_HDFS_HOME:-"/opt/hadoop"}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-"/opt/hadoop/etc/hadoop"}
export HADOOP_YARN_HOME=${HADOOP_YARN_HOME:-"/opt/hadoop"}
export HADOOP_HOME=${HADOOP_HOME:-"/opt/hadoop"}
export PATH=${PATH:-"/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/hadoop/bin"}
export LANG=${LANG:-"en_US.UTF-8"}
export HADOOP_TOKEN_FILE_LOCATION="/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/appcache/application_1795619064165_0008/container_1795619064165_0008_01_000004/container_tokens"
export CONTAINER_ID="container_1795619064165_0008_01_000004"
export NM_PORT="38133"
export NM_HOST="nodemanager"
export NM_HTTP_PORT="8042"
export LOCAL_DIRS="/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/appcache/application_1795619064165_0008"
export LOCAL_USER_DIRS="/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/"
export LOG_DIRS="/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004"
export USER="hadoop"
export LOGNAME="hadoop"
export HOME="/home/"
export PWD="/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/appcache/application_1795619064165_0008/container_1795619064165_0008_01_000004"
export LOCALIZATION_COUNTERS="281204,0,2,0,94"
export JVM_PID="$$"
export NM_AUX_SERVICE_mapreduce_shuffle="AAA0+gAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
export STDOUT_LOGFILE_ENV="/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/stdout"
export SHELL="/bin/bash"
export HADOOP_ROOT_LOGGER="INFO,console"
export HADOOP_MAPRED_HOME="/opt/hadoop"
export CLASSPATH="$PWD:$HADOOP_CONF_DIR:$HADOOP_COMMON_HOME/share/hadoop/common/*:$HADOOP_COMMON_HOME/share/hadoop/common/lib/*:$HADOOP_HDFS_HOME/share/hadoop/hdfs/*:$HADOOP_HDFS_HOME/share/hadoop/hdfs/lib/*:$HADOOP_YARN_HOME/share/hadoop/yarn/*:$HADOOP_YARN_HOME/share/hadoop/yarn/lib/*:$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/*:$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/lib/*:job.jar/*:job.jar/classes/:job.jar/lib/*:$PWD/*"
export LD_LIBRARY_PATH="$PWD:$HADOOP_COMMON_HOME/lib/native"
export STDERR_LOGFILE_ENV="/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/stderr"
export HADOOP_CLIENT_OPTS=""
export MALLOC_ARENA_MAX="4"
echo "Setting up job resources"
ln -sf -- "/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/appcache/application_1795619064165_0008/filecache/14/job.jar" "job.jar"
ln -sf -- "/tmp/hadoop-hadoop/nm-local-dir/usercache/hadoop/appcache/application_1795619064165_0008/filecache/15/job.xml" "job.xml"
echo "Copying debugging information"
# Creating copy of launch script
cp "launch_container.sh" "/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/launch_container.sh"
chmod 640 "/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/launch_container.sh"
# Determining directory contents
echo "ls -l:" 1>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
ls -l 1>>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
echo "find -L . -maxdepth 5 -ls:" 1>>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
find -L . -maxdepth 5 -ls 1>>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
echo "broken symlinks(find -L . -maxdepth 5 -type l -ls):" 1>>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
find -L . -maxdepth 5 -type l -ls 1>>"/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/directory.info"
echo "Launching container"
exec /bin/bash -c "$JAVA_HOME/bin/java -Djava.net.preferIPv4Stack=true -Dhadoop.metrics.log.level=WARN  -Xmx384m -Djava.io.tmpdir=$PWD/tmp -Dlog4j.configuration=container-log4j.properties -Dyarn.app.container.log.dir=/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004 -Dyarn.app.container.log.filesize=0 -Dhadoop.root.logger=INFO,CLA -Dhadoop.root.logfile=syslog org.apache.hadoop.mapred.YarnChild 172.18.0.3 33767 attempt_1795619064165_0008_m_000002_0 4 1>/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/stdout 2>/var/log/hadoop/userlogs/application_1795619064165_0008/container_1795619064165_0008_01_000004/stderr "
