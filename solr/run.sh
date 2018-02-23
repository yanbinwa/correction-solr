#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
if [ "$#" -ne 1 ];then
  echo "Erorr, can't open envfile: $1"
  exit 1
fi
source $1

CNAME=solr
DOCKER_IMG="solr:5.5"

cp $DIR/conf/solr.xml $VOL/

SOLR_PORT=8081
JMX_PORT=8082

docker rm -f -v $CNAME
docker run -d \
  --restart="always" \
  --name $CNAME  \
  -p $SOLR_PORT:8983 \
  -p $JMX_PORT:8082 \
  -v $VOL:/opt/solr/server/solr \
  -v $DIR/conf/set-heap.sh:/docker-entrypoint-initdb.d/set-heap.sh \
  -v $DIR/conf/set-jmx.sh:/docker-entrypoint-initdb.d/set-jmx.sh \
  -t $DOCKER_IMG