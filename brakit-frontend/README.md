# BrakIt Frontend

Frontend React + TypeScript + Tailwind CSS + Shadcn/ui para o gerenciador de torneios BrakIt.

## 🚀 Stack

- **React 18** - UI Library
- **TypeScript** - Type Safety
- **Vite** - Build Tool
- **Tailwind CSS** - Styling
- **Shadcn/ui** - Component Library
- **React Router** - Routing
- **Axios** - HTTP Client
- **React Hook Form** - Form Management
- **Lucide React** - Icons

## 📋 Pré-requisitos

- Node.js 16+ ou compatible
- npm ou yarn

## 🔧 Instalação

```bash
# Instalar dependências
npm install

# Copiar arquivo .env
copy .env.example .env

# Editar .env se necessário (ajustar URL da API)
```

## 🏃 Desenvolvimento

```bash
# Iniciar servidor de desenvolvimento
npm run dev

# Iniciar frontend com API mock local, sem backend Java/Docker
npm run dev:mock
```

A aplicação estará disponível em `http://localhost:5173`

O modo `dev:mock` sobe também uma API simulada em `http://localhost:8080` com login, cadastro e torneios básicos para testar o fluxo do front sem instalar Java ou Docker.

## 📦 Build

```bash
# Build para produção
npm run build

# Preview do build
npm run preview
```

## 📁 Estrutura do Projeto

```
src/
├── components/          # Componentes Shadcn/ui customizados
│   └── ui/             # Componentes base (Button, Input, Card, etc)
├── contexts/           # Contextos React (AuthContext)
├── hooks/             # Custom hooks (useAuth)
├── lib/               # Utilitários (api.ts, utils.ts)
├── pages/             # Páginas da aplicação
├── types.ts           # Tipos TypeScript compartilhados
├── App.tsx            # Aplicação principal com rotas
├── main.tsx           # Entrada da aplicação
└── index.css          # Estilos globais Tailwind
```

## 🔐 Autenticação

A autenticação é implementada via `AuthContext` com:

- Login/Signup
- Token JWT armazenado no localStorage
- Rotas protegidas com `ProtectedRoute`
- Interceptadores Axios para incluir token em requisições

## 🌐 API Integration

A integração com a API é feita via `lib/api.ts` usando Axios:

```typescript
import api from '@/lib/api'

// GET
const response = await api.get('/tournaments')

// POST
const response = await api.post('/tournaments', { name: 'Tournament' })

// PUT
const response = await api.put('/tournaments/1', { name: 'Updated' })

// DELETE
await api.delete('/tournaments/1')
```

## 🎯 Páginas Implementadas

### ✅ Autenticação

- [x] Login
- [x] Signup

### ✅ Dashboard

- [x] Listar torneios
- [x] Novo torneio (rota)

### ✅ Torneios

- [x] Criar torneio
- [x] Detalhes do torneio (com abas)
  - [x] Times
  - [x] Chaveamento
  - [x] Ranking
  - [x] Configurações

### 📝 Páginas TODO

- [ ] Editar torneio
- [ ] Deletar torneio
- [ ] Criar/editar time
- [ ] Gerenciar jogadores do time
- [ ] Painel de arbitragem (placar em tempo real)
- [ ] Visualizações em telão (Preview)
  - [ ] Chaveamento
  - [ ] Ranking
  - [ ] Partidas do round atual

## 🎨 Customização de Temas

Editar `src/index.css` e `tailwind.config.js` para customizar cores e temas.

As variáveis CSS estão em `src/index.css`:

- `--primary` / `--primary-foreground`
- `--secondary` / `--secondary-foreground`
- `--destructive` / `--destructive-foreground`
- E mais...

## 🚀 Deploy

### Docker

```bash
# Build da imagem
docker build -t brakit-frontend .

# Rodar container
docker run -p 3000:80 brakit-frontend
```

### Vercel / Netlify

Push para Git e configure em:

- [Vercel](https://vercel.com)
- [Netlify](https://netlify.com)

Build command: `npm run build`
Publish directory: `dist`

## 📝 Variáveis de Ambiente

Copiar `.env.example` para `.env.local`:

```
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=BrakIt
```

## 🐛 Troubleshooting

### Porta já em uso

```bash
# Mudar porta no vite.config.ts ou:
npm run dev -- --port 5174
```

### CORS errors

Certifique-se que a API está habilitada para CORS

### Token expirado

Token é automaticamente removido e usuário redirecionado para login

## 📚 Recursos

- [Documentação React](https://react.dev)
- [Documentação TypeScript](https://www.typescriptlang.org)
- [Documentação Tailwind CSS](https://tailwindcss.com)
- [Documentação Vite](https://vitejs.dev)
- [Shadcn/ui](https://ui.shadcn.com)

## 📄 Licença

MIT
