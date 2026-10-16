import React from 'react'
import { Match } from '@/types'
import { groupByRound, roundName, statusLabel } from '@/lib/bracket'
import { cn } from '@/lib/utils'

interface Props {
  matches: Match[]
  // Modo telão: fundo escuro, alto contraste e fontes grandes (RNF02)
  tv?: boolean
}

interface SideProps {
  name: string | null
  score: number
  winner: boolean
  loser: boolean
  showScore: boolean
  tv: boolean
}

const Side: React.FC<SideProps> = ({ name, score, winner, loser, showScore, tv }) => (
  <div
    className={cn(
      'flex items-center justify-between gap-3 px-3 py-2',
      tv ? 'text-xl' : 'text-sm',
      winner && (tv ? 'bg-green-500/20 font-bold text-green-300' : 'bg-green-50 font-semibold text-green-800'),
      loser && (tv ? 'text-gray-500 line-through' : 'text-gray-400 line-through'),
      !name && (tv ? 'italic text-gray-500' : 'italic text-gray-400'),
    )}
  >
    <span className="truncate">{name ?? 'A definir'}</span>
    {showScore && <span className="tabular-nums font-bold">{score}</span>}
  </div>
)

export const BracketView: React.FC<Props> = ({ matches, tv = false }) => {
  const rounds = groupByRound(matches)
  const total = rounds.length

  return (
    <div className="overflow-x-auto pb-2">
      <div className="flex min-w-max gap-6">
        {rounds.map((roundMatches, index) => (
          <div key={index} className="flex w-64 flex-col" style={tv ? { width: '22rem' } : undefined}>
            <h3
              className={cn(
                'mb-3 text-center font-semibold uppercase tracking-wide',
                tv ? 'text-2xl text-yellow-300' : 'text-sm text-gray-500',
              )}
            >
              {roundName(index + 1, total)}
            </h3>
            <div className="flex flex-1 flex-col justify-around gap-4">
              {roundMatches.map((match) => {
                const finished = match.status === 'FINISHED'
                const showScore =
                  match.status !== 'SCHEDULED' && match.resultType !== 'BYE'
                return (
                  <div
                    key={match.id}
                    className={cn(
                      'overflow-hidden rounded-lg border',
                      tv
                        ? 'border-gray-600 bg-gray-800 text-white'
                        : 'border-gray-200 bg-white',
                      match.status === 'IN_PROGRESS' &&
                        (tv ? 'border-red-400 ring-2 ring-red-400' : 'border-red-300 ring-1 ring-red-300'),
                    )}
                  >
                    <Side
                      name={match.team1Name}
                      score={match.team1Score}
                      winner={finished && match.winnerId === match.team1Id}
                      loser={finished && match.winnerId !== match.team1Id && match.team1Id != null}
                      showScore={showScore}
                      tv={tv}
                    />
                    <div className={tv ? 'border-t border-gray-600' : 'border-t border-gray-100'} />
                    <Side
                      name={
                        match.resultType === 'BYE' ? 'Avanço direto' : match.team2Name
                      }
                      score={match.team2Score}
                      winner={finished && match.winnerId === match.team2Id}
                      loser={finished && match.winnerId !== match.team2Id && match.team2Id != null}
                      showScore={showScore}
                      tv={tv}
                    />
                    <div
                      className={cn(
                        'px-3 py-1 text-xs',
                        tv ? 'bg-gray-900 text-gray-300' : 'bg-gray-50 text-gray-500',
                      )}
                    >
                      {statusLabel[match.status]}
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
