# 🚀 Frontend BrakIt - Guia Completo

Frontend moderno e responsivo para o gerenciador de torneios **BrakIt**, construído com **React**, **TypeScript**, **Tailwind CSS** e componentes padrão **Shadcn/ui**.

## 📋 Índice

- [Características](#características)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Desenvolvimento](#desenvolvimento)
- [Funcionalidades](#funcionalidades)
- [Estrutura](#estrutura)
- [API Integration](#api-integration)
- [Autenticação](#autenticação)
- [Componentes](#componentes)
- [Temas](#temas)
- [Deploy](#deploy)
- [Troubleshooting](#troubleshooting)

## ✨ Características

- ✅ **Autenticação Completa** - Login/Signup com JWT
- ✅ **Gerenciamento de Torneios** - CRUD completo
- ✅ **Gerenciamento de Times** - Cadastro e gestão de membros
- ✅ **Chaveamento Automático** - Geração de brackets
- ✅ **Painel de Arbitragem** - Atualização de placares em tempo real
- ✅ **Visualizações em Telão** - Modos preview otimizados para projeção
- ✅ **Ranking Dinâmico** - Classificação automática de times
- ✅ **Responsivo** - Funciona em mobile, tablet e desktop
- ✅ **TypeScript** - Type-safe em todo o código
- ✅ **Tailwind CSS** - Styling moderno e customizável
- ✅ **Dark Mode** - Suporte a tema escuro

## 📋 Requisitos

- Node.js 16+
- npm ou yarn
- Backend Quarkus rodando em `http://localhost:8080`

## 🔧 Instalação

```bash
# Clonar/entrar no diretório
cd brakit-frontend

# Instalar dependências
npm install

# Copiar arquivo de ambiente
cp .env.example .env

# Iniciar desenvolvimento
npm run dev
```

## 🏃 Desenvolvimento

```bash
# Servidor de desenvolvimento (hot-reload)
npm run dev

# Build para produção
npm run build

# Preview do build produção
npm run preview

# Lint e verificação
npm run lint
```

A aplicação estará disponível em `http://localhost:5173`

## 🎯 Funcionalidades Implementadas

### ✅ Autenticação

- [x] Página de Login
- [x] Página de Signup
- [x] Autenticação com JWT
- [x] Logout
- [x] Rotas protegidas

### ✅ Dashboard

- [x] Listagem de torneios do usuário
- [x] Botão para criar novo torneio
- [x] Card com informações de cada torneio

### ✅ Torneios

- [x] Criar novo torneio
- [x] Visualizar detalhes do torneio
- [x] Editar configurações (estrutura pronta)
- [x] Deletar torneio (estrutura pronta)

### ✅ Times

- [x] Gerenciar times do torneio
- [x] Adicionar times
- [x] Remover times
- [x] Gerenciar membros do time
- [x] Adicionar jogadores
- [x] Remover jogadores

### ✅ Chaveamento

- [x] Gerar bracket automaticamente
- [x] Visualizar chaveamento
- [x] Preview em telão

### ✅ Ranking

- [x] Visualizar ranking de times
- [x] Preview em telão com UI otimizada

### ✅ Arbitragem

- [x] Painel de controle para árbitros
- [x] Atualizar placares em tempo real
- [x] Desqualificar times
- [x] Definir vencedor manualmente

## 📁 Estrutura do Projeto

```
src/
├── App.tsx                 # Componente raiz com rotas
├── main.tsx               # Entrada da aplicação
├── index.css              # Estilos globais Tailwind
│
├── components/
│   ├── ui/               # Componentes Shadcn/ui
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   ├── card.tsx
│   │   ├── label.tsx
│   │   ├── textarea.tsx
│   │   ├── select.tsx
│   │   ├── alert.tsx
│   │   ├── tabs.tsx
│   │   ├── progress.tsx
│   │   └── badge.tsx
│   │
│   └── custom/           # Componentes customizados
│       └── StatsCard.tsx
│
├── contexts/
│   └── AuthContext.tsx    # Contexto de autenticação
│
├── hooks/
│   └── useAuth.ts         # Hook para autenticação
│
├── lib/
│   ├── api.ts             # Cliente Axios configurado
│   ├── services.ts        # Serviços da API
│   └── utils.ts           # Funções utilitárias
│
├── pages/
│   ├── LoginPage.tsx
│   ├── SignupPage.tsx
│   ├── DashboardPage.tsx
│   ├── CreateTournamentPage.tsx
│   ├── TournamentDetailPage.tsx
│   ├── TeamDetailPage.tsx
│   ├── ArbiterPanelPage.tsx
│   ├── BracketPreviewPage.tsx
│   ├── RankingPreviewPage.tsx
│   └── ProtectedRoute.tsx
│
└── types.ts              # Types TypeScript
```

## 🔌 API Integration

Os serviços da API estão centralizados em `src/lib/services.ts`:

```typescript
import { tournamentService, teamService, matchService } from '@/lib/services'

// Listar torneios
const tournaments = await tournamentService.getAll()

// Criar torneio
const newTournament = await tournamentService.create({
  name: 'Meu Torneio',
  description: 'Descrição',
  isPublic: true,
  maxTeams: 8,
})

// Atualizar placar de partida
await matchService.updateScore(tournamentId, matchId, {
  team1Score: 10,
  team2Score: 8,
})
```

### Endpoints Esperados

```
POST   /api/auth/login
POST   /api/auth/signup
POST   /api/auth/logout

GET    /api/tournaments
POST   /api/tournaments
GET    /api/tournaments/:id
PUT    /api/tournaments/:id
DELETE /api/tournaments/:id

GET    /api/tournaments/:id/teams
POST   /api/tournaments/:id/teams
DELETE /api/tournaments/:id/teams/:teamId

GET    /api/tournaments/:id/teams/:teamId/members
POST   /api/tournaments/:id/teams/:teamId/members
DELETE /api/tournaments/:id/teams/:teamId/members/:memberId

GET    /api/tournaments/:id/matches
PATCH  /api/tournaments/:id/matches/:matchId/score
POST   /api/tournaments/:id/matches/:matchId/winner
POST   /api/tournaments/:id/matches/:matchId/disqualify

POST   /api/tournaments/:id/bracket
GET    /api/tournaments/:id/bracket

GET    /api/tournaments/:id/ranking
```

## 🔐 Autenticação

### Fluxo

1. **Signup**: Usuário cria conta → Token armazenado no `localStorage`
2. **Login**: Credenciais validadas → Token retornado
3. **Requisições**: Token incluído automaticamente em headers
4. **Token Expirado**: Interceptador redireciona para `/login`

### Armazenamento

```typescript
// Token
localStorage.getItem('auth_token')

// Dados do usuário
localStorage.getItem('auth_user')
```

### Uso no Componente

```typescript
import { useAuth } from '@/hooks/useAuth'

export const MyComponent = () => {
  const { user, login, logout, isAuthenticated } = useAuth()

  return (
    <>
      {user?.name}
      <button onClick={logout}>Sair</button>
    </>
  )
}
```

## 🎨 Componentes Shadcn/ui

### Componentes Disponíveis

- **Button** - Botão com múltiplas variantes
- **Input** - Campo de entrada de texto
- **Card** - Container para conteúdo
- **Label** - Label para formulários
- **Textarea** - Área de texto
- **Select** - Select HTML customizado
- **Alert** - Caixa de alerta
- **Tabs** - Abas navegáveis
- **Progress** - Barra de progresso
- **Badge** - Badge/etiqueta

### Exemplo de Uso

```typescript
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

export const MyForm = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Meu Formulário</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <Input placeholder="Nome" />
        <Button>Enviar</Button>
      </CardContent>
    </Card>
  )
}
```

## 🎨 Temas

### Cores Padrão

As cores são definidas em `src/index.css` com variáveis CSS:

```css
:root {
  --primary: 210 40% 96%;
  --secondary: 210 14% 83%;
  --destructive: 0 84.2% 60.2%;
  --muted: 0 0% 96.1%;
  /* ... */
}

.dark {
  --primary: 210 40% 10%;
  /* ... */
}
```

### Customizar Tema

1. Edite `src/index.css` para alterar variáveis
2. Atualize `tailwind.config.js` se necessário
3. Aplique classes Tailwind: `bg-primary`, `text-secondary`, etc

## 🚀 Deploy

### Build

```bash
npm run build
```

Gera pasta `dist/` com arquivos otimizados.

### Docker

```bash
docker build -t brakit-frontend .
docker run -p 3000:3000 brakit-frontend
```

### Vercel (Recomendado)

1. Push para GitHub
2. Connect no Vercel
3. Build: `npm run build`
4. Output: `dist`

### Netlify

1. Push para GitHub
2. Connect no Netlify
3. Build: `npm run build`
4. Publish: `dist`

### CORS

Certifique-se que a API permite requisições do frontend:

**Backend (application.properties):**

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173,https://seudominio.com
```

## 🐛 Troubleshooting

### "Cannot GET /"

- Certifique-se que o servidor estão rodando
- Verifique porta em `vite.config.ts`

### Erros de CORS

- Configure CORS no backend
- Verifique `VITE_API_URL` em `.env`

### Token expirado

- Token é removido automaticamente
- Usuário é redirecionado para login

### Dependências desatualizadas

```bash
npm update
npm audit fix
```

### Porta já em uso

```bash
# Mudar porta
npm run dev -- --port 5174
```

## 📚 Recursos

- [React Docs](https://react.dev)
- [TypeScript Docs](https://www.typescriptlang.org)
- [Tailwind CSS](https://tailwindcss.com)
- [Vite](https://vitejs.dev)
- [React Router](https://reactrouter.com)
- [Lucide Icons](https://lucide.dev)

## 📄 Licença

MIT

---

**Desenvolvido com ❤ para BrakIt**
