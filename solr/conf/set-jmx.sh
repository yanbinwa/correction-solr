#!/bin/bash
echo 'SOLR_OPTS="$SOLR_OPTS -Dcom.sun.management.jmxremote.port=8082"' >> /opt/solr/bin/solr.in.sh
echo 'SOLR_OPTS="$SOLR_OPTS -Dcom.sun.management.jmxremote.ssl=false"' >> /opt/solr/bin/solr.in.sh
echo 'SOLR_OPTS="$SOLR_OPTS -Dcom.sun.management.jmxremote.authenticate=false"' >> /opt/solr/bin/solr.in.sh
echo 'SOLR_OPTS="$SOLR_OPTS -Dcom.sun.management.jmxremote.rmi.port=8082"' >> /opt/solr/bin/solr.in.sh
echo 'SOLR_OPTS="$SOLR_OPTS -Djava.rmi.server.hostname=0.0.0.0"' >> /opt/solr/bin/solr.in.sh