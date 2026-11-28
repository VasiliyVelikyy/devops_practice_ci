FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY build/libs/hotel-reservation-1.0.2.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar", "app.jar"]