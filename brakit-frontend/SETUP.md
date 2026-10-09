# BrakIt - Instrções de Execução

## 🚀 Iniciando o Projeto (Frontend + Backend)

### 1️⃣ Executar Backend (Quarkus)

No diretório `/brakit-api`:

```bash
# Modo desenvolvimento
./mvnw quarkus:dev

# Ou on Windows
mvnw.cmd quarkus:dev
```

Backend estará disponível em: `http://localhost:8080`
OpenAPI UI: `http://localhost:8080/q/swagger-ui/`

### 2️⃣ Executar Frontend (React)

Em novo terminal, no diretório `/brakit-frontend`:

```bash
# Instalar dependências (primeira vez)
npm install

# Iniciar modo desenvolvimento
npm run dev
```

Frontend estará disponível em: `http://localhost:5173`

## 🔗 Integração

O frontend está configurado para chamar a API em `http://localhost:8080/api`.

Para modificar a URL da API, edite `.env`:

```env
VITE_API_URL=http://localhost:8080/api
```

## 📦 Build para Produção

### Frontend:

```bash
cd brakit-frontend
npm run build
```

Arquivos gerados em: `dist/`

### Backend:

```bash
cd brakit-api
./mvnw clean package -DskipTests
```

JAR gerado em: `target/`

## 🐳 Docker

### Build e Run com Docker Compose (em desenvolvimento)

Certifique-se de ter um `docker-compose.yml` na raiz do projeto:

```bash
docker-compose up --build
```

### Build individual:

Frontend:

```bash
cd brakit-frontend
docker build -t brakit-frontend .
docker run -p 3000:3000 brakit-frontend
```

Backend:

```bash
cd brakit-api
docker build -f src/main/docker/Dockerfile.jvm -t brakit-api .
docker run -p 8080:8080 brakit-api
```

## 🗂️ Estrutura do Projeto

```
brakit/
├── brakit-api/              # Backend Quarkus
│   ├── src/main/
│   │   ├── java/           # Código Java
│   │   ├── resources/      # application.properties, etc
│   │   └── docker/         # Dockerfiles
│   ├── pom.xml
│   └── mvnw
│
├── brakit-frontend/         # Frontend React
│   ├── src/
│   │   ├── components/     # Componentes React
│   │   ├── pages/         # Páginas
│   │   ├── contexts/      # Context API
│   │   ├── hooks/         # Custom Hooks
│   │   ├── lib/           # Utilitários e API
│   │   └── index.css      # Styles
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   └── Dockerfile
│
└── docs/
    └── requirements.md     # Requisitos do sistema
```

## 📝 Fluxo Básico de Desenvolvimento

1. Backend roda em `http://localhost:8080`
2. Frontend roda em `http://localhost:5173`
3. Frontend faz requisições para `http://localhost:8080/api`

## 🔧 Configurações Adicionais

### CORS na API

Certifique-se que o Quarkus está configurado com CORS habilitado em `application.properties`:

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173
```

### Ports Alternativas

Se as portas padrão estiverem ocupadas:

**Frontend:**

```bash
npm run dev -- --port 5174
```

**Backend:**

```bash
./mvnw quarkus:dev -Dquarkus.http.port=8081
```

## 🚦 Troubleshooting

| Problema                    | Solução                                            |
| --------------------------- | -------------------------------------------------- |
| CORS error                  | Verificar configuração de CORS no backend          |
| 404 em rota da API          | Verificar URL base em `src/lib/api.ts`             |
| Porta já em uso             | Mudar porta ou encerrar processo anterior          |
| Dependências desatualizadas | Rodar `npm install` ou `./mvnw dependency:resolve` |

## 📚 Próximos Passos

- [ ] Implementar endpoints da API
- [ ] Conectar autenticação real
- [ ] Implementar atualização em tempo real (WebSockets)
- [ ] Criar testes
- [ ] Deploy inicial
