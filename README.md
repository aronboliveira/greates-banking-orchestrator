# The Greatest Banking Orchestrator

Portfolio full-stack banking orchestration demo: secure mock JWT login, RBAC, account creation, transaction registration, editable user profiles, BDD backend coverage, and a React dashboard.

<details open>
<summary><strong>English (en-US)</strong></summary>

## What This Is

The Greatest Banking Orchestrator is an agnostic portfolio project based on a small banking domain: accounts, operation types, transactions, and a secure dashboard.

The repository originally came from a hiring challenge. This version removes company-specific branding and presents the project as a standalone portfolio application.

## Fastest Boot: Docker Compose

Prerequisites:

- Docker
- Docker Compose plugin (`docker compose version`)

From the repository root:

```bash
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- API health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Stop:

```bash
docker compose down
```

Reset the local PostgreSQL volume:

```bash
docker compose down -v
```

Compose starts three services:

- `db`: PostgreSQL 17, database `greatest_banking_orchestrator`, user/password `gbo`/`gbo`.
- `api`: Spring Boot API on `localhost:8080`, waiting for PostgreSQL health.
- `web`: React/Vite static build served by Nginx on `localhost:5173`, waiting for API health.

If your local ports are occupied:

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

If you use a different web port, also pass `APP_CORS_ALLOWED_ORIGINS` with that origin.

## Mock Users

| Username | Password | Role |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` and `admin` can create accounts. Every authenticated user can create transactions and edit their own profile.

## Native Local Boot

Use this only when you do not want Docker. The backend uses an in-memory H2 database in the `local` profile.

Terminal 1:

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

Terminal 2:

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

On some external drives, `./mvnw` may fail with `Permission denied`; use `bash ./mvnw ...` as shown above.

## Tests

Backend, including Cucumber BDD and coverage gate:

```bash
bash ./mvnw clean verify
```

Frontend:

```bash
cd frontend
npm test
npm run build
npm run test:e2e
```

If Playwright browsers are missing:

```bash
cd frontend
node node_modules/playwright/cli.js install chromium
```

Docker Compose smoke check:

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

Conflict-safe smoke check when `5432`, `8080`, or `5173` are already in use:

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build -d
curl -f http://localhost:8081/actuator/health
curl -I http://localhost:5174
docker compose down
```

## Stack

- Backend: Java 21, Spring Boot, Spring MVC, JPA, PostgreSQL, Flyway, Spring Security, local HMAC JWT, Spring Mail, Resilience4j, OpenAPI.
- BDD: Cucumber for secure API scenarios.
- Frontend: React 19, TypeScript, Vite, TanStack Query, Redux Toolkit, Bootstrap, Bootstrap Icons, SCSS, DOMPurify, Zod, React Hot Toast.
- Tests: Maven/JUnit/Cucumber, Jest, Playwright.
- Deployment: Docker, Docker Compose, Render Blueprint.

## Render Deployment

`render.yaml` defines a Docker web service for the API, a Render PostgreSQL database, a static React site, generated JWT secret, SMTP placeholders, and optional AWS placeholders.

Set these secrets in Render when enabling real email:

- `SMTP_USERNAME`
- `SMTP_PASSWORD`

Real SMTP sends are intentionally limited to `aronprogamador@gmail.com` by default.

</details>

<details>
<summary><strong>Português (pt-BR)</strong></summary>

## O Que É

The Greatest Banking Orchestrator é um projeto full-stack de portfólio para contas, tipos de operação, transações e um dashboard seguro.

O repositório nasceu de um desafio técnico, mas esta versão remove a marca original e apresenta a aplicação como um produto agnóstico.

## Boot Principal: Docker Compose

Pré-requisitos: Docker e Docker Compose plugin.

Na raiz do repositório:

```bash
docker compose up --build
```

Acesse:

- Frontend: `http://localhost:5173`
- Saúde da API: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Parar:

```bash
docker compose down
```

Resetar o banco local:

```bash
docker compose down -v
```

O Compose sobe PostgreSQL, API Spring Boot e frontend React servido por Nginx. Se as portas estiverem ocupadas:

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

## Usuários Mock

| Usuário | Senha | Papel |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` e `admin` criam contas. Todos os usuários autenticados criam transações e editam o próprio perfil.

## Boot Nativo

Sem Docker, use H2 em memória:

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

Em outro terminal:

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

Em discos externos, `./mvnw` pode falhar com `Permission denied`; use `bash ./mvnw`.

## Testes

```bash
bash ./mvnw clean verify
cd frontend
npm test
npm run build
npm run test:e2e
```

Smoke test com Compose:

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

</details>

<details>
<summary><strong>Español (es-ES)</strong></summary>

## Qué Es

The Greatest Banking Orchestrator es una aplicación full-stack de portfolio para cuentas, tipos de operación, transacciones y un dashboard seguro.

El repositorio partió de un reto técnico, pero esta versión elimina la marca original y presenta el sistema como un producto agnóstico.

## Arranque Principal: Docker Compose

Requisitos: Docker y Docker Compose plugin.

Desde la raíz del repositorio:

```bash
docker compose up --build
```

Abrir:

- Frontend: `http://localhost:5173`
- Salud de la API: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Detener:

```bash
docker compose down
```

Reiniciar los datos locales:

```bash
docker compose down -v
```

Compose inicia PostgreSQL, API Spring Boot y frontend React servido por Nginx. Si los puertos están ocupados:

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

## Usuarios Mock

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` y `admin` pueden crear cuentas. Todos los usuarios autenticados pueden crear transacciones y editar su perfil.

## Arranque Nativo

Sin Docker, use H2 en memoria:

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

En otro terminal:

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

En discos externos, `./mvnw` puede fallar con `Permission denied`; use `bash ./mvnw`.

## Pruebas

```bash
bash ./mvnw clean verify
cd frontend
npm test
npm run build
npm run test:e2e
```

Smoke test con Compose:

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

</details>

<details>
<summary><strong>Deutsch (de-DE)</strong></summary>

## Überblick

The Greatest Banking Orchestrator ist ein Full-Stack-Portfolio-Projekt für Konten, Operationstypen, Transaktionen und ein geschütztes Dashboard.

Das Repository stammt ursprünglich aus einer technischen Challenge. Diese Version entfernt die alte Markenbindung und präsentiert die Anwendung als neutrales Portfolio-Projekt.

## Hauptstart: Docker Compose

Voraussetzungen: Docker und Docker Compose Plugin.

Im Repository-Root:

```bash
docker compose up --build
```

Öffnen:

- Frontend: `http://localhost:5173`
- API Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

Stoppen:

```bash
docker compose down
```

Lokale Daten zurücksetzen:

```bash
docker compose down -v
```

Compose startet PostgreSQL, die Spring-Boot-API und das React-Frontend über Nginx. Wenn Ports belegt sind:

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

## Mock-Benutzer

| Benutzer | Passwort | Rolle |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` und `admin` können Konten erstellen. Alle angemeldeten Benutzer können Transaktionen anlegen und ihr Profil bearbeiten.

## Nativer Start

Ohne Docker wird H2 im Speicher verwendet:

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

In einem zweiten Terminal:

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

Auf externen Laufwerken kann `./mvnw` mit `Permission denied` fehlschlagen; verwenden Sie `bash ./mvnw`.

## Tests

```bash
bash ./mvnw clean verify
cd frontend
npm test
npm run build
npm run test:e2e
```

Compose Smoke Test:

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

</details>

<details>
<summary><strong>Français (fr)</strong></summary>

## Présentation

The Greatest Banking Orchestrator est une application full-stack de portfolio pour les comptes, les types d'opération, les transactions et un tableau de bord sécurisé.

Le dépôt vient à l'origine d'un défi technique. Cette version retire l'ancienne marque et présente l'application comme un projet de portfolio autonome.

## Démarrage Principal : Docker Compose

Prérequis : Docker et Docker Compose plugin.

À la racine du dépôt :

```bash
docker compose up --build
```

Ouvrir :

- Frontend : `http://localhost:5173`
- Santé de l'API : `http://localhost:8080/actuator/health`
- Swagger UI : `http://localhost:8080/swagger-ui.html`

Arrêter :

```bash
docker compose down
```

Réinitialiser la base locale :

```bash
docker compose down -v
```

Compose lance PostgreSQL, l'API Spring Boot et le frontend React servi par Nginx. Si les ports sont occupés :

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

## Utilisateurs Mock

| Utilisateur | Mot de passe | Rôle |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` et `admin` peuvent créer des comptes. Tous les utilisateurs authentifiés peuvent créer des transactions et modifier leur profil.

## Démarrage Natif

Sans Docker, utilisez H2 en mémoire :

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

Dans un autre terminal :

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

Sur certains disques externes, `./mvnw` peut échouer avec `Permission denied`; utilisez `bash ./mvnw`.

## Tests

```bash
bash ./mvnw clean verify
cd frontend
npm test
npm run build
npm run test:e2e
```

Smoke test Compose :

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

</details>

<details>
<summary><strong>中文 (zh)</strong></summary>

## 项目说明

The Greatest Banking Orchestrator 是一个作品集全栈项目，覆盖账户、操作类型、交易和受保护的仪表盘。

该仓库最初来自技术挑战。本版本移除了原公司的品牌信息，将应用呈现为独立作品集项目。

## 推荐启动方式：Docker Compose

前置要求：Docker 和 Docker Compose plugin。

在仓库根目录运行：

```bash
docker compose up --build
```

打开：

- 前端：`http://localhost:5173`
- API 健康检查：`http://localhost:8080/actuator/health`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

停止：

```bash
docker compose down
```

重置本地 PostgreSQL 数据：

```bash
docker compose down -v
```

Compose 会启动 PostgreSQL、Spring Boot API，以及由 Nginx 提供服务的 React 前端。如果端口被占用：

```bash
DB_PORT=5433 API_PORT=8081 WEB_PORT=5174 VITE_API_BASE_URL=http://localhost:8081 docker compose up --build
```

## 模拟用户

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| `super-admin` | `orchestrate-all` | `SUPER_ADMIN` |
| `admin` | `approve-flow` | `ADMIN` |
| `user` | `submit-flow` | `USER` |

`super-admin` 和 `admin` 可以创建账户。所有已登录用户都可以创建交易并编辑自己的资料。

## 本地原生命令

不使用 Docker 时，可以使用内存 H2 数据库：

```bash
SPRING_PROFILES_ACTIVE=local bash ./mvnw spring-boot:run
```

另一个终端运行：

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 npm run dev -- --port 5173 --host 127.0.0.1 --strictPort
```

在某些外接硬盘上，`./mvnw` 可能因为 `Permission denied` 失败；请使用 `bash ./mvnw`。

## 测试

```bash
bash ./mvnw clean verify
cd frontend
npm test
npm run build
npm run test:e2e
```

Compose 冒烟测试：

```bash
docker compose up --build -d
curl -f http://localhost:8080/actuator/health
curl -I http://localhost:5173
docker compose down
```

</details>
