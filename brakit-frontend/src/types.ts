export type TournamentVisibility = 'PUBLIC' | 'PRIVATE'

// Usuário
export interface User {
  id: number
  email: string
  name: string
}

// Torneio
export interface Tournament {
  id: number
  name: string
  description: string
  visibility: TournamentVisibility
  teamCountLimit: number
  ownerId: number
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// Time (GET/PUT não retornam membros)
export interface Team {
  id: number
  tournamentId: number
  name: string
}

export interface TeamWithMembers extends Team {
  members: TeamMember[]
}

// Integrante
export interface TeamMember {
  id: number
  teamId: number
  name: string
  role: string | null
}

// Partida do chaveamento (mata-mata simples)
export type MatchStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'FINISHED'
export type MatchResultType = 'SCORE' | 'MANUAL' | 'DISQUALIFICATION' | 'BYE'

export interface Match {
  id: number
  tournamentId: number
  round: number // 1 = primeira rodada; a maior é a final
  position: number // posição dentro da rodada (0-based)
  team1Id: number | null
  team2Id: number | null
  team1Name: string | null
  team2Name: string | null
  team1Score: number
  team2Score: number
  status: MatchStatus
  winnerId: number | null
  resultType: MatchResultType | null
}

// Ranking
export type RankingStatus = 'ACTIVE' | 'ELIMINATED' | 'DISQUALIFIED' | 'CHAMPION'

export interface RankingRow {
  position: number
  teamId: number
  teamName: string
  played: number
  wins: number
  losses: number
  pointsFor: number
  pointsAgainst: number
  scoreDiff: number
  points: number
  status: RankingStatus
}

// Erro padrão da API
export interface ErrorResponse {
  status: number
  message: string
}

// Requisições
export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

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

export interface MemberRequest {
  name: string
  role?: string
}

export interface CreateTeamRequest {
  name: string
  members?: MemberRequest[]
}

export interface UpdateTeamRequest {
  name: string
}
