import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'

interface StatsCardProps {
  title: string
  value: string | number
  description?: string
  variant?: 'default' | 'success' | 'warning' | 'danger'
  icon?: React.ReactNode
}

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  description,
  icon,
}) => {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon && <div className="text-2xl opacity-75">{icon}</div>}
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {description && (
          <p className="text-xs text-muted-foreground">{description}</p>
        )}
      </CardContent>
    </Card>
  )
}

interface TeamRosterProps {
  teamName: string
  members: Array<{
    id: string
    name: string
    position: string
  }>
}

export const TeamRoster: React.FC<TeamRosterProps> = ({
  teamName,
  members,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">{teamName}</CardTitle>
      </CardHeader>
      <CardContent>
        {members.length === 0 ? (
          <p className="text-sm text-gray-500">Sem jogadores</p>
        ) : (
          <div className="space-y-2">
            {members.map((member) => (
              <div
                key={member.id}
                className="flex items-center justify-between rounded bg-gray-50 p-2"
              >
                <div>
                  <p className="text-sm font-semibold">{member.name}</p>
                  <Badge variant="outline" className="text-xs">
                    {member.position}
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
