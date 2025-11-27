# Meet Backend API

Backend приложение для мобильного приложения Meet - платформы для организации профессиональных митапов.

## 🎯 Текущий статус

**✅ Созданные компоненты:**
- Domain Layer (Entity классы)
  - BaseEntity, User, Tag, Meeting, Community
- Repository Layer (все репозитории с кастомными запросами)
- Configuration
  - JwtTokenProvider
  - application.yml
- Database
  - Flyway миграция V1
  - Начальные данные (теги)
- Docker Compose

**⏳ TODO (Следующие шаги):**
- Security Configuration
- Auth Module (Service + Controller)
- User Module (Service + Controller)
- Meeting Module (Service + Controller)
- Community Module (Service + Controller)

## 🚀 Быстрый старт

### 1. Запуск базы данных

```bash
docker-compose up -d
```

Это запустит:
- PostgreSQL на порту 5432
- Redis на порту 6379

### 2. Проверка БД

```bash
# Подключитесь к PostgreSQL
docker exec -it meet-postgres psql -U postgres -d meet_db

# Проверьте таблицы
\dt

# Проверьте начальные данные
SELECT * FROM tags;

# Выход
\q
```

### 3. Запуск приложения

#### Через IntelliJ IDEA:
1. Откройте `MeetBackendApplication.kt`
2. Нажмите Run (зелёная стрелка)
3. Приложение запустится на http://localhost:8080

#### Через Gradle:
```bash
./gradlew bootRun
```

#### С профилем dev:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. Проверка работы

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Health Check:** http://localhost:8080/actuator/health

## 📁 Структура проекта

```
src/main/kotlin/dev/whysoezzy/meet/
├── MeetBackendApplication.kt          # Главный класс
├── domain/
│   ├── entity/                        # ✅ JPA Entity классы
│   │   ├── BaseEntity.kt
│   │   ├── User.kt
│   │   ├── Tag.kt
│   │   ├── Meeting.kt
│   │   └── Community.kt
│   └── repository/                    # ✅ Spring Data JPA Repositories
│       ├── UserRepository.kt
│       ├── TagRepository.kt
│       ├── MeetingRepository.kt
│       └── CommunityRepository.kt
├── api/
│   ├── dto/                           # ✅ Data Transfer Objects
│   │   └── common/
│   │       └── CommonDto.kt
│   └── controller/                    # ⏳ TODO: REST Controllers
├── service/                           # ⏳ TODO: Business Logic
├── config/                            # ✅ Configuration
│   └── JwtTokenProvider.kt
└── security/                          # ⏳ TODO: Security Config
```

## 🔧 Технологии

| Компонент | Технология | Версия |
|-----------|-----------|--------|
| Language | Kotlin | 1.9.25 |
| Framework | Spring Boot | 3.5.7 |
| Database | PostgreSQL | 14+ |
| Cache | Redis | 7+ |
| Build Tool | Gradle | 8.5 |
| Java | OpenJDK | 17 |
| Migrations | Flyway | - |
| JWT | JJWT | 0.12.3 |
| API Docs | SpringDoc | 2.3.0 |

## 🗄️ База данных

### Схема:
- **users** - пользователи
- **tags** - теги интересов
- **user_interests** - интересы пользователей
- **user_social_media** - соцсети пользователей
- **communities** - сообщества
- **community_tags** - теги сообществ
- **community_subscribers** - подписчики
- **meetings** - встречи/митапы
- **meeting_tags** - теги встреч
- **meeting_participants** - участники встреч
- **sms_codes** - SMS коды для верификации

### Начальные данные:
12 тегов: Android, Kotlin, Compose, Backend, iOS, UI/UX, DevOps, Data Science, Flutter, React Native, JavaScript, Python

## ⚙️ Конфигурация

### Environment Variables (опционально):
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/meet_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT
JWT_SECRET=your-production-secret-key-minimum-256-bits

# SMS Provider
SMS_PROVIDER=console  # console | twilio

# S3
S3_ENABLED=false
S3_BUCKET_NAME=meet-storage
```

### Профили:
- **default** - базовая конфигурация
- **dev** - для разработки (verbose logging)
- **prod** - для production (TODO)

## 🧪 Тестирование

```bash
# Запуск всех тестов
./gradlew test

# Запуск с coverage
./gradlew test jacocoTestReport
```

## 📝 Следующие шаги разработки

### Phase 1: Security & Auth (3-4 дня)
1. SecurityConfig - настройка Spring Security
2. JwtAuthenticationFilter - фильтр для JWT
3. AuthService - бизнес-логика аутентификации
4. SmsService - отправка SMS кодов
5. AuthController - REST API
6. GlobalExceptionHandler - обработка ошибок

### Phase 2: Users Module (2 дня)
1. UserService - работа с профилями
2. UserController - REST API
3. FileStorageService - загрузка аватаров

### Phase 3: Meetings Module (3 дня)
1. MeetingService - работа с встречами
2. MeetingController - REST API
3. Главный экран (hero + popular events)

### Phase 4: Communities Module (2 дня)
1. CommunityService
2. CommunityController

### Phase 5: Finalization (2 дня)
1. Search & Tags
2. Caching (Redis)
3. Testing
4. Documentation

**Оценка времени до MVP: 2-3 недели**

## 🐛 Troubleshooting

### Проблемы с подключением к БД:
```bash
# Проверьте что PostgreSQL запущен
docker ps

# Проверьте логи
docker logs meet-postgres

# Перезапустите контейнеры
docker-compose restart
```

### Flyway ошибки:
```bash
# Очистите БД и пересоздайте
docker-compose down -v
docker-compose up -d

# Подождите 10 секунд
# Затем запустите приложение
```

### Port already in use:
```bash
# Проверьте что порты свободны
netstat -ano | findstr :8080
netstat -ano | findstr :5432
netstat -ano | findstr :6379
```

## 📚 Полезные ссылки

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Kotlin for Spring](https://kotlinlang.org/docs/spring-boot-restful.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

--- Добавить в проект serviceAccountKey.json!!!!

---

**Версия:** 0.0.1-SNAPSHOT  
**Создано:** 2025-11-10  
**Статус:** 🟡 In Development (40% ready)
