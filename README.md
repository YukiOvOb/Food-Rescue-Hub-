# Food Rescue Hub

Food Rescue Hub is a full-stack web app that connects suppliers, consumers, and admins to reduce food waste.

## Local Startup Guide

This README focuses on local startup (frontend + backend on your machine).

## Prerequisites

- Java 17
- Node.js 20+
- npm
- Git
- Docker Desktop (recommended for local MySQL)

## Local Ports

- Frontend (Vite): `5174`
- Backend (Spring Boot): `8080`
- MySQL (if local container): `33306`

## 1. Clone the repository

```bash
git clone https://github.com/YukiOvOb/Food-Rescue-Hub-.git
cd Food-Rescue-Hub-
```

## 2. Choose your database mode

### Option A (recommended): Local MySQL container

Start only MySQL from `docker-compose.yml`:

```bash
docker compose up -d mysql
```

Use these backend DB values:

- Host: `localhost`
- Port: `33306`
- Database: `frh`
- Username: `frh_user`
- Password: `123456`
- Seed: automatic on first backend startup (when DB is empty)

### Option B: Shared team MySQL (remote)

Use the shared DB:

- Host: `13.228.183.177`
- Port: `33306`
- Database: `frh`
- Username: `frh_user`
- Password: `123456`

Make sure your network can reach `13.228.183.177:33306`.

## 3. Run backend locally

Open Terminal 1 and run from `backend/`.

PowerShell (Option A: local MySQL):

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
.\mvnw.cmd spring-boot:run
```

PowerShell (Option B: shared MySQL on AWS server):

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
$env:LOCAL_DB_URL="jdbc:mysql://13.228.183.177:33306/frh?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:LOCAL_DB_USERNAME="frh_user"
$env:LOCAL_DB_PASSWORD="123456"
.\mvnw.cmd spring-boot:run
```

Bash/Zsh:

```bash
cd backend
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
```

First backend startup with a fresh DB may take longer because it creates schema then seeds demo data from `backend/src/main/resources/data.sql`.

Backend health check:

- `http://localhost:8080`
- `http://localhost:8080/api/hello`

Seeded demo logins (password: `password123`):

- Consumer: `alice.tan@email.com`
- Supplier: `bakery@breadtalk.sg`

## 4. Run frontend locally

Open Terminal 2:

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

- `http://localhost:5174`

Vite proxies `/api` and `/uploads` to `http://localhost:8080`.

## 5. Stop local services

- Stop backend: `Ctrl + C` in Terminal 1
- Stop frontend: `Ctrl + C` in Terminal 2
- If using local MySQL container: `docker compose stop mysql`

## Backend Test and Coverage

From `backend/`:

PowerShell:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Bash:

```bash
./mvnw test
./mvnw verify
```

JaCoCo report:

- `backend/target/site/jacoco/index.html`

## Troubleshooting

1. Backend test fails with `Driver org.h2.Driver claims to not accept jdbcUrl`  
   Clear datasource overrides before running tests:

```powershell
Remove-Item Env:SPRING_DATASOURCE_URL,Env:SPRING_DATASOURCE_USERNAME,Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
Remove-Item Env:LOCAL_DB_URL,Env:LOCAL_DB_USERNAME,Env:LOCAL_DB_PASSWORD -ErrorAction SilentlyContinue
```

2. Backend cannot connect to MySQL  
   Verify `LOCAL_DB_URL`, `LOCAL_DB_USERNAME`, `LOCAL_DB_PASSWORD`.  
   For Option A, confirm container is running: `docker compose ps mysql`.

3. Need to reseed local DB from scratch  
   Remove MySQL volume and restart:

```bash
docker compose down -v
docker compose up -d mysql
```

4. Frontend cannot call backend  
   Confirm backend is running on `http://localhost:8080` and frontend on `http://localhost:5174`.
