FROM docker.io/alpine:latest as initdbpermfix

COPY init-container/init.sh /tmp
COPY init-container/healthcheck.sh /tmp
RUN chmod +x /tmp/init.sh && chmod +x /tmp/healthcheck.sh

FROM docker.io/cassandra:4.1.8 as initdb
COPY --from=initdbpermfix /tmp/init.sh /usr/bin
COPY --from=initdbpermfix /tmp/healthcheck.sh /usr/bin
COPY xconf-angular-admin/src/test/resources/schema.cql /tmp
HEALTHCHECK --start-period=60s CMD "/usr/bin/healthcheck.sh"
ENTRYPOINT ["/usr/bin/init.sh"]

FROM docker.io/maven:3.9.9-amazoncorretto-8-debian-bookworm as builder
RUN apt-get -y update && apt-get -y install git
RUN useradd -m -s /bin/bash build
COPY . /tmp/build
RUN chown -R build:build /tmp/build
USER build
RUN cd /tmp/build && \
    cp xconf-angular-admin/src/main/resources/container.service.properties xconf-angular-admin/src/main/resources/service.properties && \
    cp xconf-dataservice/src/main/resources/container.service.properties xconf-dataservice/src/main/resources/service.properties && \
    mvn -DskipTests=true clean install

FROM docker.io/jetty:9.4.57-jre8 as angular
COPY --from=builder /tmp/build/xconf-angular-admin/target/xconfAdminService2.war /var/lib/jetty/webapps/admin.war

FROM docker.io/jetty:9.4.57-jre8 as dataservice
COPY --from=builder /tmp/build/xconf-dataservice/target/xconf-dataservice.war /var/lib/jetty/webapps/ROOT.war
