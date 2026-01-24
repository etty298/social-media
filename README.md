# Social Media Platform

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Масштабируемая платформа социальной сети, построенная на микросервисной архитектуре, демонстрирующая современные паттерны проектирования распределенных систем.

## Содержание

- [Обзор](#обзор)
- [Архитектура](#архитектура)
- [Сервисы](#сервисы)
  - [Authentication Service](#authentication-service)
  - [User Service](#user-service)
- [Быстрый старт](#быстрый-старт)
  - [Требования](#требования)
  - [Установка](#установка)
  - [Конфигурация](#конфигурация)
- [API документация](#api-документация)
- [Технологический стек](#технологический-стек)
- [Разработка](#разработка)
- [Развертывание](#развертывание)
- [Roadmap](#roadmap)
- [Лицензия](#лицензия)

## Обзор

Social Media Platform — это распределенное приложение социальной сети корпоративного уровня, демонстрирующее принципы микросервисной архитектуры. Платформа предоставляет основные функции социальных сетей, включая аутентификацию пользователей, управление профилями и социальные связи.

### Ключевые возможности

- **Безопасная аутентификация**: JWT-аутентификация с поддержкой refresh токенов
- **Управление пользователями**: Полноценное управление профилями и социальными связями
- **Масштабируемая архитектура**: Event-driven микросервисы с асинхронной коммуникацией
- **База данных на сервис**: Каждый микросервис имеет собственное хранилище данных
- **API-first дизайн**: RESTful API с OpenAPI документацией
- **Контейнеризация**: Поддержка Docker и Docker Compose

## Архитектура

Платформа следует принципам микросервисной архитектуры с domain-driven дизайном. Сервисы взаимодействуют через Apache Kafka для event-driven процессов и REST API для синхронных операций.

### Архитектурная диаграмма

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (в планах)                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
              ┌────────────┴────────────┐
              │                         │
    ┌─────────▼─────────┐    ┌─────────▼─────────┐
    │  Authentication   │    │   User Service    │
    │     Service       │    │                   │
    │                   │    │  • Профили        │
    │  • Регистрация    │    │  • Связи          │
    │  • Вход/Выход     │    │  • Подписки       │
    │  • JWT токены     │    │  • Поиск          │
    │  • Валидация      │    │                   │
    └─────────┬─────────┘    └─────────┬─────────┘
              │                        │
              │   ┌────────────────────▼──────────┐
              └──►│     Apache Kafka              │
                  │                               │
                  │  • user_registered            │
                  │                               │
                  └───────────────┬───────────────┘
                                  │
              ┌───────────────────┴───────────────┐
              │                                   │
    ┌─────────▼─────────┐            ┌──────────▼─────────┐
    │   PostgreSQL      │            │   PostgreSQL       │
    │   (Auth DB)       │            │   (User DB)        │
    └───────────────────┘            └────────────────────┘
```

### Принципы проектирования

- **Автономность сервисов**: Каждый сервис независимо развертывается и масштабируется
- **База данных на сервис**: Устраняет тесную связанность и обеспечивает независимое масштабирование
- **Event-driven коммуникация**: Асинхронный обмен сообщениями через Kafka для слабой связанности
- **Паттерн API Gateway**: Централизованная точка входа для клиентских запросов (в планах)
- **Circuit Breaker**: Паттерны устойчивости для межсервисного взаимодействия (в планах)

## Сервисы

### Authentication Service

Authentication Service обрабатывает все операции, связанные с безопасностью, включая регистрацию пользователей, аутентификацию и управление токенами.

**Порт**: `8081`  
**База данных**: PostgreSQL (authentication-service)

#### Endpoints

| Метод  | Endpoint                 | Описание                                     | Аутентификация |
|--------|--------------------------|----------------------------------------------|----------------|
| POST   | `/api/v1/auth/register`  | Регистрация нового пользователя              | Публичный      |
| POST   | `/api/v1/auth/login`     | Аутентификация пользователя и выдача токенов | Публичный      |
| POST   | `/api/v1/auth/logout`    | Инвалидация текущей сессии                   | Требуется      |
| POST   | `/api/v1/auth/refresh`   | Обновление access токена через refresh токен | Публичный      |
| GET    | `/api/v1/token/validate` | Валидация JWT токена (service-to-service)    | N/A            |
| DELETE | `/api/v1/auth/token`     | Отзыв конкретного токена                     | Требуется      |
| DELETE | `/api/v1/auth/tokens`    | Отзыв всех токенов пользователя              | Требуется      |

#### События (Kafka)

**Публикуемые события:**
- `user_registered`: Создается при успешной регистрации нового пользователя

#### Технологический стек

- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- JWT (jjwt 0.13.0)
- Lombok

---

### User Service

User Service управляет профилями пользователей, социальными связями (подписчики, подписки, друзья) и функциями поиска пользователей.

**Порт**: `8080`  
**База данных**: PostgreSQL (user-service)

#### Endpoints

##### Управление профилем

| Метод | Endpoint                     | Описание                             | Аутентификация |
|-------|------------------------------|--------------------------------------|----------------|
| GET   | `/api/v1/users`              | Поиск пользователей с фильтрами      | Требуется      |
| GET   | `/api/v1/users/{identifier}` | Получение профиля по ID или username | Требуется      |
| GET   | `/api/v1/users/me`           | Получение собственного профиля       | Требуется      |
| PATCH | `/api/v1/users/me`           | Обновление собственного профиля      | Требуется      |

##### Социальные связи

| Метод  | Endpoint                                             | Описание                                        | Аутентификация |
|--------|------------------------------------------------------|-------------------------------------------------|----------------|
| POST   | `/api/v1/users/{userId}/follow`                      | Подписаться на пользователя                     | Требуется      |
| DELETE | `/api/v1/users/{userId}/unfollow`                    | Отписаться от пользователя                      | Требуется      |
| GET    | `/api/v1/users/{userId}/followers`                   | Получить список подписчиков                     | Требуется      |
| GET    | `/api/v1/users/{userId}/followings`                  | Получить список подписок                        | Требуется      |
| GET    | `/api/v1/users/{userId}/friends`                     | Получить список друзей (взаимные подписки)      | Требуется      |
| GET    | `/api/v1/users/{userId}/relationship/{targetUserId}` | Проверить статус отношений между пользователями | Требуется      |

#### События (Kafka)

**Потребляемые события:**
- `user_registered`: Создает профиль пользователя в базе user-service

#### Технологический стек

- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- JWT (jjwt 0.13.0)
- Lombok

---

## Быстрый старт

### Требования

- **Java Development Kit (JDK)**: Версия 17 или выше
- **Apache Maven**: Версия 3.8+
- **Docker**: Версия 20.10+ (для контейнерного развертывания)
- **Docker Compose**: Версия 2.0+
- **Git**: Для клонирования репозитория

### Установка

#### Вариант 1: Docker Compose (рекомендуется)

1. Клонируйте репозиторий:
```bash
git clone https://github.com/etty298/social-media.git
cd social-media
```

2. Запустите все сервисы через Docker Compose:
```bash
docker-compose up -d
```

Это запустит:
- Authentication Service (порт 8081)
- User Service (порт 8080)
- PostgreSQL базы данных (порты 5432, 5433)
- Apache Kafka (порты 9092, 29092)

3. Проверьте, что сервисы запущены:
```bash
docker-compose ps
```

4. Просмотр логов:
```bash
docker-compose logs -f [имя-сервиса]
```

#### Вариант 2: Локальная разработка

1. Клонируйте репозиторий:
```bash
git clone https://github.com/etty298/social-media.git
cd social-media
```

2. Запустите инфраструктурные сервисы (Kafka и PostgreSQL):
```bash
docker-compose up -d authentication-db user-db kafka
```

3. Соберите проект:
```bash
mvn clean install
```

4. Запустите каждый сервис:

```bash
# Терминал 1: Authentication Service
cd authentication-service
mvn spring-boot:run

# Терминал 2: User Service
cd user-service
mvn spring-boot:run
```

### Конфигурация

Каждый сервис конфигурируется через файл `application.properties` в директории `src/main/resources/`.

#### Конфигурация Authentication Service

```properties
# Конфигурация сервера
spring.application.name=authentication-service
server.port=8081

# Конфигурация базы данных
spring.datasource.url=jdbc:postgresql://localhost:5432/authentication-service
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.globally_quoted_identifiers=true

# Конфигурация Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Конфигурация JWT
token.access.secret=your-secret-key-change-in-production
token.access.lifetime=600000
token.refresh.lifetime=2592000000
```

#### Конфигурация User Service

```properties
# Конфигурация сервера
spring.application.name=user-service
server.port=8080

# Конфигурация базы данных
spring.datasource.url=jdbc:postgresql://localhost:5432/user-service
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.globally_quoted_identifiers=true

# Конфигурация Kafka
spring.kafka.bootstrap-servers=localhost:9092

# JWT
token.access.secret=your-secret-key-change-in-production
```

## API документация

### OpenAPI/Swagger документация

Каждый сервис предоставляет интерактивную API документацию через Swagger UI:

- **Authentication Service**: http://localhost:8081/swagger-ui.html
- **User Service**: http://localhost:8080/swagger-ui.html

### Примеры API запросов

#### Регистрация нового пользователя

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "SecurePassword123!",
    "name": "John Doe"
  }'
```

**Ответ:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "name": "John Doe"
}
```

#### Вход в систему

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePassword123!"
  }'
```

**Ответ:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 600000
}
```

*Примечание: Refresh токен возвращается в HTTP-only cookie*

#### Обновление access токена

```bash
curl -X POST http://localhost:8081/api/v1/auth/refresh \
  --cookie "refreshToken=your_refresh_token_here"
```

#### Выход из системы

```bash
curl -X POST http://localhost:8081/api/v1/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Получение профиля пользователя

```bash
curl -X GET http://localhost:8080/api/v1/users/john_doe \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Ответ:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "name": "John Doe",
  "bio": "Software Engineer",
  "stats": {
    "followersCount": 150,
    "followingCount": 200,
    "friendsCount": 75
  }
}
```

#### Получение собственного профиля

```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Обновление собственного профиля

```bash
curl -X PATCH http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "bio": "Senior Software Engineer"
  }'
```

#### Подписка на пользователя

```bash
curl -X POST http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440001/follow \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Ответ:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "username": "jane_doe",
  "isFollowing": true,
  "isFriend": false
}
```

#### Отписка от пользователя

```bash
curl -X DELETE http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440001/unfollow \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Получение списка подписчиков

```bash
curl -X GET "http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/followers?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Ответ:**
```json
{
  "content": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440001",
      "username": "jane_doe",
      "name": "Jane Doe",
      "isFriend": true
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

#### Получение списка подписок

```bash
curl -X GET "http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/followings?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Получение списка друзей

```bash
curl -X GET "http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/friends?page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Проверка статуса отношений

```bash
curl -X GET "http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000/relationship/550e8400-e29b-41d4-a716-446655440001" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Ответ:**
```json
{
  "isFollowing": true,
  "isFollower": false,
  "isFriend": false
}
```

#### Поиск пользователей

```bash
curl -X GET "http://localhost:8080/api/v1/users?q=john&page=0&size=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Ответ:**
```json
{
  "content": [
    {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "username": "john_doe",
      "name": "John Doe"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

## Технологический стек

### Backend Framework
- **Spring Boot 3.5.6**: Основной фреймворк
- **Spring Security**: Аутентификация и авторизация
- **Spring Data JPA**: Слой персистентности данных
- **Spring Kafka**: Event streaming

### Базы данных
- **PostgreSQL 18.1**: Основное хранилище данных для всех сервисов

### Message Broker
- **Apache Kafka 8.1.1**: Платформа event streaming

### Безопасность
- **JWT (JSON Web Tokens)**: Stateless аутентификация
- **jjwt 0.13.0**: Библиотека реализации JWT

### API документация
- **SpringDoc OpenAPI 2.7.0**: Документация и тестирование API

### Инструменты разработки
- **Lombok**: Уменьшение boilerplate кода
- **Maven**: Сборка и управление зависимостями
- **Docker**: Контейнеризация
- **Docker Compose**: Оркестрация мульти-контейнерных приложений

### Тестирование
- **JUnit 5**: Фреймворк для unit-тестирования
- **Spring Boot Test**: Интеграционное тестирование

## Разработка

### Структура проекта

```
social-media/
├── authentication-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ru/home/authentication/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entities/
│   │   │   │       ├── exception/
│   │   │   │       ├── factory/
│   │   │   │       ├── kafka/
│   │   │   │       ├── repository/
│   │   │   │       ├── security/
│   │   │   │       └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── user-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── ru/home/user/
│   │   │   │       ├── config/
│   │   │   │       ├── controller/
│   │   │   │       ├── dto/
│   │   │   │       ├── entities/
│   │   │   │       ├── exceptions/
│   │   │   │       ├── kafka/
│   │   │   │       ├── mapper/
│   │   │   │       ├── repositories/
│   │   │   │       ├── security/
│   │   │   │       └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yaml
├── pom.xml
└── README.md
```

### Сборка из исходников

```bash
# Сборка всех сервисов
mvn clean package

# Сборка конкретного сервиса
cd authentication-service
mvn clean package

# Пропуск тестов
mvn clean package -DskipTests

# Только запуск тестов
mvn test
```

### Запуск тестов

```bash
# Запуск всех тестов
mvn test

# Запуск тестов для конкретного сервиса
cd user-service
mvn test
```

## Roadmap

### Версия 1.1.0 (Q1 2026)
- [ ] Реализация Token Blacklist
- [ ] Валидация входных данных на всех DTO
- [ ] Функционал сброса пароля
- [ ] Сервис верификации email
- [ ] Admin endpoints для управления пользователями

### Версия 1.2.0 (Q2 2026)
- [ ] **Post Service** с CRUD операциями для постов
- [ ] Feed Service с персонализированным контентом
- [ ] Comment Service с вложенными комментариями
- [ ] Система лайков/реакций
- [ ] Redis кэширование
- [ ] Real-time уведомления через WebSocket

### Версия 2.0.0 (Q3 2026)
- [ ] API Gateway с rate limiting
- [ ] Service mesh (Istio)
- [ ] Kubernetes deployment манифесты
- [ ] CI/CD pipeline
- [ ] Comprehensive мониторинг и логирование
- [ ] Оптимизация производительности

### Будущие улучшения
- [ ] Media Service для загрузки изображений/видео
- [ ] Search Service с Elasticsearch
- [ ] Analytics Service
- [ ] API для мобильных приложений
- [ ] Поддержка GraphQL API
- [ ] Мультиязычная поддержка

## Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

---

**Создано etty298**

[⬆ Наверх](#social-media-platform)