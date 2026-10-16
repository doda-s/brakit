# Relatório de pendências do backend (brakit-api)

O front implementa o chaveamento, a arbitragem, o ranking e os telões (RF04, RF05, RF06) contra um mock
(`brakit-frontend/scripts/mock-server.mjs`). **O mock é o contrato de referência**: a lógica de
`generateBracket`, `finishMatch`, `resolveDisqualified` e `rankingFor` pode ser portada para Java.
Hoje o backend não tem nenhuma tabela, entidade ou endpoint de partidas.

Convenções já usadas pelo backend e que devem ser mantidas: prefixo `/api`, ids numéricos, erro
`{ "status": int, "message": string }`, torneio privado respondendo 404 a quem não é dono,
403 `NotTournamentOwner` em escrita por não dono, 401 sem corpo quando falta token.

## 1. Decisões de regra de negócio assumidas (confirmar com o time)

Os requisitos (`docs/requirements.md`) não definem formato nem pontuação. Foi assumido:

| Tema | Decisão implementada no front/mock |
|---|---|
| Formato | Mata-mata de eliminação simples |
| Nº de times que não é potência de 2 | Os times que sobram avançam direto na 1ª rodada (bye). Os byes são distribuídos pela ordem de seeds (nenhuma partida tem dois byes). O sorteio dos times é aleatório |
| Empate | Não existe. Ao encerrar pelo placar, empate é recusado (409); o dono muda o placar ou declara o vencedor |
| Pontuação do ranking | Vitória = 3 pontos; derrota = 0; bye não conta como vitória nem como jogo |
| Desempate do ranking | Campeão sempre em 1º; depois pontos, saldo de placar (pontos marcados - sofridos), pontos marcados, nome |
| Quem controla partidas | Só o dono do torneio. Não há papel de árbitro no backend |
| Travas (RN04) | Com a chave gerada: não pode criar nem excluir time (409). Renomear time e editar integrantes continuam permitidos |
| Desqualificação | Elimina o time do torneio; a partida pendente dele é vencida pelo adversário por W.O. Se o adversário ainda não está definido, o W.O. é aplicado automaticamente quando ele chegar |
| Reiniciar chave | O dono pode apagar a chave e todos os resultados a qualquer momento |
| Fim do torneio | Quando a partida da última rodada (final) é encerrada; o vencedor é o campeão |

Se alguma dessas decisões mudar, ajustar o mock e o front junto.

## 2. Modelo de dados (migração Flyway nova, ex.: V6)

Tabela `match`:

| Coluna | Tipo | Observação |
|---|---|---|
| id | bigint PK | |
| tournament_id | bigint FK tournament, ON DELETE CASCADE | |
| round | int | 1 = primeira rodada; a maior é a final |
| position | int | posição dentro da rodada, 0-based |
| team1_id, team2_id | bigint FK team, nullable | null = "a definir" ou bye. ON DELETE: ver item 6 |
| team1_score, team2_score | int default 0, check >= 0 | |
| status | varchar, check | `SCHEDULED`, `IN_PROGRESS`, `FINISHED` |
| winner_id | bigint FK team, nullable | |
| result_type | varchar, nullable, check | `SCORE`, `MANUAL`, `DISQUALIFICATION`, `BYE` |

Restrição: `unique (tournament_id, round, position)`.

Desqualificação: coluna `disqualified boolean not null default false` em `team` (ou tabela própria).
Não deve aparecer em `TeamResponse` (o front deriva do ranking).

Avanço do vencedor: a partida `(r, p)` alimenta `(r+1, p/2)`; vai para `team1` se `p` é par, `team2` se ímpar.

## 3. Endpoints novos

"Dono" = autenticado e dono do torneio (404 se o torneio é privado e o usuário não é dono; 403 se visível mas não é dono).
Leituras seguem a visibilidade do torneio (anônimo vê os públicos).

| Método | Path | Auth | Resposta | Erros |
|---|---|---|---|---|
| POST | `/tournaments/{id}/bracket` | dono | 201 `Match[]` | 409 menos de 2 times; 409 chave já gerada |
| DELETE | `/tournaments/{id}/bracket` | dono | 204 | 409 chave não gerada |
| GET | `/tournaments/{id}/matches` | opcional | 200 `Match[]` ordenado por round, position. Lista vazia = sem chave | |
| GET | `/tournaments/{id}/matches/{matchId}` | opcional | 200 `Match` | 404 |
| POST | `/tournaments/{id}/matches/{matchId}/start` | dono | 200 `Match` | 409 não está `SCHEDULED`; 409 times ainda indefinidos |
| PATCH | `/tournaments/{id}/matches/{matchId}/score` | dono | 200 `Match` | 400 placar negativo/nulo; 409 não está `IN_PROGRESS` |
| POST | `/tournaments/{id}/matches/{matchId}/finish` | dono | 200 `Match` | 409 não está `IN_PROGRESS`; 409 empate |
| POST | `/tournaments/{id}/matches/{matchId}/winner` | dono | 200 `Match` | 400 `teamId` nulo ou fora da partida; 409 já encerrada; 409 times indefinidos |
| POST | `/tournaments/{id}/teams/{teamId}/disqualify` | dono | 200 `Match[]` (chave atualizada) | 404 time; 409 sem chave; 409 já desqualificado; 409 time já fora do torneio |
| GET | `/tournaments/{id}/ranking` | opcional | 200 `RankingRow[]` | |

Bodies: `score` recebe `{ "team1Score": int>=0, "team2Score": int>=0 }` (valores absolutos, não incrementos);
`winner` recebe `{ "teamId": long }`.

Alterações em endpoints existentes:
- `POST /tournaments/{id}/teams` e `DELETE .../teams/{teamId}`: responder 409
  (`Bracket already generated; teams can no longer be added or removed.`) quando existe chave.
- `DELETE /tournaments/{id}`: apagar partidas junto (cascade).
- Todas as escritas que alteram a chave devem usar o lock pessimista do torneio
  (`getOwnedForUpdate`), como já é feito em `createTeam`.

## 4. Formatos de resposta

`Match`:
```json
{
  "id": 10, "tournamentId": 2, "round": 1, "position": 0,
  "team1Id": 5, "team2Id": null,
  "team1Name": "Alfa", "team2Name": null,
  "team1Score": 0, "team2Score": 0,
  "status": "FINISHED", "winnerId": 5, "resultType": "BYE"
}
```
`team1Name`/`team2Name` são nomes dos times (null quando o time é nulo). O front só usa esses campos para exibir.

`RankingRow` (um por time do torneio, mesmo antes de gerar a chave; ordenado):
```json
{
  "position": 1, "teamId": 5, "teamName": "Alfa",
  "played": 2, "wins": 2, "losses": 0,
  "pointsFor": 7, "pointsAgainst": 3, "scoreDiff": 4,
  "points": 6, "status": "CHAMPION"
}
```
`status`: `CHAMPION` (venceu a final), `DISQUALIFIED`, `ELIMINATED` (perdeu uma partida encerrada), `ACTIVE`.
Partidas com `BYE` não contam em `played`/`wins`. Placares de partidas `MANUAL` e `DISQUALIFICATION` entram como
estão registrados.

## 5. Regras de execução

- **Gerar chave:** embaralhar os times; `size` = menor potência de 2 >= nº de times; criar todas as
  partidas de todas as rodadas (`log2(size)` rodadas); preencher a 1ª rodada com a ordem de seeds
  (`[1]`, depois cada seed `s` vira `s, total+1-s`); seed > nº de times = bye; partidas com bye nascem
  `FINISHED` com `resultType=BYE` e o time avança para a próxima partida.
- **Encerrar (qualquer forma):** gravar `winner_id`, `result_type`, `status=FINISHED`, colocar o vencedor
  na próxima partida e então verificar W.O. pendente (abaixo).
- **W.O. pendente:** se numa partida não encerrada um time está desqualificado e o outro está definido e não
  desqualificado, encerrar com `DISQUALIFICATION` vencendo o outro. Repetir em cascata.
- **Soberania do árbitro (RN05):** `winner` e `disqualify` valem independentemente do placar.
- **Desqualificar:** o time precisa aparecer em alguma partida não encerrada; marcar `disqualified=true` e
  resolver o W.O. dessa partida.

## 6. Pontos de atenção

- Excluir time não pode corromper partidas: como a exclusão passa a ser bloqueada com chave gerada, as FKs de
  `match` para `team` podem ser `ON DELETE CASCADE` na hora de apagar o torneio, mas o `deleteTeam` deve checar a chave antes.
- Reiniciar a chave (`DELETE .../bracket`) deve limpar também `team.disqualified`.
- Atualizar o limite de times (`PUT /tournaments/{id}`) continua sem relação com a chave.

## 7. Pendências fora do mock

- **RNF03 tempo real:** o front usa polling (3 a 5 s). Para atender o requisito, expor SSE (ou WebSocket)
  por torneio, por exemplo `GET /tournaments/{id}/events`, emitindo um evento a cada mudança de partida/chave.
  O hook `usePolling` do front seria trocado por um listener de eventos.
- **RN01 cota de torneios ativos:** não implementado no backend e a cota não está definida. O front já exibe
  o `message` do erro; sugerido responder 409 em `POST /tournaments` ao exceder. Definir o valor da cota e o
  que significa "ativo" (hoje não existe estado de torneio; poderia ser "chave gerada e final não encerrada").
- **Papel de árbitro (RF05):** hoje só o dono controla partidas. Se árbitros forem usuários distintos, é
  preciso modelar (por exemplo tabela `tournament_referee`) e liberar os endpoints de partida a eles.
- **CORS:** o front usa o proxy do Vite (`/api`), então não é necessário em desenvolvimento. Para produção com
  domínios diferentes, configurar `quarkus.http.cors`.
- **Documentação:** registrar as decisões da seção 1 em `docs/requirements.md` (RF04/RF05 e novas RNs).
- **Testes:** cobrir geração com 2, 3, 5 e 8 times, byes, W.O. em cascata, empate, travas da seção 3 e visibilidade.
