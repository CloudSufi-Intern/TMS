# TMS — Ticket Management System

Full-stack helpdesk app. Spring Boot 3 + MySQL on the back, React 19 + Vite on the front.

## Features

- JWT-based auth with auto-logout on token expiry (24-hour tokens)
- Tickets with priority, status, SLA, assignee, audit history
- Comments with attachments (16 MB cap), `@`-mentions, sort, author filter
- Attachments at both ticket and comment level (PNG/JPG/PDF, MEDIUMBLOB stored)
- HTML email notifications with full ticket history + comments and a deep link
- Status transition guard — RESOLVED and CLOSED are terminal
- RBAC — only ticket creator or assignee can update; only IT can reassign
- Sortable ticket list (createdAt, updatedAt, priority, status, title)
- Configurable CORS origins, base URL, and secrets via environment variables

---

## Quick start (Docker Compose)

The simplest path. Brings up MySQL, the backend, and the frontend as one stack.

```bash
# 1. Configure (copy and edit)
cp tms-backend/.env.example .env
cp tms-frontend/.env.example tms-frontend/.env

# 2. Run
docker compose up --build

# 3. Open
# Frontend: http://localhost:3000
# Backend:  http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui.html
```

Sign up, log in, and you're in.

To stop: `docker compose down`. To wipe the database: `docker compose down -v`.

---

## Local development (without Docker)

You'll need: Java 21, Maven 3.8+, Node 18+, MySQL 8.

```bash
# 1. Database
mysql -u root -e "CREATE DATABASE IF NOT EXISTS TMS;"

# 2. Backend
cd tms-backend
cp .env.example .env   # edit with your DB password and Gmail app password
./mvnw spring-boot:run

# 3. Frontend (new terminal)
cd tms-frontend
cp .env.example .env
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`, backend at `http://localhost:8080`.

---

## Free-tier deployment (Render)

The included `render.yaml` is a Render Blueprint. The free plan covers both web services. For the database, point the backend at any free MySQL provider — Aiven, Railway, or Clever Cloud all offer a free tier that fits this app.

### Steps

1. **Push to GitHub.** Render reads from a connected repo.
2. **Provision a free MySQL** somewhere. Grab the JDBC URL, username, password.
3. **In Render, click "New → Blueprint"** and select the repo. It picks up `render.yaml` and creates two services.
4. **Set the secrets** (the ones with `sync: false` in `render.yaml`):
   - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
   - `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail App Password)
   - `TMS_BASE_URL`, `TMS_CORS_ALLOWED_ORIGINS` — set to the deployed frontend URL once it's known (you'll do this after the first deploy)
   - `VITE_API_BASE_URL` on the frontend service — set to the deployed backend URL
5. **Deploy.** First boot takes 5–10 minutes (Maven dependency download).
6. **Update CORS + base URL.** After you have both URLs, fill in `TMS_BASE_URL` and `TMS_CORS_ALLOWED_ORIGINS` on the backend service and trigger a redeploy.

### Free-tier caveats

- Render free instances spin down after 15 min idle. First request after spin-down takes ~30s. That's why login may feel slow on the first hit.
- Free MySQL providers usually cap storage at 1 GB. With 16 MB BLOB attachments stored in-DB, plan for ~50 attachments before you hit the cap. Upgrade to S3-backed storage if you outgrow this.

---

## Environment variables

### Backend (`tms-backend/.env`)

| Variable | Default | Required for prod |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/TMS?createDatabaseIfNotExist=true` | yes |
| `SPRING_DATASOURCE_USERNAME` | `root` | yes |
| `SPRING_DATASOURCE_PASSWORD` | empty | yes |
| `SPRING_JPA_DDL_AUTO` | `update` | recommend `validate` in prod |
| `JWT_SECRET` | (dev fallback) | **yes** — generate 64+ chars |
| `JWT_EXPIRATION` | `86400000` (24h) | optional |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | empty | yes (Gmail App Password) |
| `TMS_BASE_URL` | `http://localhost:5173` | yes |
| `TMS_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | yes (comma-separated) |
| `TMS_MAX_ATTACHMENT_BYTES` | `16777216` | optional |
| `PORT` | `8080` | Render sets this |

### Frontend (`tms-frontend/.env`)

| Variable | Default |
|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` |

The frontend bakes `VITE_API_BASE_URL` into the static bundle at build time, so changing it requires a rebuild.

---

## Project layout

```
TMS/
├── docker-compose.yml         # local dev stack
├── render.yaml                # Render free-tier blueprint
├── tms-backend/
│   ├── src/main/java/cloudsufi/nextgen/tms/
│   │   ├── config/            # SecurityConfig, CorsConfig
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # request/response DTOs
│   │   ├── entity/            # JPA entities
│   │   ├── exception/         # GlobalExceptionHandler
│   │   ├── filter/            # JwtAuthenticationFilter
│   │   ├── repository/        # JPA repositories
│   │   ├── service/           # business logic
│   │   └── util/              # JwtUtil
│   └── src/main/resources/application.properties
└── tms-frontend/
    └── src/
        ├── components/        # Header, Toolbar, TicketRow, etc.
        ├── context/           # TicketContext
        ├── hooks/             # useTickets, useToast
        ├── pages/             # Login, Signup, Dashboard, ticketDetails
        ├── services/          # AuthService, TicketService, etc.
        └── utils/             # apiClient (axios), auth (JWT helpers)
```

---

## API summary

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/signup` | Register |
| POST | `/api/auth/login` | Returns JWT |
| GET | `/api/tickets/my?sortBy=&sortDir=` | List user's tickets |
| GET | `/api/tickets/{id}` | Full ticket detail |
| POST | `/api/tickets` | Create ticket (multipart) |
| PATCH | `/api/tickets/{id}` | Partial update |
| GET | `/api/tickets/{id}/comments?sortDir=&author=` | List comments |
| POST | `/api/tickets/{id}/comments` | Add comment (multipart or JSON) |
| GET | `/api/attachments/{id}/download` | Download a file |
| GET | `/api/user`, `/api/user/search` | User lookup, prefix search |
| GET | `/api/dashboard` | Status counts |
| GET | `/actuator/health` | Health probe |

Full schema: `http://localhost:8080/swagger-ui.html`

