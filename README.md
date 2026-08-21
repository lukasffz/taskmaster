# TaskMaster

Monorepo de um sistema de gestão de projetos e tarefas.

## Componentes

- `backend-taskmaster`: Spring Boot 3.3, Java 21, PostgreSQL, Flyway e JWT em cookie HttpOnly.
- `frontend-taskmaster`: React, Vite, TypeScript, Tailwind, Axios, React Router e Cypress.
- Banco de produção: Neon PostgreSQL.
- Deploy planejado: Render para a API e Vercel para o frontend.

## Execução local

### Backend

Configure `backend-taskmaster/.env` a partir de `.env.example` e execute:

```powershell
Set-Location backend-taskmaster
$env:JAVA_HOME = "C:\Users\stigm\.jdks\temurin-21.0.11"
.\mvnw.cmd spring-boot:run
```

Health check: `http://localhost:8080/actuator/health`

Swagger: `http://localhost:8080/swagger-ui.html`

### Frontend

Configure `frontend-taskmaster/.env` a partir de `.env.example` e execute:

```powershell
Set-Location frontend-taskmaster
npm install
npm run dev
```

## Validação

```powershell
Set-Location backend-taskmaster
$env:JAVA_HOME = "C:\Users\stigm\.jdks\temurin-21.0.11"
.\mvnw.cmd test

Set-Location ..\frontend-taskmaster
npm run lint
npm run build
npm run cypress:run
```

Os testes Cypress precisam da API em `http://127.0.0.1:8080` e do Vite em `http://127.0.0.1:5173`.

## Deploy

### Render

O arquivo `render.yaml` cria o Web Service do backend com build Maven, health check e variáveis secretas marcadas como `sync: false`.

Variáveis obrigatórias no Render:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `FRONTEND_URL` com a URL pública do Vercel

### Vercel

Configure o projeto apontando para `frontend-taskmaster` como Root Directory. O `vercel.json` mantém o fallback das rotas SPA.

Variável obrigatória no Vercel:

- `VITE_API_URL` com a URL pública do Render

Depois do primeiro deploy, atualize `FRONTEND_URL` no Render com a URL definitiva do Vercel e valide login, logout, CRUD, cookies e CORS.

## Segurança

Secrets não são versionados. Nunca copie valores reais para `.env.example`, README, código ou commits. Cookies de autenticação são HttpOnly; o frontend não armazena JWT em storage do navegador.

## CI

O GitHub Actions em `.github/workflows/ci.yml` executa testes Maven, lint e build frontend em pushes e pull requests para `main`.
