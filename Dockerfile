# Use a base image with Java pre-installed
FROM openjdk:20-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the application class file and resources from the build directory into the container
COPY build/libs/spring-boot-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy the resources directory from the build directory into the container
COPY build/resources/main/ ./resources/

# Expose the ports your application will listen on
EXPOSE 8080 8085

# Command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]
