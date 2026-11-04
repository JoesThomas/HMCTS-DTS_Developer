# Caseworker Task Manager

A full-stack task manager developed for the HMCTS DTS developer technical challenge. It provides task creation, retrieval, status updates and deletion, with a browser-based user interface.

## Technology
- Java 21, Spring Boot 3, Spring Web, Spring Data JPA, Jakarta Bean Validation
- File-backed H2 relational database (persists across restarts)
- React 19, TypeScript, Vite
- JUnit 5, Spring Boot MockMvc integration tests

## Requirements
- JDK 21
- Maven 3.9+
- Node.js 20+

## Run locally

In one terminal, from the repository root:

```sh
mvn spring-boot:run
```

In a second terminal:

```sh
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The frontend proxies `/api` to the backend at http://localhost:8080. H2 stores records under `./data/` (ignored by Git).

## Test and build

```sh
mvn test
cd frontend && npm install && npm run build
```

The backend tests cover successful creation/retrieval, invalid requests, missing resources, status changes and deletion.

## API

| Method | Endpoint | Purpose | Success |
| --- | --- | --- | --- |
| POST | `/api/tasks` | Create task | 201, Location header |
| GET | `/api/tasks` | List tasks | 200 |
| GET | `/api/tasks/{id}` | Retrieve task | 200 or 404 |
| PATCH | `/api/tasks/{id}/status` | Update task status | 200 or 404 |
| DELETE | `/api/tasks/{id}` | Delete task | 204 or 404 |

### Create task

```sh
curl -i -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Review documents","description":"Check supporting evidence","status":"TODO","dueAt":"2027-01-10T15:00:00Z"}'
```

`title` must be non-blank and at most 150 characters; `description` is optional and at most 2000 characters. `status` must be `TODO`, `IN_PROGRESS`, or `DONE`. `dueAt` is a required ISO-8601 timestamp with timezone offset.

### Change status

```sh
curl -i -X PATCH http://localhost:8080/api/tasks/1/status \
  -H 'Content-Type: application/json' -d '{"status":"DONE"}'
```

Validation errors return 400, missing tasks return 404, and unexpected server failures return 500. The frontend displays API errors rather than silently ignoring them.

## Design choices and limitations

The REST API is deliberately small and uses HTTP status codes and input validation. A file-backed H2 database keeps local setup simple while meeting the requirement for persistence. Spring Data JPA provides repository operations. The UI sorts tasks by due date and allows status changes and deletion without a full page refresh.

This is a technical demonstration, not a production case-management system. For production, add authentication and role-based authorisation, audit logging, database migrations, observability, pagination, and an externally managed database. The development CORS setting only permits localhost:5173 and is not a substitute for authentication.

## Anonymity

This project contains no candidate name, personal email, school, employer history or other identifying details in the authored source files. **Before submitting**, check GitHub account name, repository owner, commit author metadata, Git history and any linked profile for personal identifiers. GitHub ownership and commit metadata can still reveal identity.
