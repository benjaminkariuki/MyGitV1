# Use the OpenJDK 17 slim image as the base
FROM openjdk:17-jdk-slim

# Copy the src directory to /src in the container
COPY src /src

# Set the working directory to /src
WORKDIR /src

# Compile all Java files in the src directory
RUN javac *.java

# Set the default command to run the MyGit class
ENTRYPOINT ["java", "MyGit"]
