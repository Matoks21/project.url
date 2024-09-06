#Dockerfile
FROM openjdk:17-jdk-slim
ENV TZ=Europe/Kyiv
ENV SPRING_PROFILES_ACTIVE=h2
ENV PORT=8080
COPY build/libs/urlshorten-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dserver.port=8080", "-jar", "/app.jar"]
#FROM openjdk:17-jdk-slim
#ENV TZ=Europe/Kyiv
#ENV SPRING_PROFILES_ACTIVE=default
#COPY build/libs/urlshorten-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java", "-Dserver.port=$PORT", "-jar", "/app.jar"]
#ENTRYPOINT ["java", "-jar", "/app.jar"]
#ENV SPRING_PROFILES_ACTIVE=default