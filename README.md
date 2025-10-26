# 🌐 Social Media — Прототип социальной сети на микросервисной архитектуре

## 📖 Описание

**Social Media** — это прототип современной социальной сети, построенной на принципах **микросервисной архитектуры**.  
Проект демонстрирует взаимодействие между сервисами через **Kafka**, безопасную аутентификацию через **JWT**, а также масштабируемую структуру для будущего развития.

В текущей версии реализованы:
- 🔐 **Authentication Service** — регистрация, вход, валидация токенов.
- 👤 **User Service** — профили пользователей, подписки и взаимодействия между ними.

В будущем планируется добавить сервисы:
- 📝 **Post Service** — CRUD-постов пользователей.
- 💬 **Comment Service** — CRUD-комментариев и лайки комментариев.  
  Асинхронное уведомление Post Service или Notification Service о новых комментариях.
- 📰 **Feed Service** — генерация персонализированной ленты.  
  Интеграция с Post и User Service; кэширование в Redis.
- 🔔 **Notification Service** — асинхронные уведомления о событиях.

---

## 🧩 Архитектура

Проект состоит из независимых сервисов, взаимодействующих через **Apache Kafka (KRaft mode)**.  
Каждый сервис имеет собственную базу данных и отвечает за строго определённую бизнес-область.

```text
┌────────────────────┐        ┌────────────────────┐
│ Authentication     │        │ User Service       │
│  • JWT Auth        │◄──────►│  • Профили         │
│  • Регистрация     │        │  • Подписки        │
│  • Валидация токен │        │  • Поиск и фильтр  │
└────────┬───────────┘        └─────────┬──────────┘
         │ Kafka Event: user_registered │
         │ Kafka Event: username_changed│
         ▼                              ▼
  (другие сервисы в будущем)
```

---

## 🔐 Authentication Service

Отвечает за регистрацию, аутентификацию и валидацию JWT токенов.
При регистрации отправляет событие `user_registered` в Kafka для синхронизации с другими сервисами.

**Базовый URL:** `http://localhost:8081`

### Эндпоинты

| Метод    | Путь                        | Описание                                                                  |
|----------|-----------------------------|---------------------------------------------------------------------------|
| **POST** | `/auth/signup`              | Регистрация нового пользователя. Проверяет уникальность email и username. |
| **POST** | `/auth/signin`              | Вход пользователя, возвращает JWT токен.                                  |
| **POST** | `/auth/validate-token`      | Проверка валидности токена (используется другими сервисами).              |
| **POST** | `/settings/change-password` | Смена пароля текущего пользователя.                                       |

### Kafka события

* `user_registered` — создаётся после успешной регистрации.

---

## 👤 User Service

Отвечает за хранение и обработку данных пользователей, их связей и подписок.
Выполняет валидацию токена, обращаясь к **Authentication Service** через REST.

**Базовый URL:** `http://localhost:8080/api/users`

### Эндпоинты

| Метод     | Путь                                    | Описание                                                                         |
|-----------|-----------------------------------------|----------------------------------------------------------------------------------|
| **GET**   | `/api/users`                            | Получение списка пользователей с фильтрацией по prefix_username или prefix_name. |
| **GET**   | `/api/users/{username}`                 | Получение профиля пользователя.                                                  |
| **PATCH** | `/api/users/{username}?action=...`      | Обновление профиля (например, изменение username, name и др.).                   |
| **POST**  | `/api/users/{username}?action=follow`   | Подписка на пользователя.                                                        |
| **POST**  | `/api/users/{username}?action=unfollow` | Отписка от пользователя.                                                         |
| **GET**   | `/api/users/{username}/friends`         | Получение списка друзей пользователя.                                            |
| **GET**   | `/api/users/{username}/followers`       | Список подписчиков.                                                              |
| **GET**   | `/api/users/{username}/followings`      | Список подписок.                                                                 |

### Kafka события

* `user_changed_username`
* `user_deleted_profile`

---

## 🔗 Взаимодействие сервисов

### Через Kafka

**Authentication → User Service:**
событие `user_registered` создаёт профиль пользователя в user-service.

**User Service → Authentication:**
событие `user_changed_username` синхронизирует изменение username.

### Через HTTP

**User Service → Authentication Service:**
при каждом запросе с токеном фильтр `TokenValidationFilter` вызывает `/auth/validate-token`.

---

## ⚙️ Запуск проекта

1. Убедитесь, что **Kafka** и **PostgreSQL** запущены локально.
2. В `application.yml` каждого сервиса укажите свои настройки БД и Kafka.
3. Запустите сервисы в порядке:

    * Kafka (KRaft)
    * authentication-service
    * user-service

---

## 📡 Примеры HTTP-запросов

### 🔸 Регистрация пользователя

```bash
curl -X POST http://localhost:8081/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Ответ (успешно):**

```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "name": "John Doe"
}
```

### 🔸 Авторизация пользователя

```bash
curl -X POST http://localhost:8081/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

**Ответ (JWT токен):**

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 🔸 Получение профиля пользователя

```bash
curl -X GET http://localhost:8082/api/users/john_doe \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 🔸 Подписка на пользователя

```bash
curl -X POST "http://localhost:8082/api/users/jane_doe?action=follow" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 🔸 Получение списка подписчиков

```bash
curl -X GET "http://localhost:8082/api/users/john_doe/followers" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 🧩 Используемые технологии

* **Spring Boot 3** (Web, Security, JPA)
* **PostgreSQL** — основная база данных
* **Kafka** — обмен событиями между сервисами
* **Docker** (в планах)
* **JWT** — аутентификация и авторизация
* **Lombok**, **MapStruct**, **FeignClient** — удобство и чистота кода

---

## 🔮 Планы на будущее

* ✅ Реализовать **Refresh Token** для обновления JWT.
* 📝 Добавить **Post Service** с CRUD постов.
* 💬 Добавить **Comment Service** и лайки комментариев.
* 📰 Разработать **Feed Service** с кэшированием ленты.
* 🔔 Добавить **Notification Service** с асинхронной отправкой уведомлений.
* 🐳 Создать **Dockerfile** и **docker-compose.yml** для автоматического запуска стека.
