## Локальная разработка

### Требования

- Java 25 (проверить: `java -version`)
- Docker + Docker Compose (для Postgres, Redis, MinIO)
- Maven не нужен отдельно — в корне лежит `./mvnw` (`mvnw.cmd` для Windows)

### Первый запуск

1. Скопируй `.env.example` как `.env`, выставь свои переменные:

```bash
cp .env.example .env
```

2. Подними инфраструктуру (Postgres, Redis, MinIO) — **до** запуска приложения:

```bash
docker compose up --remove-orphans -d
docker compose ps
```

3. Собери проект:

```bash
# MacOS/Linux (первый раз сделай скрипт исполняемым)
chmod +x ./mvnw
./mvnw clean compile

# Windows
./mvnw.cmd clean compile
```

4. Запусти приложение:

```bash
./mvnw spring-boot:run
# Windows: ./mvnw.cmd spring-boot:run
```

### Куда зайти

| Что                  | Адрес                 | Примечание                          |
|----------------------|-----------------------|-------------------------------------|
| Приложение           | http://localhost:8080 | порт из `APP_PORT` в `.env`         |
| MinIO-консоль        | http://localhost:9001 | логин/пароль из `MINIO_ROOT_*`      |
| Postgres             | localhost:5432        | данные из `POSTGRES_*`              |
| Redis                | localhost:6379        | пароль из `REDIS_PASSWORD`          |

Если MinIO-консоль открылась — инфраструктура и приложение живы.

### Тесты

Тесты полностью изолированы: ходят в H2 in-memory (`src/test/resources/application.properties`),
внешние Postgres/Redis/MinIO и `.env` им не нужны. Запускаются без поднятого Docker:

```bash
./mvnw clean test
# Windows: ./mvnw.cmd clean test
```

Полная сборка с тестами:

```bash
./mvnw clean install
```

### Ручки API

Все ручки, кроме login/refresh, требуют заголовок `Authorization: Bearer <accessToken>`
(иначе `401`). Пользователь для login должен уже существовать в БД — отдельной
ручки регистрации пока нет.

| Метод | Путь                        | Тело / ответ                              |
|-------|-----------------------------|-------------------------------------------|
| POST  | `/api/v1/auth/login`        | `{"email","password"}` → пара токенов     |
| POST  | `/api/v1/auth/refresh`      | `{"refreshToken"}` → новая пара токенов   |
| POST  | `/api/v1/auth/logout`       | `{"refreshToken"}` → `204`                |
| POST  | `/api/v1/files`             | multipart-поле `file` → `201 {"id"}`      |
| GET   | `/api/v1/files/{uuid}`      | байты файла с content-type                |

### Остановка

```bash
# остановить приложение: Ctrl+C в терминале spring-boot:run
# остановить инфраструктуру:
docker compose down
# остановить и удалить данные (БД, MinIO-файлы, Redis):
docker compose down -v
```

### Частые проблемы

- **Тесты падают с `Connection refused` к Postgres/Redis** — так быть не должно: тесты ходят в H2.
  Проверь, что существует `src/test/resources/application.properties`.
- **Flyway: `Migration checksum mismatch for migration version 1`** — локальный файл
  `src/main/resources/db/migration/V1__.sql` менялся после применения к dev-БД.
  Чинится repair'ом истории (данные сохраняются):
```bash
PGPASSWORD=<POSTGRES_PASSWORD> psql -h localhost -U <POSTGRES_USER> -d <POSTGRES_DB> \
  -c "UPDATE flyway_schema_history SET checksum = 557005216 WHERE version = '1';"
```
  Checksum `557005216` — текущий для `V1__.sql`; если ошибка назовёт другое число
  в `Resolved locally`, подставь его. Либо снеси dev-данные: `docker compose down -v`.
- **`401` на ручках `/api/v1/files`** — нужен заголовок `Authorization: Bearer <accessToken>`.
  Исключение — только `/api/v1/auth/login` и `/api/v1/auth/refresh`, они открыты.
- **После `git pull` странные ошибки компиляции про Lombok** — выполнить `./mvnw clean`
  (в `target/` могли остаться классы, собранные IDE без Lombok).
