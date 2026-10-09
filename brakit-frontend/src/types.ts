export type TournamentVisibility = 'PUBLIC' | 'PRIVATE'

// Tipos de Usuário
export interface User {
  id: number
  email: string
  name: string
}

// Tipos de Torneio
export interface Tournament {
  id: number
  name: string
  description: string
  visibility: TournamentVisibility
  teamCountLimit: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// Tipos de Time
export interface Team {
  id: string
  tournamentId: string
  name: string
  members: TeamMember[]
  createdAt: string
}

export interface TeamMember {
  id: string
  teamId: string
  name: string
  position: string
  joinedAt: string
}

// Tipos de Partida/Round
export interface Match {
  id: string
  roundNumber: number
  team1Id: string
  team2Id: string
  team1Score: number
  team2Score: number
  winnerId?: string
  status: 'pending' | 'in_progress' | 'finished'
  startedAt?: string
  finishedAt?: string
}

// Tipos de Ranking
export interface Ranking {
  teamId: string
  teamName: string
  wins: number
  losses: number
  points: number
  position: number
}

// Tipos de Requisições
export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export type SignupRequest = RegisterRequest

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface CreateTournamentRequest {
  name: string
  description: string
  visibility: TournamentVisibility
  teamCountLimit: number
}

export interface UpdateTournamentRequest {
  name?: string
  description?: string
  visibility?: TournamentVisibility
  teamCountLimit?: number
}

export interface CreateTeamRequest {
  name: string
}

export interface AddTeamMemberRequest {
  name: string
  position: string
}

export interface UpdateMatchScoreRequest {
  team1Score: number
  team2Score: number
}

export interface SetMatchWinnerRequest {
  winnerId: string
}
