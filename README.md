# Food Rescue Hub

Food Rescue Hub is a full-stack web app that connects suppliers, consumers, and admins to reduce food waste.

## Local Startup Guide

This README is for local startup only (no Docker app startup path).

### Prerequisites

- Java 17
- Node.js 20+
- npm
- Git
- Network access to `13.228.183.177:33306`

### Default Local Ports

- Frontend (Vite): `5174`
- Backend (Spring Boot): `8080`
- AWS MySQL: `13.228.183.177:33306`

## 1. Clone the repository

```bash
git clone https://github.com/YukiOvOb/Food-Rescue-Hub-.git
cd Food-Rescue-Hub-
```

## 2. Use existing AWS MySQL (team default)

Use the existing shared database:

- Host: `13.228.183.177`
- Port: `33306`
- Database: `frh`
- Username: `frh_user`
- Password: `123456`

Before starting backend, make sure your network can reach `13.228.183.177:33306`.

## 3. Run backend locally

Open Terminal 1, then run from `backend/`.

PowerShell:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="local"
$env:LOCAL_DB_URL="jdbc:mysql://13.228.183.177:33306/frh?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:LOCAL_DB_USERNAME="frh_user"
$env:LOCAL_DB_PASSWORD="123456"
.\mvnw.cmd spring-boot:run
```

Backend should be available at:

- `http://localhost:8080`
- `http://localhost:8080/api/hello`

## 4. Run frontend locally

Open Terminal 2, then run from `frontend/`:

```bash
cd frontend
npm install
npm run dev
```

Frontend should be available at:

- `http://localhost:5174`

The frontend proxies `/api` calls to `http://localhost:8080`.

## 5. Stop the app

- Stop backend: `Ctrl + C` in Terminal 1
- Stop frontend: `Ctrl + C` in Terminal 2

## Test and Coverage (Backend)

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

JaCoCo HTML report:

- `backend/target/site/jacoco/index.html`

## Troubleshooting

1. Backend test fails with `Driver org.h2.Driver claims to not accept jdbcUrl`  
   Clear datasource overrides before running tests:
   ```powershell
   Remove-Item Env:SPRING_DATASOURCE_URL,Env:SPRING_DATASOURCE_USERNAME,Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
   ```

2. Backend cannot connect to MySQL  
   Verify `LOCAL_DB_URL`, `LOCAL_DB_USERNAME`, `LOCAL_DB_PASSWORD` and confirm access to `13.228.183.177:33306` from your network.

3. Frontend cannot call backend  
   Confirm backend is running on `http://localhost:8080` and frontend is running from `frontend/` via `npm run dev`.
