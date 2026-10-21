# School schedules

<img src="src/main/resources/static/favicon.svg" alt="Surok" height="64">

[![Build](https://github.com/eshabakhov/surok/actions/workflows/build.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/build.yml)
[![Deploy](https://github.com/eshabakhov/surok/actions/workflows/deploy.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/deploy.yml)
[![actionlint](https://github.com/eshabakhov/surok/actions/workflows/actionlint.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/actionlint.yml)
[![yamllint](https://github.com/eshabakhov/surok/actions/workflows/yamllint.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/yamllint.yml)
[![shellcheck](https://github.com/eshabakhov/surok/actions/workflows/shellcheck.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/shellcheck.yml)
[![trufflehog-oss](https://github.com/eshabakhov/surok/actions/workflows/trufflehog-oss.yml/badge.svg)](https://github.com/eshabakhov/surok/actions/workflows/trufflehog-oss.yml)
[![Lines-of-Code](https://img.shields.io/tokei/lines/github/eshabakhov/surok)](https://github.com/eshabakhov/surok)
[![Hits-of-Code](https://hitsofcode.com/github/eshabakhov/surok)](https://hitsofcode.com/view/github/eshabakhov/surok)
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](https://github.com/eshabakhov/surok/blob/main/LICENSE.txt)

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Docker Required](https://img.shields.io/badge/Docker-Required-2496ED?logo=docker&logoColor=white)
![Release 0.0.1](https://img.shields.io/badge/Release-0.0.1-111827)

**Surok** manages school schedules, classes, teachers, subjects, and classrooms.

## Local development

Install Docker Desktop with Linux containers (or Docker Engine with Compose v2.24+), then run from the repository root:

```sh
docker compose up --build
```

Open [http://localhost:19500/](http://localhost:19500/). Docker builds the application from source and starts it with PostgreSQL. No `.env`, prebuilt JAR, or host Java, Maven, or Node.js installation is needed.

To customize settings, copy `.env.example` to `.env`. After source changes, run the same command again. `docker compose down` stops the services and preserves database data.

## CI build and deploy

The release process uses two separate GitHub Actions workflows:

1. **[Build](.github/workflows/build.yml)** runs tests and frontend linters, builds the JAR and Docker image, and publishes images from `main` to GHCR with commit SHA and `latest` tags.
2. **[Deploy](.github/workflows/deploy.yml)** runs after a successful Build on `main`, pulls the image for that commit, and starts the production stack without rebuilding it. It also supports manual deployment of a previously published commit SHA.

Pull requests run build checks without publishing or deploying images.

See the [development and deployment guide](docs/development-and-deployment.md) for administrator setup, configuration, and server requirements.
