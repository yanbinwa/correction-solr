#FROM maven:3.3.9-jdk-8
FROM docker-reg.emotibot.com.cn:55688/base/java8:20160809

# Set TimeZone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# set bundle version
ENV BUNDLE_VER=1.0-SNAPSHOT

# setup workspace
RUN mkdir -p /usr/src/correction-solr/
RUN mkdir -p /usr/src/correction-solr/log
WORKDIR /usr/src/correction-solr

COPY bin ./bin
COPY conf ./conf
COPY docker ./docker
COPY file ./file

# Download main jar
RUN wget "http://docker-reg.emotibot.com.cn:8081/artifactory/libs-snapshot-local/com/emotibot/correction-standard/${BUNDLE_VER}/correction-standard-${BUNDLE_VER}.jar" -P /usr/src/correction-solr

# defaults
ENTRYPOINT ["bin/run.sh"]