# Team Task Manager

A full-stack team task management app: authentication, an admin panel (users, projects, project members), and a member-facing dashboard with projects, tasks, comments, and task history.

- **front/** — Vue 3 SPA (Vue Router, Pinia, Axios, Vite)
- **back/** — Spring Boot REST API (Spring Data JPA, Spring Security/JWT, springdoc-openapi)
- **db/** — MySQL runs via Docker; schema and seed data are Flyway migrations under `back/src/main/resources/db/migration/`, applied automatically on backend startup

See [project_team_task_manager.html](project_team_task_manager.html) for the full product spec and business logic.

## Prerequisites

| Tool | Version |
|---|---|
| Docker / Docker Compose | any recent version |
| Node.js | 18+ |
| Java | 17+ |
| Maven | 3.9+ (or use the included `mvnw` if present) |

## Quick start

Run these from the repository root, in order, in three terminals.

**1. Start the database (Docker)**

```bash
docker compose up -d db
```

This starts MySQL 8 on `localhost:3306` with an empty database.

**2. Start the backend API**

```bash
cd back
cp .env.example .env
mvn spring-boot:run
```

On startup, Flyway automatically applies the schema and seed-data migrations from `back/src/main/resources/db/migration/` against the MySQL instance. API available at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

**3. Start the frontend**

```bash
cd front
cp .env.example .env
npm install
npm run dev -- --host
```

App available at `http://192.168.1.11:5173/`.

**4. Log in**

The database is seeded with two users (password for both: `Admin123!`):

| Email | Role |
|---|---|
| `ana.petrovic@example.com` | admin |
| `marko.jovanovic@example.com` | member |

## Resetting the database

To wipe and re-seed the database from scratch:

```bash
docker compose down -v
docker compose up -d db
```

`down -v` removes the MySQL data volume. On the next backend startup, Flyway detects the empty schema and re-applies all migrations (schema + seed data) from scratch.

## Running tests

The backend test suite covers login (success/failure), task creation and validation, project-access authorization, task status changes with history recording, and dashboard statistics. It runs against an in-memory H2 database (via Flyway migrations) so it needs no external services.

```bash
cd back
mvn test
```

The frontend currently has no automated test suite (see Known limitations below).

## Configuration

Each app has its own `.env`, copied from an `.env.example`. Defaults match `docker-compose.yml` and work out of the box for local development.

**`back/.env`**

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Port the API listens on |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_USER` | `appuser` | MySQL user |
| `DB_PASSWORD` | `apppassword` | MySQL password |
| `DB_NAME` | `myapp_dev` | MySQL database name |
| `JWT_SECRET` | *(dev placeholder)* | Secret used to sign JWTs — change for anything beyond local dev |
| `JWT_EXPIRATION_MINUTES` | `120` | Access token lifetime |
| `REFRESH_TOKEN_EXPIRATION_DAYS` | `7` | Refresh token lifetime |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*` | Comma-separated list of allowed CORS origin patterns. In production, set this to the deployed frontend's origin(s), e.g. `https://your-app.pages.dev` |

**`front/.env`**

| Variable | Default | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | Base URL the frontend uses to call the API |

## Technology stack

- **Frontend:** Vue 3, Vue Router, Pinia, Axios, Vite
- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security (JWT via jjwt), springdoc-openapi
- **Database:** MySQL 8
- **Infra/tooling:** Docker Compose, Git, Swagger/OpenAPI, Postman

## API overview

All endpoints are prefixed with `/api`. Full interactive docs are in Swagger UI once the backend is running.

| Area | Method | Path |
|---|---|---|
| Auth | POST | `/auth/register` |
| Auth | POST | `/auth/login` |
| Auth | POST | `/auth/refresh` |
| Auth | POST | `/auth/logout` |
| Users | GET | `/users/me` |
| Users (admin) | GET (paginated) / POST | `/users` |
| Users (admin) | PUT | `/users/{id}` |
| Users (admin) | PATCH | `/users/{id}/status` |
| Users (admin) | GET | `/users/export` (CSV) |
| Projects | GET (paginated) / POST | `/projects` |
| Projects | GET / PUT / DELETE | `/projects/{id}` |
| Projects (admin) | GET | `/projects/export` (CSV) |
| Project members | GET / POST | `/projects/{projectId}/members` |
| Project members | DELETE | `/projects/{projectId}/members/{userId}` |
| Tasks | GET (paginated) / POST | `/tasks` |
| Tasks | GET / PUT / DELETE | `/tasks/{id}` |
| Tasks | PATCH | `/tasks/{id}/status` |
| Tasks | PATCH | `/tasks/{id}/assignee` |
| Tasks | GET | `/tasks/export` (CSV, same filters as the list endpoint) |
| Task comments | GET / POST | `/tasks/{id}/comments` |
| Task history | GET | `/tasks/{id}/history` |
| Dashboard | GET | `/dashboard/statistics` |
| Audit log (admin) | GET (paginated) | `/audit-log` |

Protected routes require an `Authorization: Bearer <token>` header, obtained from `/auth/login`. Access tokens are short-lived; `/auth/refresh` exchanges a valid refresh token for a new access + refresh token pair (rotating — the old refresh token is invalidated), and `/auth/logout` revokes the current refresh token server-side.

Paginated list endpoints (`/users`, `/projects`, `/tasks`, `/audit-log`) accept standard Spring Data query params: `page` (0-based, default `0`), `size` (default `20`), and `sort` (e.g. `sort=dueDate,asc`; tasks default to `sort=createdAt,desc`). Responses are a Spring `Page` object (`content`, `totalElements`, `totalPages`, etc). Export endpoints ignore pagination and return the full matching result set as a CSV file download.

## Project structure

```
team-task-manager/
├── back/                  # Spring Boot API
│   ├── Dockerfile
│   └── src/main/java/com/ttm/back/
│       ├── controller/    # REST controllers
│       ├── dto/           # Request/response payloads
│       ├── model/         # JPA entities
│       ├── repository/    # Spring Data repositories
│       ├── security/      # JWT auth filter, guards
│       └── service/       # Business logic
├── front/                 # Vue 3 SPA
│   └── src/
│       ├── views/         # Route-level pages (dashboard, projects, tasks, admin/*)
│       ├── components/    # Shared components
│       ├── services/      # Axios API clients
│       ├── store/         # Pinia stores
│       └── router/        # Vue Router config
├── docker-compose.yml     # MySQL service
```

Flyway migrations (schema + seed data) live under `back/src/main/resources/db/migration/` and run automatically when the backend starts.

## Deployment

The backend ships with a multi-stage `back/Dockerfile` (Maven build → `eclipse-temurin:17-jre-jammy` runtime) so it can run as a container independent of the host's Java/Maven install. The frontend is a static Vite build, deployable to any static host (e.g. Cloudflare Pages). MySQL is expected to run outside the backend container — either via `docker-compose.yml` (local dev) or installed directly on the host (production).

**Building and running the backend image**

```bash
cd back
docker build -t team-task-backend:latest .

docker run -d \
  --name team-task-backend \
  --restart unless-stopped \
  --network host \
  --env-file .env \
  team-task-backend:latest
```

`--network host` lets the container reach a MySQL instance bound to `127.0.0.1` on the same machine without exposing any extra ports. The backend listens on `SERVER_PORT` (default `8080`) — put a reverse proxy (e.g. Nginx) in front of it for TLS; don't expose the backend port directly to the internet. Flyway migrations run automatically on container startup, same as local dev.

**Health check**

`GET /api/health` returns `{"status": "UP"}` with no authentication required — use it to verify the container/proxy is serving traffic (`curl http://127.0.0.1:8080/api/health`).

**Checking logs**

```bash
docker logs --tail 200 team-task-backend
```

**Redeploying after a `git pull`**

```bash
cd /path/to/team-task-manager
git pull
cd back
docker build -t team-task-backend:latest .
docker rm -f team-task-backend
docker run -d \
  --name team-task-backend \
  --restart unless-stopped \
  --network host \
  --env-file .env \
  team-task-backend:latest
```

**CORS in production**

Set `CORS_ALLOWED_ORIGINS` in the server's `back/.env` to the deployed frontend's exact origin (e.g. `https://your-app.pages.dev`) — the default only allows local/private-network origins for dev.

**Frontend build**

```bash
cd front
npm install
npm run build
```

Output goes to `front/dist/`. Set `VITE_API_BASE_URL` (build-time env var) to the production API's public HTTPS URL before building — e.g. `https://api.your-domain.tld/api`. `front/public/_redirects` (copied into `dist/` on build) provides the SPA fallback (`/* /index.html 200`) needed for Vue Router's history mode on static hosts like Cloudflare Pages.

**Secrets**

None of `back/.env`, `front/.env`, `JWT_SECRET`, or DB credentials are committed — only `.env.example` templates are. Production secrets live solely in the server's environment/`.env` file and the static host's build-time environment variable settings.

## Known limitations & future improvements

- No frontend automated test suite (backend has JUnit/MockMvc coverage; see "Running tests" above).
- Non-admin users' paginated project list is sliced in memory after fetching all of a user's memberships, rather than via a paginated DB query — acceptable at this project's scale, but wouldn't scale to a user with very many project memberships.
- No Kanban board, email notifications, file attachments, or WebSocket notifications — listed as optional/bonus features in the spec and not implemented. (Dark mode, CSV export, rotating refresh tokens, and an admin audit log — also bonus features — are implemented.)
- The audit log covers admin actions on users and projects (create/update/status/deactivate, member add/remove) but not auth events (login/logout) or task field changes (those are already covered separately by per-task history, see `/tasks/{id}/history`).
