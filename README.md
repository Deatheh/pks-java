## Локальная разработка

### Первый запуск

1. Скопируй .env.example как .env, выстави свои переменные

2.1 Для MacOS/Linux:

```bash
chmod +x ./mvnw
./mvnw clean compile # или ./mvnw clean install, если нужно прогнать тесты
./mvnw spring-boot:run
docker compose up --remove-orphans -d
```

2.2 Для windows:

```cmd
./mvnw.cmd clean compile # или ./mvnw clean install, если нужно прогнать тесты
./mvnw.cmd spring-boot:run
docker compose up --remove-orphans -d
```

1. Зайди на http://localhost:9001 . Если все успешно, то ты увидишь окно регистрации.
