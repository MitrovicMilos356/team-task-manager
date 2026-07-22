# Team Task Manager

A full-stack team task management app: authentication, an admin panel (users, projects, project members), and a member-facing dashboard with projects, tasks, comments, and task history.

- **front/** — Vue 3 SPA (Vue Router, Pinia, Axios, Vite)
- **back/** — Spring Boot REST API (Spring Data JPA, Spring Security/JWT, springdoc-openapi)
- **db/** — MySQL schema and seed data, run automatically via Docker

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

This starts MySQL 8 on `localhost:3306` and automatically runs `db/init/01_schema.sql` and `db/init/02_seed.sql` on first boot, creating the schema and seeding sample data.

**2. Start the backend API**

```bash
cd back
cp .env.example .env
mvn spring-boot:run
```

API available at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

**3. Start the frontend**

```bash
cd front
cp .env.example .env
npm install
npm run dev
```

App available at `http://localhost:5173`.

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

`down -v` removes the MySQL data volume, so the init scripts run again on the next `up`.

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
| `JWT_EXPIRATION_MINUTES` | `120` | JWT token lifetime |

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
| Users | GET | `/users/me` |
| Users (admin) | GET / POST | `/users` |
| Users (admin) | PUT | `/users/{id}` |
| Users (admin) | PATCH | `/users/{id}/status` |
| Projects | GET / POST | `/projects` |
| Projects | GET / PUT / DELETE | `/projects/{id}` |
| Project members | GET / POST | `/projects/{projectId}/members` |
| Project members | DELETE | `/projects/{projectId}/members/{userId}` |
| Tasks | GET / POST | `/tasks` |
| Tasks | GET / PUT / DELETE | `/tasks/{id}` |
| Tasks | PATCH | `/tasks/{id}/status` |
| Tasks | PATCH | `/tasks/{id}/assignee` |
| Task comments | GET / POST | `/tasks/{id}/comments` |
| Task history | GET | `/tasks/{id}/history` |
| Dashboard | GET | `/dashboard/statistics` |

Protected routes require an `Authorization: Bearer <token>` header, obtained from `/auth/login`.

## Project structure

```
team-task-manager/
├── back/                  # Spring Boot API
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
├── db/init/               # SQL run automatically on first `docker compose up`
├── docker-compose.yml      # MySQL service
```
