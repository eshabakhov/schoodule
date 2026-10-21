# Разработка, сборка и деплой Surok

## Запуск одной командой

Нужен только Docker Desktop с Linux containers или Docker Engine с Compose v2.24+. Из корня проекта выполните:

```sh
docker compose up --build
```

Откройте [приложение](http://localhost:19500/). Java, Maven и Node.js на компьютере не нужны. Предварительно собирать JAR, создавать `.env` или включать профиль не требуется. По умолчанию запускаются только `surok` и `postgres`; TLS и мониторинг не нужны.

Для создания первого администратора задайте `BOOTSTRAP_ADMIN_ENABLED=true` в `.env`; имя, email и пароль задаются общими `BOOTSTRAP_ADMIN_*`. После создания отключите bootstrap. По умолчанию он выключен в обоих режимах. Для фонового запуска можно добавить `-d`. `docker compose down` останавливает окружение и сохраняет данные PostgreSQL. Не добавляйте `-v`, если данные нужно сохранить.

Первая сборка скачивает Maven/JDK и зависимости. Последующие используют кеш Docker и Maven.

## Как устроена сборка

Многоэтапный Dockerfile собирает приложение из `pom.xml` и `src`. Приложение выполняет SQL-запросы с параметрами через jOOQ `DSLContext`, без сгенерированных классов. Для сборки JAR не нужны PostgreSQL, генерация кода или доступ к Docker socket. Liquibase применяет миграции к рабочей БД при запуске приложения; интеграционные тесты используют отдельные PostgreSQL-контейнеры.

В итоговый JRE-образ копируется только JAR. Maven и JDK в него не попадают. Docker-сборка пакует приложение без тестов и frontend lint; полные проверки выполняются отдельно в CI. Готовый `target/*.jar` на компьютере для такой сборки не используется.

CI выполняет полный Maven `clean verify`, а затем упаковывает уже проверенный JAR через Dockerfile target `prebuilt`. Это сохраняет раздельные Build и Deploy и исключает повторную Maven-сборку в CI.

SQL-запросы и условия фильтрации находятся в `src/main/resources/sql`, сгруппированы по сущностям. `ClasspathSql` читает их из classpath в UTF-8, поэтому они доступны и из исполняемого JAR. Значения передаются отдельно в `DSLContext`: `?` используется для JDBC-параметров, `{0}`, `{1}` — для безопасных jOOQ QueryPart-шаблонов фильтрации, сортировки и пагинации. Транзакции и обработка результатов остаются в Java. После изменения `.sql` повторите `docker compose up --build`.

## Одна конфигурация окружения

В проекте один `docker-compose.yml` и один `.env.example`. `.env` необязателен для локального запуска. Если нужно изменить настройки, один раз скопируйте шаблон в `.env` и измените необходимые значения. Существующий файл не перезаписывается; рабочие env-файлы исключены из Git.

Для обоих режимов используется один набор: `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`. `DB_PORT` по умолчанию равен `15432` и задаёт локальный порт доступа к PostgreSQL; внутри Docker приложение подключается к БД на порту `5432`. `APP_PORT` по умолчанию равен `19500` и задаёт локальный опубликованный порт приложения. `BOOTSTRAP_ADMIN_ENABLED`, `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL` и `BOOTSTRAP_ADMIN_PASSWORD` общие для обоих режимов.

В обоих режимах БД сохраняется в стандартном volume `postgres-data`. Docker Compose автоматически добавляет к его имени префикс проекта: `<project>_postgres-data`. Существующий volume продолжит использоваться при прежнем имени Compose-проекта. Перед переходом остановите старое окружение через `docker compose down --remove-orphans` без `-v`, чтобы не оставлять старые контейнеры с занятыми портами. При изменении credentials уже инициализированный PostgreSQL автоматически не меняет пользователей и пароли.

Для настоящего входа через Яндекс задайте `YA_ID`, `YA_SECRET` и callback `http://localhost:19500/login/oauth2/code/yandex` с учётом выбранного порта. Заглушки позволяют использовать локальный вход по паролю.

`application-dev.yml` удалён. Оба режима используют один сервис `surok` и настройки из общих переменных. Для локального HTTP по умолчанию используется `APP_SECURITY_SECURE_COOKIE=false`; для HTTPS на сервере задайте `true`. Исходники не монтируются в контейнер: после изменений Java, SQL, HTML/CSS/JS или changelog повторите `docker compose up --build`.

## Выпуск и повторный деплой

Push в `main` запускает **Build**: тесты, JAR, Docker image и публикация в `ghcr.io/<owner>/<repository>` с тегами полного SHA и `latest`. Затем отдельный **Deploy** выкладывает этот SHA. В PR образы проверяются, но не публикуются и не деплоятся.

Для повторного деплоя откройте GitHub Actions → Deploy → Run workflow и укажите в `revision` полный SHA уже опубликованного Build. Для отката укажите SHA предыдущего опубликованного релиза. Короткий SHA, имя ветки и `latest` не принимаются. Результат смотрите в Actions → Deploy; дополнительные локальные инструменты для этого не нужны.

Deploy получает image и конфигурацию одного коммита, сохраняет серверные `.env` и volumes, включает профиль `production` и явно запускает `haproxy` с его зависимостями: приложение, PostgreSQL, Grafana и Prometheus. Используются те же сервисы `surok` и `postgres`, что и локально, а `IMAGE` выбирает опубликованный образ релиза. Серверная сборка запрещена через `--no-build`; workflow ждёт healthcheck и перезапускает HAProxy.

Для ручного старта подготовленного серверного стека используйте `docker compose --profile production up -d --no-build --pull always --wait haproxy`. Профиль `production` добавляет HAProxy, Grafana и Prometheus к общим сервисам приложения и БД. Перед запуском задайте `IMAGE=ghcr.io/<owner>/<repository>:<SHA>` в серверном `.env`; CI передаёт этот параметр автоматически.

Откат image не отменяет миграции Liquibase. Перед несовместимыми изменениями схемы нужны backup и план восстановления.

## Однократная подготовка сервера

Нужны Linux amd64, Docker Engine с Compose v2.24+, SSH, Bash, rsync и envsubst (`gettext-base` на Ubuntu/Debian). Deploy-пользователь должен иметь доступ к Docker и каталогу приложения. DNS направьте на сервер, откройте порт 443.

Создайте каталог, например `/opt/surok`, и положите туда `.env` на основе `.env.example`. Заполните настоящие DB/OAuth/monitoring credentials и `APP_BASE_URL`; локальные defaults шаблона замените серверными значениями. Значения с символами shell заключайте в одинарные кавычки: файл читают и Compose, и Bash. Ограничьте доступ к файлу.

Для HTTPS используйте `APP_SECURITY_SECURE_COOKIE=true`. Если нужен первый администратор, временно включите bootstrap с отдельным паролем, затем отключите. Метрики включаются через `APP_SECURITY_PROMETHEUS_ENABLED=true`.

В GitHub environment `production` задайте:

| Secret | Назначение |
| --- | --- |
| `DEPLOY_HOST` | DNS/IP сервера |
| `DEPLOY_USER` | SSH-пользователь |
| `DEPLOY_PATH` | Абсолютный путь без пробелов и символов shell |
| `DEPLOY_SSH_KEY` | Приватный SSH-ключ |
| `GHCR_TOKEN` | Токен с `read:packages` |
| `SSL_PRIVATE_KEY` | TLS-ключ, соответствующий `haproxy/fullchain.pem` релиза |

Build публикует пакет через `GITHUB_TOKEN` с `packages:write`. Серверное приложение использует `postgres:5432`; порты приложения и БД опубликованы только на `127.0.0.1` для доступа с хоста. Внешний HTTPS обслуживает HAProxy на порту 443. Bootstrap и docs по умолчанию отключены.

Текущий workflow обслуживает один сервер. Для нескольких независимых серверов потребуются отдельные environments и переиспользуемый Deploy/matrix с тем же SHA. Реплики одного приложения требуют общего решения для БД, сессий и балансировки.

## Дальнейшие улучшения

- DevTools и автоматическое обновление исходников; сейчас изменения кода и ресурсов требуют повторения одной команды.
- Согласовать версии PostgreSQL для тестов и runtime; восстановить Maven Wrapper; использовать `npm ci` в Maven.
- Для production закрепить digest образов, настроить backup/restore, проверку внешнего HTTPS и автоматическое обновление TLS.
- Заменить `ssh-keyscan` доверенным host key; добавить правило auth для monitoring_users в HAProxy и настройки Grafana для `/grafana/`.

Справка: [Docker multi-stage builds](https://docs.docker.com/build/building/multi-stage/), [Compose optional env files](https://docs.docker.com/compose/how-tos/environment-variables/set-environment-variables/), [Compose profiles](https://docs.docker.com/compose/how-tos/profiles/).
