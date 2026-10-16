import React from 'react'
import { RankingRow, RankingStatus } from '@/types'
import { cn } from '@/lib/utils'

interface Props {
  rows: RankingRow[]
  tv?: boolean
}

const statusText: Record<RankingStatus, string> = {
  CHAMPION: 'Campeão',
  ACTIVE: 'Em disputa',
  ELIMINATED: 'Eliminado',
  DISQUALIFIED: 'Desqualificado',
}

export const RankingTable: React.FC<Props> = ({ rows, tv = false }) => (
  <table className={cn('w-full', tv ? 'text-2xl text-white' : 'text-sm')}>
    <thead>
      <tr className={cn('border-b', tv ? 'border-gray-500 text-yellow-300' : 'text-gray-500')}>
        <th className="py-2 text-left">#</th>
        <th className="py-2 text-left">Time</th>
        <th className="py-2 text-center">J</th>
        <th className="py-2 text-center">V</th>
        <th className="py-2 text-center">D</th>
        <th className="py-2 text-center">Saldo</th>
        <th className="py-2 text-right">Pontos</th>
        <th className="py-2 pl-4 text-left">Situação</th>
      </tr>
    </thead>
    <tbody>
      {rows.map((row) => (
        <tr
          key={row.teamId}
          className={cn(
            'border-b',
            tv ? 'border-gray-700' : 'border-gray-100',
            row.status === 'CHAMPION' && (tv ? 'bg-yellow-500/20' : 'bg-yellow-50'),
            (row.status === 'ELIMINATED' || row.status === 'DISQUALIFIED') &&
              (tv ? 'text-gray-400' : 'text-gray-500'),
          )}
        >
          <td className="py-2 font-bold">{row.position}</td>
          <td className="py-2 font-medium">{row.teamName}</td>
          <td className="py-2 text-center">{row.played}</td>
          <td className="py-2 text-center">{row.wins}</td>
          <td className="py-2 text-center">{row.losses}</td>
          <td className="py-2 text-center">
            {row.scoreDiff > 0 ? `+${row.scoreDiff}` : row.scoreDiff}
          </td>
          <td className="py-2 text-right font-bold">{row.points}</td>
          <td className="py-2 pl-4">{statusText[row.status]}</td>
        </tr>
      ))}
    </tbody>
  </table>
)
