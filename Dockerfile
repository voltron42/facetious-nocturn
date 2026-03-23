# Step 1: Build the application
FROM clojure:openjdk-17-lein-2.10.0 AS builder
WORKDIR /app

# Copy project definition and download dependencies
COPY project.clj /app/
RUN lein deps

# Copy source and build uberjar
COPY . /app
RUN lein uberjar

# Step 2: Create the runtime image
FROM openjdk:17-slim
WORKDIR /app

# Copy the compiled uberjar
COPY --from=builder /app/target/*-standalone.jar /app/app.jar
ENV PORT 3000
EXPOSE 3000
CMD ["java", "-jar", "app.jar"]