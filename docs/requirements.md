## 1. Visão Geral do Sistema

O **BrakIt** é uma plataforma web para criação, gerenciamento e visualização de torneios de forma simples e configurável. O sistema suporta gerenciamento de equipes, geração de chaveamentos, controle em tempo real de partidas por árbitros e modos de exibição otimizados para transmissões ou telões.

## 2. Requisitos Funcionais (RF)

### RF01 — Gestão de Contas e Autenticação

- **RF01.1:** O sistema deve permitir que novos usuários se cadastrem criando uma conta.
- **RF01.2:** O sistema deve permitir o login de usuários cadastrados.

### RF02 — Gestão de Torneios

- **RF02.1:** O usuário autenticado deve conseguir criar novos torneios.
- **RF02.2:** O criador/dono deve conseguir editar as informações de torneios existentes.
- **RF02.3:** O criador/dono deve conseguir deletar torneios.
- **RF02.4:** O sistema deve permitir definir as seguintes propriedades do torneio:
    - Nome e Descrição.
    - Visibilidade (_Público_ ou _Privado_).
    - Quantidade máxima de times participantes.

### RF03 — Gestão de Times e Integrantes

- **RF03.1:** O(s) dono(s) do torneio deve(m) conseguir cadastrar times no torneio.
- **RF03.2:** O(s) dono(s) do torneio deve(m) conseguir excluir times do torneio.
- **RF03.3:** O(s) dono(s) do torneio deve(m) conseguir cadastrar integrantes nos times, informando **Nome** e **Função/Cargo** no time.
- **RF03.4:** O(s) dono(s) do torneio deve(m) conseguir remover integrantes de um time.

### RF04 — Chaveamento e Classificação

- **RF04.1:** O sistema deve ser capaz de gerar a chave (bracket) do torneio automaticamente com base nos times cadastrados.
- **RF04.2:** O(s) dono(s) do torneio deve(m) conseguir disparar a geração da chave do torneio.
- **RF04.3:** O sistema deve calcular e exibir o ranking por pontuação dos times do torneio.

### RF05 — Controle de Partidas e Arbitragem

- **RF05.1:** O árbitro/dono deve conseguir alterar a pontuação dos times em tempo real durante um round/partida em andamento.
- **RF05.2:** O árbitro/dono deve ter controles manuais para tomar decisões arbitrárias em um round.
- **RF05.3:** O árbitro/dono deve conseguir declarar um time como vencedor de um round manualmente, independentemente da pontuação ou estado da partida.
- **RF05.4:** O árbitro/dono deve conseguir eliminar um time de um round/torneio de forma sumária.
    

### RF06 — Modos de Exibição (Telão / Stream View)

- **RF06.1:** O sistema deve fornecer um modo de visualização em telão (_Preview_) para a **Chave do Torneio**.
- **RF06.2:** O sistema deve fornecer um modo de visualização em telão (_Preview_) para o **Ranking dos Times**.
- **RF06.3:** O sistema deve possuir um modo de exibição em telão dedicado aos **Confrontos do Round Atual**, destacando os times em disputa.
- **RF06.4:** No modo de exibição do round atual, o sistema deve apresentar a lista/agenda dos **Próximos Rounds/Partidas**.

## 3. Regras de Negócio (RN)

- **RN01 — Limite de Torneios Ativos:** Cada usuário possui uma cota limite de torneios simultaneamente ativos no sistema.
- **RN02 — Capacidade do Torneio:** O sistema não pode permitir o cadastro de mais times do que a `quantidade máxima de times` configurada no torneio.
- **RN03 — Capacidade do Time:** O sistema não pode permitir o cadastro de mais integrantes em um time do que a `quantidade máxima de pessoas por time`, caso definida nas configurações do torneio.
- **RN04 — Trava de Alteração da Chave:** Após a geração inicial e o início das partidas do chaveamento, edições estruturais nos times não devem corromper os confrontos em andamento.
- **RN05 — Soberania da Arbitragem:** Ações manuais do árbitro (vitória forçada ou desqualificação) se sobrepõem à contagem automática de pontos do sistema.

## 4. Requisitos Não-Funcionais (RNF)

- **RNF01 — Usabilidade / Interface Simples:** A interface principal do sistema deve priorizar a simplicidade de uso para permitir criação e gestão rápida durante eventos ao vivo.
- **RNF02 — Design Responsivo para Telões:** As visões de _Preview/Telão_ devem ser otimizadas para exibição responsiva, com alto contraste e boa legibilidade à distância.
- **RNF03 — Atualização em Tempo Real (Recomendado):** As telas de exibição (telão/ranking/chave) devem refletir alterações de placar e status de partidas instantaneamente sem necessidade de recarregar a página (_ex: via WebSockets ou Server-Sent Events_).