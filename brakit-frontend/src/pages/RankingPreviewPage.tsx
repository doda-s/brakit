import React from 'react'
import { useParams } from 'react-router-dom'

type RankingRow = {
  teamId: string
  teamName: string
  points: number
  wins: number
  losses: number
}

export const RankingPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const ranking: RankingRow[] = []

  React.useEffect(() => {
    // TODO: Carregar ranking
    console.log('Carregando ranking:', id)
  }, [id])

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gradient-to-br from-blue-900 to-blue-950 p-8">
      <div className="w-full max-w-4xl">
        <h1 className="mb-8 text-center text-6xl font-bold text-white">
          Ranking
        </h1>

        <div className="space-y-4">
          {ranking.length === 0 ? (
            <div className="text-center text-2xl text-gray-300">
              Nenhum dado disponível
            </div>
          ) : (
            ranking.map((row, index) => (
              <div
                key={row.teamId}
                className="flex items-center justify-between rounded-lg bg-white/10 px-8 py-6 backdrop-blur"
              >
                <div className="flex items-center gap-6">
                  <div className="text-4xl font-bold text-yellow-400">
                    {index + 1}
                  </div>
                  <div>
                    <h3 className="text-3xl font-bold text-white">
                      {row.teamName}
                    </h3>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-5xl font-bold text-white">
                    {row.points}
                  </div>
                  <div className="text-lg text-gray-300">
                    {row.wins}V - {row.losses}D
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}
