# Step 1 — Build the app using Maven
# This image has Java 21 and Maven already installed
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first so Maven can download dependencies
# This is cached separately so rebuilds are faster
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the JAR file, skip tests for speed
RUN mvn clean package -DskipTests

# Step 2 — Run the app using a smaller image
# We don't need Maven anymore, just Java to run the JAR
FROM eclipse-temurin:21-jre

# Set the working directory
WORKDIR /app

# Copy only the built JAR from the build step
COPY --from=build /app/target/*.jar app.jar

# Tell Docker this app runs on port 8080
EXPOSE 8080

# Command to start the app
ENTRYPOINT ["java", "-jar", "app.jar"]