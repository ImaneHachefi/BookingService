FROM eclipse-temurin:11-jre

# Même version de H2 que le projet Maven (1.4.200)
ADD https://repo1.maven.org/maven2/com/h2database/h2/1.4.200/h2-1.4.200.jar /opt/h2/h2.jar

VOLUME /h2-data
EXPOSE 1521 81

CMD ["java", "-cp", "/opt/h2/h2.jar", "org.h2.tools.Server", \
     "-tcp", "-tcpAllowOthers", "-tcpPort", "1521", \
     "-web", "-webAllowOthers", "-webPort", "81", \
     "-baseDir", "/h2-data", "-ifNotExists"]