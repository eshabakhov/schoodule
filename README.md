# School schedules

<img src="src/main/resources/static/favicon.svg" alt="Schoodule" height="64">

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Docker Required](https://img.shields.io/badge/Docker-Required-2496ED?logo=docker&logoColor=white)
![Release 0.0.1](https://img.shields.io/badge/Release-0.0.1-111827)

**Schoodule** is an application for school schedule management. It helps organize schools, classes, teachers, subjects, cabinets, and schedules in one place.

To try it locally, make sure Java 21, Docker Desktop, and Node.js are installed, then run the full project check:

```text
mvn clean install -Pqulice
```

The build starts PostgreSQL in Docker, applies Liquibase migrations, generates jOOQ classes, runs tests, lints frontend assets, and packages the application. After that, start the app with:

```text
java -jar target/schoodule-0.0.1.jar
```

To run the packaged application with Docker Compose, build the jar first and then start the stack:

```text
mvn clean install -Pqulice
docker compose up --build
```

Docker Compose reads deployment variables directly from the environment. For deployment from GitHub Actions or a server shell, pass image names, ports, database credentials, OAuth, and bootstrap admin settings through environment variables.

Open `http://localhost:9500/`. If OAuth login is needed, provide `ya-id` and `ya-secret`. If you need to create the first administrator, enable bootstrap admin settings:

```text
BOOTSTRAP_ADMIN_ENABLED=true
BOOTSTRAP_ADMIN_USERNAME=admin
BOOTSTRAP_ADMIN_EMAIL=admin@example.com
BOOTSTRAP_ADMIN_PASSWORD=StrongPass1!
```

## Main Use Cases

Manage the school structure: schools, classes, teachers, subjects, cabinets, and schedules.

Work with role-based access so administrators and school staff can manage data with different permission levels.

Plan and maintain schedules through the web interface and API.

## How to Contribute

Install Java 21, Docker Desktop, and Node.js.

Run the full verification command before changes:

```text
mvn clean install -Pqulice
```

Frontend linting is also available separately:

```text
npm run lint
```

If you change the database schema, update the Liquibase changelog first and then run the normal Maven build. Keep changes small and verify role-sensitive behavior before sending them forward.
