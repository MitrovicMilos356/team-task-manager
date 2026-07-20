# Team Task Manager

## Scope of this increment

This increment adds the authentication entry point only:

- **front/** — Vue 3 landing page with **Login** and **Sign up** screens (Vue Router, Pinia, Axios).
- **back/** — Spring Boot REST API to register and authenticate users against MySQL (`POST /api/auth/register`, `POST /api/auth/login`, `GET /api/users/me`).

The rest of the application (projects, tasks, comments, dashboard) is out of scope for this increment; see `project_team_task_manager.html` for the full spec.

## Technologies

- **Frontend:** Vue 3, Vue Router, Pinia, Axios, Vite
- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, springdoc-openapi (Swagger UI), jjwt
- **Database:** MySQL 8

## Prerequisites

- Node.js **18+** (this repo's Vite/Vue tooling requires it)
- Java **17+** and Maven **3.9+**
- MySQL 8, or Docker (via `docker-compose.yml`)

## Database

```bash
docker compose up -d db
```

This starts MySQL and runs `db/init/01_schema.sql` and `db/init/02_seed.sql` automatically. Passwords in the DB are stored as **bcrypt hashes** in the `password_hash` column — never plaintext.

## Backend (`back/`)

```bash
cd back
cp .env.example .env   # then edit values as needed
mvn spring-boot:run
```

Environment variables (see `back/.env.example`): `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `SERVER_PORT`.

API runs at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Endpoints in this increment

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Create a user (first name, last name, email, password, role, active) |
| POST | `/api/auth/login` | Validate email + password, returns a JWT and user profile |
| GET | `/api/users/me` | Returns the authenticated user (requires `Authorization: Bearer <token>`) |

## Frontend (`front/`)

```bash
cd front
cp .env.example .env   # then edit VITE_API_BASE_URL if needed
npm install
npm run dev
```

App runs at `http://localhost:5173`, with `/login` and `/signup` routes.

## Known limitations

- Login/signup is not yet integrated with the rest of the app (projects/tasks screens don't exist yet).
- `GET /api/users/me` is the only protected route; broader role-based authorization will be added with the rest of the API.
- Not verified against a live Node 18+/Maven toolchain in this environment — this sandbox only had Node 14 and no Maven available, so `npm run dev` and `mvn spring-boot:run` should be run and sanity-checked on a machine with the prerequisite versions before relying on it.
