#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
if [ "$#" -ne 1 ];then
  echo "Erorr, can't open envfile: $1"
  exit 1
fi
source $1

CNAME=consul-server
DOCKER_IMG="consul:0.8.4"

CONSUL_PORT=8500

docker rm -f -v $CNAME
docker run -d \
  --restart="always" \
  --name $CNAME  \
  -p $CONSUL_PORT:8500 \
  -v $VOL:/consul/data \
  -v $DIR/conf:/consul/config \
  -t $DOCKER_IMG
