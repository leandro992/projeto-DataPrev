Dataprev CNAB Processor
=======================

A Spring Boot service that processes Dataprev CNAB remessa/retorno flows using Spring Batch, with SQL Server persistence and a local CONNECT (SFTP) simulation for end-to-end testing.

Quick Start
-----------

- Requirements: Java 21, Docker Desktop, Gradle Wrapper.
- Start SQL Server: `docker compose up -d sqlserver`
- Start CONNECT sim: `docker compose -f docker-compose.connect.yml up -d`
- Run app (dev): `./gradlew bootRun --args='--spring.profiles.active=dev'`

Profiles & Config
-----------------

- dev (local defaults):
  - DB: `jdbc:sqlserver://localhost:1433;databaseName=dataprevdb` with `appuser/AppTeste#1`.
  - CNAB output: `./connect-volume/outbox`, returns in `./connect-volume/retorno`.
  - Flyway clean allowed for development only.
- prod (safe defaults):
  - Set environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
  - Flyway clean disabled, show-sql disabled, Batch schema auto-init disabled.
  - Run: `JAVA_OPTS=... ./gradlew bootRun --args='--spring.profiles.active=prod'` or as a container.

Env vars (prod):
- `DB_URL` (e.g. `jdbc:sqlserver://host:1433;databaseName=dataprevdb;encrypt=true;trustServerCertificate=false`)
- `DB_USER`, `DB_PASSWORD`

Endpoints
---------

- `GET /health` – basic health.
- `GET /health/batch-test` – executes a simple Spring Batch test job.
- `GET /api/creditos` – list créditos.
- `GET /api/creditos/{id}` – get by id.
- `GET /api/creditos/tipo/{tipo}` – filter by `TipoCredito`.
- `POST /api/creditos` – create.
- `PUT /api/creditos/{id}` – update.
- `DELETE /api/creditos/{id}` – delete.
- `POST /api/creditos/processar-remessa` – run remessa job (`gerarArquivoConcessaoJob`).
- `POST /api/creditos/processar-cnab` – run CNAB job (reads `app.cnab.file`, writes to outbox).

OpenAPI/Swagger: `http://localhost:8080/swagger-ui.html`

CONNECT Simulation
------------------

- Start: `./test-connect-flow.sh` or `docker compose -f docker-compose.connect.yml up -d`
- Outbox: `connect-volume/outbox/` (app writes here)
- Returns: `connect-volume/retorno/` (mock writes receipts and returns)
- Inbox: `connect-volume/inbox/` (archived originals)

Notes
-----

- Credentials are sourced from env in `application.yml`; `application-dev.yml` holds dev-only defaults.
- Flyway `clean` is disabled by default; do not enable outside dev.
- Duplicated classes were consolidated to avoid conflicting beans and imports.
