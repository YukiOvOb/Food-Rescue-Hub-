# Build stage - Node 20 for Vite build
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files for dependency caching
COPY frontend/package.json frontend/package-lock.json* ./

# Install dependencies
RUN npm ci

# Copy source code
COPY frontend/ ./

# Build production assets
RUN npm run build

# Production stage - nginx to serve static files and proxy API
FROM nginx:1.27-alpine

# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*

# Update OS packages to pick up security fixes
RUN apk update && apk upgrade --no-cache

# Copy built static files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Create simple nginx config for serving static React app
RUN echo 'server { \
    listen 80; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]