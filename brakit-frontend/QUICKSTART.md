# 🚀 BrakIt Frontend - Quick Start

Guia rápido para começar em 5 minutos!

## Pré-requisitos

- Node.js 16+
- npm ou yarn
- Backend Quarkus rodando em `http://localhost:8080`

## 1️⃣ Setup (2 min)

```bash
cd brakit-frontend
npm install
cp .env.example .env
```

## 2️⃣ Iniciar Desenvolvimento (1 min)

```bash
npm run dev
```

Abrir: `http://localhost:5173`

## 3️⃣ Primeiro Uso

1. Clique em "Cadastrar" (ou "Faça login" se tiver conta)
2. Preencha email, senha e nome
3. Acesse o dashboard
4. Clique em "Novo Torneio"
5. Configure e crie seu primeiro torneio!

## 📝 Arquivos Importantes

| Arquivo               | Descrição             |
| --------------------- | --------------------- |
| `src/App.tsx`         | Rotas e estrutura     |
| `src/pages/`          | Todas as páginas      |
| `src/components/ui/`  | Componentes padrão    |
| `src/lib/services.ts` | Serviços da API       |
| `.env`                | Variáveis de ambiente |
| `vite.config.ts`      | Configuração build    |

## 🔗 API Proxy

O frontend proxeia requisições para:

```
http://localhost:8080/api → /api (localmente)
```

Configure em `.env`:

```bash
VITE_API_URL=http://localhost:8080/api
```

## 🏗️ Build

```bash
npm run build
# Output: dist/
```

## 🐳 Docker

```bash
# Build
docker build -t brakit-frontend .

# Run
docker run -p 3000:3000 brakit-frontend
```

## 🎯 Estrutura Básica

```
src/
├── App.tsx              → Rotas
├── pages/               → Páginas (Login, Dashboard, etc)
├── components/ui/       → Componentes reutilizáveis
├── contexts/            → Auth
├── lib/services.ts      → API
└── types.ts             → TypeScript
```

## 📚 Documentação Completa

- Ver `README.md` para detalhes completos
- Ver `GUIDE.md` para guia extenso
- Ver `SETUP.md` para setup avançado

## ⚡ Comandos Importantes

```bash
npm run dev      # Desenvolvimento
npm run build    # Build produção
npm run preview  # Preview do build
npm run lint     # Verificar erros
```

## 🆘 Problemas?

**Porta já em uso:**

```bash
npm run dev -- --port 5174
```

**CORS error:**
Certifique-se que o backend tem CORS habilitado

**Dependências:**

```bash
npm install
npm audit fix
```

## ✅ Pronto!

Seu frontend está pronto para desenvolvimento. Comece a explorar as páginas e componentes!

---

**Happy Coding! 🎉**
