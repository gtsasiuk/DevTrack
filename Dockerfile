FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y wget unzip

RUN wget https://services.gradle.org/distributions/gradle-8.11.1-bin.zip -P /tmp \
    && unzip /tmp/gradle-8.11.1-bin.zip -d /opt \
    && rm /tmp/gradle-8.11.1-bin.zip

ENV GRADLE_HOME=/opt/gradle-8.11.1
ENV PATH=$PATH:$GRADLE_HOME/bin

WORKDIR /app

COPY . .

RUN gradle build

RUN ls -R build/libs

CMD ["java", "-jar", "build/libs/app.jar"]