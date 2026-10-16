import api from '@/lib/api'
import {
  PageResponse,
  Tournament,
  Team,
  TeamWithMembers,
  TeamMember,
  User,
  LoginRequest,
  RegisterRequest,
  TokenResponse,
  CreateTournamentRequest,
  UpdateTournamentRequest,
  CreateTeamRequest,
  UpdateTeamRequest,
  MemberRequest,
  Match,
  RankingRow,
} from '@/types'

type Id = string | number

// ============= Autenticação =============

export const authService = {
  login(data: LoginRequest) {
    return api.post<TokenResponse>('/auth/login', data)
  },
  register(data: RegisterRequest) {
    return api.post<User>('/auth/register', data)
  },
  me() {
    return api.get<User>('/auth/me')
  },
}

// ============= Torneios =============

export const tournamentService = {
  getAll(page = 0, size = 20) {
    return api.get<PageResponse<Tournament>>('/tournaments', {
      params: { page, size },
    })
  },
  getById(id: Id) {
    return api.get<Tournament>(`/tournaments/${id}`)
  },
  create(data: CreateTournamentRequest) {
    return api.post<Tournament>('/tournaments', data)
  },
  update(id: Id, data: UpdateTournamentRequest) {
    return api.put<Tournament>(`/tournaments/${id}`, data)
  },
  delete(id: Id) {
    return api.delete(`/tournaments/${id}`)
  },
}

// ============= Times =============

export const teamService = {
  getByTournament(tournamentId: Id) {
    return api.get<Team[]>(`/tournaments/${tournamentId}/teams`)
  },
  getById(tournamentId: Id, teamId: Id) {
    return api.get<Team>(`/tournaments/${tournamentId}/teams/${teamId}`)
  },
  create(tournamentId: Id, data: CreateTeamRequest) {
    return api.post<TeamWithMembers>(`/tournaments/${tournamentId}/teams`, data)
  },
  update(tournamentId: Id, teamId: Id, data: UpdateTeamRequest) {
    return api.put<Team>(`/tournaments/${tournamentId}/teams/${teamId}`, data)
  },
  delete(tournamentId: Id, teamId: Id) {
    return api.delete(`/tournaments/${tournamentId}/teams/${teamId}`)
  },
}

// ============= Integrantes de Time =============

export const teamMemberService = {
  getByTeam(tournamentId: Id, teamId: Id) {
    return api.get<TeamMember[]>(
      `/tournaments/${tournamentId}/teams/${teamId}/members`,
    )
  },
  getById(tournamentId: Id, teamId: Id, memberId: Id) {
    return api.get<TeamMember>(
      `/tournaments/${tournamentId}/teams/${teamId}/members/${memberId}`,
    )
  },
  add(tournamentId: Id, teamId: Id, data: MemberRequest) {
    return api.post<TeamMember>(
      `/tournaments/${tournamentId}/teams/${teamId}/members`,
      data,
    )
  },
  update(tournamentId: Id, teamId: Id, memberId: Id, data: MemberRequest) {
    return api.put<TeamMember>(
      `/tournaments/${tournamentId}/teams/${teamId}/members/${memberId}`,
      data,
    )
  },
  remove(tournamentId: Id, teamId: Id, memberId: Id) {
    return api.delete(
      `/tournaments/${tournamentId}/teams/${teamId}/members/${memberId}`,
    )
  },
}

// ============= Chaveamento =============

export const bracketService = {
  generate(tournamentId: Id) {
    return api.post<Match[]>(`/tournaments/${tournamentId}/bracket`)
  },
  reset(tournamentId: Id) {
    return api.delete(`/tournaments/${tournamentId}/bracket`)
  },
}

// ============= Partidas =============

export const matchService = {
  getByTournament(tournamentId: Id) {
    return api.get<Match[]>(`/tournaments/${tournamentId}/matches`)
  },
  getById(tournamentId: Id, matchId: Id) {
    return api.get<Match>(`/tournaments/${tournamentId}/matches/${matchId}`)
  },
  start(tournamentId: Id, matchId: Id) {
    return api.post<Match>(`/tournaments/${tournamentId}/matches/${matchId}/start`)
  },
  updateScore(
    tournamentId: Id,
    matchId: Id,
    data: { team1Score: number; team2Score: number },
  ) {
    return api.patch<Match>(
      `/tournaments/${tournamentId}/matches/${matchId}/score`,
      data,
    )
  },
  // Encerra pelo placar (empate é recusado)
  finish(tournamentId: Id, matchId: Id) {
    return api.post<Match>(`/tournaments/${tournamentId}/matches/${matchId}/finish`)
  },
  // Vitória manual: prevalece sobre o placar (RN05)
  setWinner(tournamentId: Id, matchId: Id, teamId: number) {
    return api.post<Match>(
      `/tournaments/${tournamentId}/matches/${matchId}/winner`,
      { teamId },
    )
  },
}

// Desqualificação sumária de um time (RF05.4); devolve a chave atualizada
export const disqualifyService = {
  disqualify(tournamentId: Id, teamId: Id) {
    return api.post<Match[]>(
      `/tournaments/${tournamentId}/teams/${teamId}/disqualify`,
    )
  },
}

// ============= Ranking =============

export const rankingService = {
  getByTournament(tournamentId: Id) {
    return api.get<RankingRow[]>(`/tournaments/${tournamentId}/ranking`)
  },
}
