FROM mcr.microsoft.com/playwright/java:v1.49.0-noble

# Set the working directory
WORKDIR /app

# Copy the project files into the container
COPY . .

# Install project dependencies and build the project
RUN ./gradlew build

# Set the entry point to run your Playwright test
CMD ["./gradlew", "run"]
