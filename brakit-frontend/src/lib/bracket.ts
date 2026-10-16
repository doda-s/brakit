import { Match, MatchResultType } from '@/types'

export function totalRounds(matches: Match[]): number {
  return matches.reduce((max, m) => Math.max(max, m.round), 0)
}

export function roundName(round: number, total: number): string {
  const fromEnd = total - round
  if (fromEnd === 0) return 'Final'
  if (fromEnd === 1) return 'Semifinal'
  if (fromEnd === 2) return 'Quartas de final'
  if (fromEnd === 3) return 'Oitavas de final'
  return `Rodada ${round}`
}

export function groupByRound(matches: Match[]): Match[][] {
  const total = totalRounds(matches)
  return Array.from({ length: total }, (_, i) =>
    matches
      .filter((m) => m.round === i + 1)
      .sort((a, b) => a.position - b.position),
  )
}

// Rodada em disputa: a primeira com partida em andamento; senão a primeira com partida pendente
export function currentRound(matches: Match[]): number | null {
  const live = matches.filter((m) => m.status === 'IN_PROGRESS')
  if (live.length) return Math.min(...live.map((m) => m.round))
  const pending = matches.filter(
    (m) => m.status === 'SCHEDULED' && m.team1Id && m.team2Id,
  )
  if (pending.length) return Math.min(...pending.map((m) => m.round))
  return null
}

export function getChampion(
  matches: Match[],
): { id: number; name: string } | null {
  const total = totalRounds(matches)
  const final = matches.find((m) => m.round === total)
  if (!final || final.status !== 'FINISHED' || final.winnerId == null) {
    return null
  }
  const name = final.winnerId === final.team1Id ? final.team1Name : final.team2Name
  return { id: final.winnerId, name: name ?? '' }
}

export const isReady = (m: Match) => m.team1Id != null && m.team2Id != null

export const statusLabel: Record<Match['status'], string> = {
  SCHEDULED: 'Agendada',
  IN_PROGRESS: 'Em andamento',
  FINISHED: 'Encerrada',
}

export const resultLabel: Record<MatchResultType, string> = {
  SCORE: 'Placar',
  MANUAL: 'Decisão do árbitro',
  DISQUALIFICATION: 'W.O. (desqualificação)',
  BYE: 'Avanço direto',
}
