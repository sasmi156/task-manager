# Personal Task Management System (Backend)

Spring Boot 3 · Java 17 · MySQL 8 · Spring Data JPA · Spring Security (JWT)

## Features
- Register / login with JWT authentication (BCrypt-hashed passwords)
- Per-user task CRUD (users can only see and modify their own tasks)
- Status (`TODO`, `IN_PROGRESS`, `DONE`), priority (`LOW`, `MEDIUM`, `HIGH`), due date
- Filtering, keyword search, sorting and pagination
- Dashboard stats (counts per status, total, overdue)
- Request validation and consistent JSON error responses

## Run
1. Start MySQL (either way):
   - `docker compose up -d`  (user `root`, password `root`, database `task_manager`)
   - or use your own MySQL and set `DB_USERNAME` / `DB_PASSWORD` env vars
2. `mvn spring-boot:run`  (API at http://localhost:8080)

Tables are created automatically (`spring.jpa.hibernate.ddl-auto=update`).
Set `JWT_SECRET` (32+ characters) in any real deployment.

## API
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create account, returns JWT |
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/tasks` | Create task |
| GET | `/api/tasks` | List (params: `status`, `priority`, `q`, `page`, `size`, `sortBy`, `direction`) |
| GET | `/api/tasks/{id}` | Get one task |
| PUT | `/api/tasks/{id}` | Update task |
| PATCH | `/api/tasks/{id}/status` | Change status only |
| DELETE | `/api/tasks/{id}` | Delete task |
| GET | `/api/tasks/stats` | Counts by status + overdue |

All `/api/tasks/**` endpoints need the header `Authorization: Bearer <token>`.

## Quick test (curl)
```bash
curl -X POST localhost:8080/api/auth/register -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","password":"secret123"}'

TOKEN=<paste token from response>

curl -X POST localhost:8080/api/tasks -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"Finish report","description":"Q4 summary","priority":"HIGH","dueDate":"2026-12-01"}'

curl "localhost:8080/api/tasks?status=TODO&priority=HIGH&q=report" -H "Authorization: Bearer $TOKEN"

curl -X PATCH localhost:8080/api/tasks/1/status -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"status":"DONE"}'

curl localhost:8080/api/tasks/stats -H "Authorization: Bearer $TOKEN"
```

## Structure
```
controller/  REST endpoints           service/    business logic
repository/  Spring Data JPA          model/      JPA entities + enums
dto/         request/response records security/   JWT service, filter, config
exception/   ApiException + global handler
```
