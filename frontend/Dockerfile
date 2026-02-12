# Build stage - Node 20 for Vite build
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files for dependency caching
COPY frontend/package.json frontend/package-lock.json* ./

# Install dependencies
RUN npm ci

# Inject Vite env var at build time so it is bundled into frontend assets
ARG VITE_GOOGLE_MAPS_API_KEY
ENV VITE_GOOGLE_MAPS_API_KEY=$VITE_GOOGLE_MAPS_API_KEY

# Copy source code
COPY frontend/ ./

# Build production assets
RUN npm run build

# Production stage - nginx to serve static files and proxy API
FROM nginx:1.27-alpine

# Remove default nginx content and update OS packages
RUN rm -rf /usr/share/nginx/html/* \
    && apk update \
    && apk upgrade --no-cache \
    && mkdir -p /tmp/nginx /var/cache/nginx \
    && chown -R nginx:nginx /usr/share/nginx/html /etc/nginx/conf.d /tmp/nginx /var/cache/nginx \
    && sed -i -e 's|/var/run/nginx.pid|/tmp/nginx.pid|' -e 's|/run/nginx.pid|/tmp/nginx.pid|' /etc/nginx/nginx.conf

# Copy built static files from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Create nginx config for serving static React app as non-root user
RUN echo 'server { \
    listen 8080; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location = /healthz { \
        return 200 "ok"; \
        add_header Content-Type text/plain; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/healthz || exit 1

USER nginx
CMD ["nginx", "-g", "daemon off;"]
