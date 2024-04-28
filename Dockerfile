# Use the official Clojure image as the base image
FROM clojure:temurin-22-lein AS builder

# Install curl & node 22
RUN apt-get update && apt-get install -y curl
RUN curl -fsSL https://deb.nodesource.com/setup_22.x | bash -
RUN apt-get install -y nodejs

# Set the working directory in the container
WORKDIR /app

# Copy the project.clj file to the container
COPY project.clj .

# Download and cache the dependencies
RUN lein deps

# Copy the entire project to the container
COPY . .

# Build the app
RUN lein release
RUN lein build-html

# Use the official Nginx image as the base image
FROM nginx:latest

# Copy the static files from the builder stage to the Nginx web root directory
COPY --from=builder /app/resources/public/ /usr/share/nginx/html

# Expose port 80 for Nginx
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]