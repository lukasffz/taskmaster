# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some Oxlint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

# TaskMaster Frontend

Frontend do TaskMaster, criado com React, Vite e TypeScript.

## Desenvolvimento local

1. Copie `.env.example` para `.env` e ajuste `VITE_API_URL` se necessário.
2. Instale as dependências com `npm install`.
3. Inicie o frontend com `npm run dev`.

O cliente usa Axios com `withCredentials` para enviar o cookie HttpOnly da API. Falhas de rede durante a primeira chamada recebem até duas tentativas controladas para acomodar o cold start do Render. Respostas `401` não são mascaradas como cold start.

## Validação

```powershell
npm run build
npm run lint
```

## Estrutura

As pastas de componentes, features, hooks, páginas, rotas, contextos, tipos e utilitários estão preparadas para as Fases 7 e 8. A autenticação e as telas de negócio serão implementadas nas fases correspondentes.
See the [Oxlint rules documentation](https://oxc.rs/docs/guide/usage/linter/rules) for the full list of rules and categories.
