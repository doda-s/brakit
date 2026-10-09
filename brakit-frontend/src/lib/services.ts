import api from '@/lib/api'
import {
  PageResponse,
  Tournament,
  Team,
  TeamMember,
  Match,
  Ranking,
  LoginRequest,
  RegisterRequest,
  TokenResponse,
  CreateTournamentRequest,
  UpdateTournamentRequest,
  CreateTeamRequest,
  AddTeamMemberRequest,
  UpdateMatchScoreRequest,
  SetMatchWinnerRequest,
} from '@/types'

// ============= Autenticação =============

export const authService = {
  login(data: LoginRequest) {
    return api.post<TokenResponse>('/auth/login', data)
  },
  register(data: RegisterRequest) {
    return api.post('/auth/register', data)
  },
  me() {
    return api.get('/auth/me')
  },
}

// ============= Torneios =============

export const tournamentService = {
  getAll(page = 0, size = 20) {
    return api.get<PageResponse<Tournament>>('/tournaments', {
      params: { page, size },
    })
  },
  getById(id: string) {
    return api.get<Tournament>(`/tournaments/${id}`)
  },
  create(data: CreateTournamentRequest) {
    return api.post<Tournament>('/tournaments', data)
  },
  update(id: string, data: UpdateTournamentRequest) {
    return api.put<Tournament>(`/tournaments/${id}`, data)
  },
  delete(id: string) {
    return api.delete(`/tournaments/${id}`)
  },
  generateBracket(id: string) {
    return api.post(`/tournaments/${id}/bracket`, {})
  },
  getBracket(id: string) {
    return api.get(`/tournaments/${id}/bracket`)
  },
}

// ============= Times =============

export const teamService = {
  getByTournament(tournamentId: string) {
    return api.get<Team[]>(`/tournaments/${tournamentId}/teams`)
  },
  getById(tournamentId: string, teamId: string) {
    return api.get<Team>(`/tournaments/${tournamentId}/teams/${teamId}`)
  },
  create(tournamentId: string, data: CreateTeamRequest) {
    return api.post<Team>(`/tournaments/${tournamentId}/teams`, data)
  },
  delete(tournamentId: string, teamId: string) {
    return api.delete(`/tournaments/${tournamentId}/teams/${teamId}`)
  },
}

// ============= Integrantes de Time =============

export const teamMemberService = {
  getByTeam(tournamentId: string, teamId: string) {
    return api.get<TeamMember[]>(
      `/tournaments/${tournamentId}/teams/${teamId}/members`,
    )
  },
  add(tournamentId: string, teamId: string, data: AddTeamMemberRequest) {
    return api.post<TeamMember>(
      `/tournaments/${tournamentId}/teams/${teamId}/members`,
      data,
    )
  },
  remove(tournamentId: string, teamId: string, memberId: string) {
    return api.delete(
      `/tournaments/${tournamentId}/teams/${teamId}/members/${memberId}`,
    )
  },
}

// ============= Partidas =============

export const matchService = {
  getByTournament(tournamentId: string) {
    return api.get<Match[]>(`/tournaments/${tournamentId}/matches`)
  },
  getById(tournamentId: string, matchId: string) {
    return api.get<Match>(`/tournaments/${tournamentId}/matches/${matchId}`)
  },
  updateScore(
    tournamentId: string,
    matchId: string,
    data: UpdateMatchScoreRequest,
  ) {
    return api.patch<Match>(
      `/tournaments/${tournamentId}/matches/${matchId}/score`,
      data,
    )
  },
  setWinner(
    tournamentId: string,
    matchId: string,
    data: SetMatchWinnerRequest,
  ) {
    return api.post(
      `/tournaments/${tournamentId}/matches/${matchId}/winner`,
      data,
    )
  },
  disqualifyTeam(tournamentId: string, matchId: string, teamId: string) {
    return api.post(
      `/tournaments/${tournamentId}/matches/${matchId}/disqualify`,
      { teamId },
    )
  },
}

// ============= Ranking =============

export const rankingService = {
  getByTournament(tournamentId: string) {
    return api.get<Ranking[]>(`/tournaments/${tournamentId}/ranking`)
  },
}
