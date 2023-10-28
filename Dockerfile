FROM java:8-alpine

# Copy local code to the container image.
WORKDIR /app

COPY target ./target

EXPOSE 8080
# Run the web service on container startup.
CMD ["java","-jar","/app/target/user-center-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]