FROM eclipse-temurin:21-jre

RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /sbin/nologin appuser


WORKDIR /app

COPY --chown=appuser:appuser build/libs/hotel-reservation-1.0.2.jar app.jar

USER appuser

EXPOSE 8090

ENTRYPOINT ["java","-jar", "app.jar"]