## 1. Visão Geral do Sistema

O **BrakIt** é uma plataforma web para criação, gerenciamento e visualização de torneios de forma simples e configurável. O sistema suporta gerenciamento de equipes, geração de chaveamentos em diferentes formatos de torneio, controle em tempo real das partidas pelos donos do torneio e modos de exibição otimizados para transmissões ou telões.

As regras das partidas são genéricas e ajustadas por configurações do torneio (**RF02.4**), de forma que o sistema atenda modalidades com regras distintas. Os valores padrão seguem as regras do Hockey de Robôs.

### 1.1 Glossário

- **Tournament (Torneio):** competição criada por um usuário, com times, formato e chave próprios.
- **Match (Partida):** confronto entre dois times dentro da chave de um Tournament.
- **Chave (Bracket):** conjunto de Matches de um Tournament, organizado em rodadas conforme o formato.
- **Bye (Avanço direto):** vaga vazia na chave; o time sem adversário avança para a rodada seguinte sem jogar.
- **W.O.:** vitória concedida ao adversário de um time desclassificado ou que desistiu da Match (por não comparecimento ou abandono).
- **Prorrogação (Morte súbita):** tempo extra de uma Match empatada, encerrado pelo primeiro ponto marcado.
- **Cronômetro:** contador de tempo de uma Match, controlado pelos donos.
- **Criador:** usuário que criou o Tournament.
- **Co-dono:** usuário adicionado pelo criador para gerenciar o Tournament junto com ele.
- **Donos:** o criador e os co-donos do Tournament.

## 2. Requisitos Funcionais (RF)

### RF01 — Gestão de Contas e Autenticação

- **RF01.1:** O sistema deve permitir que novos usuários se cadastrem criando uma conta.
- **RF01.2:** O sistema deve permitir o login de usuários cadastrados.

### RF02 — Gestão de Torneios

- **RF02.1:** O usuário autenticado deve conseguir criar novos torneios, tornando-se o criador do torneio.
- **RF02.2:** Os donos devem conseguir editar as informações de torneios existentes, respeitando a **RN05**.
- **RF02.3:** Apenas o criador deve conseguir deletar o torneio.
- **RF02.4:** O sistema deve permitir definir as seguintes propriedades do torneio:
    - Nome e Descrição.
    - Visibilidade (_Público_ ou _Privado_).
    - Quantidade máxima de times participantes (mínimo 2).
    - Formato do torneio. Inicialmente apenas _Mata-mata_ (eliminação simples) é suportado.
    - Pontos por vitória (inteiro maior que zero; padrão 3).
    - Placar de W.O. (padrão 3 × 0), aplicado conforme a **RN11**.
    - Modo de desempate: _Automático_ (prorrogação com morte súbita) ou _Manual_ (padrão _Automático_), conforme a **RN10**.
    - Duração da Match em minutos (padrão 5), usada como referência pelo cronômetro (**RN12**).
- **RF02.5:** O criador deve conseguir adicionar co-donos ao torneio, informando o e-mail de um usuário cadastrado, e remover co-donos.
- **RF02.6:** Os co-donos têm as mesmas permissões do criador sobre o torneio, exceto deletar o torneio e gerenciar co-donos.

### RF03 — Gestão de Times e Integrantes

- **RF03.1:** Os donos do torneio devem conseguir cadastrar times no torneio.
- **RF03.2:** Os donos do torneio devem conseguir excluir times do torneio.
- **RF03.3:** Os donos do torneio devem conseguir cadastrar integrantes nos times, informando **Nome** e, opcionalmente, **Função/Cargo** no time. A função é um texto livre, sem valores pré-definidos, e nomes de integrantes podem se repetir.
- **RF03.4:** Os donos do torneio devem conseguir remover integrantes de um time.
- **RF03.5:** Os donos do torneio devem conseguir editar o nome de um time.
- **RF03.6:** Os donos do torneio devem conseguir editar o **Nome** e a **Função/Cargo** de um integrante.
- **RF03.7:** O nome de um time deve ser único dentro do torneio.
- **RF03.8:** Ao cadastrar um time, os donos do torneio podem informar opcionalmente seus integrantes iniciais (até 20 por requisição); o time e os integrantes são criados juntos ou nenhum deles é criado.

### RF04 — Chaveamento e Classificação

- **RF04.1:** O sistema deve ser capaz de gerar a chave do torneio automaticamente, com base nos times cadastrados e no formato do torneio (**RN04**).
- **RF04.2:** Os donos do torneio devem conseguir disparar a geração da chave. A geração exige pelo menos 2 times cadastrados.
- **RF04.3:** Os donos do torneio devem conseguir reiniciar a chave a qualquer momento. Reiniciar apaga todas as Matches e seus resultados, desfaz as desclassificações e devolve o Tournament ao estado `SCHEDULED`.
- **RF04.4:** O sistema deve calcular e exibir o ranking por pontuação dos times do torneio (**RN09**).

### RF05 — Controle de Matches (Arbitragem)

Os controles de arbitragem são exercidos pelos donos do torneio; não existe um papel de árbitro separado.

- **RF05.1:** Os donos devem conseguir iniciar uma Match cujos dois times já estejam definidos. Várias Matches podem estar em andamento ao mesmo tempo.
- **RF05.2:** Em uma Match em andamento, os donos devem conseguir acrescentar pontos, reduzir pontos ou definir um valor específico para o placar de cada time.
- **RF05.3:** Os donos devem conseguir encerrar uma Match em andamento pelo placar; vence o time com maior pontuação. Em caso de empate, aplica-se a **RN10**.
- **RF05.4:** Os donos devem conseguir declarar manualmente o vencedor de uma Match agendada (com os dois times definidos) ou em andamento, independentemente do placar (ex.: robôs incapacitados ou sorteio). O placar registrado segue a **RN11**.
- **RF05.5:** Os donos devem conseguir desclassificar um time, ou registrar a sua desistência, em uma Match agendada (com os dois times definidos) ou em andamento. O adversário vence por W.O., o placar registrado segue a **RN11** e o time desclassificado é eliminado do torneio.
- **RF05.6:** Os donos devem conseguir corrigir o resultado (vencedor e placar) de uma Match encerrada, conforme a **RN08**.
- **RF05.7:** Os donos devem conseguir pausar e retomar o cronômetro de uma Match em andamento (**RN12**).

### RF06 — Modos de Exibição (Telão / Stream View)

- **RF06.1:** O sistema deve fornecer um modo de visualização em telão (_Preview_) para a **Chave do Torneio**.
- **RF06.2:** O sistema deve fornecer um modo de visualização em telão (_Preview_) para o **Ranking dos Times**.
- **RF06.3:** O sistema deve possuir um modo de exibição em telão dedicado aos **Confrontos do Round Atual**, destacando os times em disputa.
- **RF06.4:** No modo de exibição do round atual, o sistema deve apresentar a lista/agenda dos **Próximos Rounds/Partidas**.

## 3. Regras de Negócio (RN)

- **RN01 — Capacidade do Torneio:** O sistema não pode permitir o cadastro de mais times do que a `quantidade máxima de times` configurada no torneio, que deve ser no mínimo 2. Não há limite de integrantes por time.
- **RN02 — Visibilidade do Torneio:** Torneios _Privados_ são visíveis apenas para os donos; torneios _Públicos_ são visíveis para qualquer pessoa, autenticada ou não.
- **RN03 — Estados de Tournament e Match:** Tournaments e Matches possuem os estados:
    - `SCHEDULED`: ainda não foi iniciado.
    - `IN_PROGRESS`: está em andamento.
    - `FINISHED`: foi finalizado.

    Uma Match nasce `SCHEDULED`, passa a `IN_PROGRESS` ao ser iniciada e a `FINISHED` ao ser encerrada (pelo placar, pela morte súbita na prorrogação, por vitória manual ou por W.O.). Uma Match agendada pode ir direto para `FINISHED` por vitória manual ou desclassificação, e uma Match com bye já nasce `FINISHED`.

    Um Tournament permanece `SCHEDULED` até a primeira Match ser iniciada, quando passa a `IN_PROGRESS`, e passa a `FINISHED` quando a Match final é encerrada. Reiniciar a chave devolve o Tournament a `SCHEDULED`.
- **RN04 — Formato Mata-mata:** No formato de eliminação simples:
    - Os times são sorteados aleatoriamente na chave.
    - O tamanho da chave é a menor potência de 2 maior ou igual ao número de times; as vagas que sobram são byes.
    - Os byes são distribuídos pela ordem de seeds, de forma que nenhuma Match tenha duas vagas vazias.
    - O time de uma Match com bye avança diretamente para a rodada seguinte.
    - O vencedor de cada Match avança para a Match seguinte da chave; o perdedor é eliminado.
    - Não existe empate: uma Match empatada é resolvida conforme a **RN10**.
    - O vencedor da Match final é o campeão do torneio.
- **RN05 — Trava da Chave:** Após a geração da chave:
    - A estrutura da chave não pode ser alterada, exceto reiniciando-a (**RF04.3**).
    - Não é possível adicionar nem remover times.
    - Não é possível alterar a quantidade máxima de times, o formato nem as configurações que afetam resultados (pontos por vitória, placar de W.O. e modo de desempate).
    - Nome, descrição e visibilidade continuam editáveis.
    - A duração da Match continua editável e vale apenas para as Matches que ainda não foram iniciadas.

    Edições em um time (nome e integrantes) só são permitidas enquanto o time não estiver em uma Match em andamento.
- **RN06 — Placar:** O placar de cada time é um inteiro maior ou igual a zero; uma redução que o deixaria negativo deve ser recusada. O placar só pode ser alterado em Matches em andamento, ou pela correção de resultado (**RN08**).
- **RN07 — Soberania dos Donos:** A vitória manual e a desclassificação se sobrepõem ao placar. Uma Match tem exatamente um vencedor, e no máximo um dos seus times pode ser desclassificado.
- **RN08 — Correção de Resultado:** O resultado de uma Match encerrada pode ser corrigido enquanto a Match seguinte da chave ainda não tiver sido iniciada. Ao corrigir:
    - O vencedor anterior é retirado da Match seguinte e o novo vencedor ocupa a mesma vaga.
    - A situação dos times (eliminado, desclassificado) passa a refletir o novo resultado.
    - A Match final pode ser corrigida; o campeão é atualizado e o Tournament permanece `FINISHED`.
    - Matches com bye não podem ser corrigidas.
    - O placar corrigido deve ser coerente com o vencedor: o vencedor precisa ter pontuação maior que a do adversário.
- **RN09 — Pontuação e Ranking:**
    - Cada vitória, seja pelo placar, manual ou por W.O., vale os `pontos por vitória` do torneio. Derrotas valem 0.
    - O bye não conta como partida jogada nem como vitória.
    - O W.O. conta como partida jogada para os dois times: vitória para um e derrota para o outro.
    - O placar registrado de cada Match, inclusive os definidos pela **RN11**, entra no cálculo de pontos marcados, sofridos e saldo.
    - O ranking inclui todos os times do torneio, mesmo antes da geração da chave.
    - Ordem do ranking: o campeão fica em 1º; os demais são ordenados por pontos, saldo de placar (pontos marcados menos sofridos), pontos marcados e nome.
    - Cada time tem uma situação no ranking: _Campeão_, _Desclassificado_, _Eliminado_ ou _Ativo_. Um time que perde por W.O., seja por desclassificação ou desistência, aparece como _Desclassificado_.
- **RN10 — Desempate:** Não existe empate em uma Match. Ao encerrar pelo placar uma Match empatada:
    - No modo _Automático_, a Match entra em prorrogação com morte súbita e continua `IN_PROGRESS`. A prorrogação não tem limite de tempo. A primeira alteração de placar que desfaça o empate encerra a Match automaticamente, com vitória do time que ficou à frente.
    - No modo _Manual_, o encerramento é recusado. Os donos ajustam o placar ou declaram o vencedor (**RF05.4**).
- **RN11 — Placar em Decisões dos Donos:** Quando a Match é decidida por vitória manual ou por W.O., o placar registrado deve sempre refletir o vencedor:
    - Se o vencedor já está à frente no placar, o placar é mantido.
    - Na vitória manual com placar empatado, o vencedor recebe 1 ponto a mais para desempatar (ex.: sorteio após empate; sorteio quando nenhum time comparece resulta em 1 × 0).
    - Na vitória manual com o vencedor atrás no placar, e no W.O. com o vencedor empatado ou atrás, o placar passa a ser o `placar de W.O.` configurado no torneio, a favor do vencedor (ex.: não comparecimento resulta em 3 × 0).
- **RN12 — Cronômetro:** Cada Match possui um cronômetro progressivo (conta o tempo decorrido, sem limite):
    - O cronômetro começa ao iniciar a Match e pode ser pausado e retomado pelos donos a qualquer momento enquanto a Match estiver em andamento.
    - A `duração da Match` é apenas uma referência: ao atingi-la, o sistema indica que o tempo regulamentar se esgotou, mas o cronômetro continua contando e a Match não é encerrada automaticamente. O encerramento é sempre feito pelos donos.
    - Durante a prorrogação, o cronômetro continua contando a partir do tempo já decorrido.
    - O cronômetro para quando a Match é encerrada, registrando o tempo total.
    - O estado do cronômetro é mantido pelo servidor, de forma que todas as telas exibam o mesmo tempo.

## 4. Requisitos Não-Funcionais (RNF)

- **RNF01 — Usabilidade / Interface Simples:** A interface principal do sistema deve priorizar a simplicidade de uso para permitir criação e gestão rápida durante eventos ao vivo.
- **RNF02 — Design Responsivo para Telões:** As visões de _Preview/Telão_ devem ser otimizadas para exibição responsiva, com alto contraste e boa legibilidade à distância.
- **RNF03 — Atualização em Tempo Real:** As telas de exibição (telão/ranking/chave) devem refletir alterações de placar, status de partidas e estado do cronômetro instantaneamente, sem necessidade de recarregar a página. A atualização é feita via _Server-Sent Events_ (SSE), com um fluxo de eventos por torneio.
