FROM node:20-alpine AS build
WORKDIR /app
# copy entire context so we can handle both presence/absence of package.json
COPY . .
# If package.json exists, install and build; otherwise create a minimal placeholder dist
RUN if [ -f package.json ]; then \
			npm install && npm run build; \
		else \
			mkdir -p /app/dist && echo "<!doctype html><html><head><meta charset=\"utf-8\"><title>Placeholder</title></head><body><h1>Frontend placeholder</h1></body></html>" > /app/dist/index.html; \
		fi

FROM alpine:3.18 AS out
RUN mkdir -p /out
COPY --from=build /app/dist /out
