import React from 'react'
import { useParams } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

type BracketInfo = {
  id: string
}

export const BracketPreviewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const bracket: BracketInfo | null = null

  React.useEffect(() => {
    // TODO: Carregar dados do chaveamento
    console.log('Carregando chaveamento:', id)
  }, [id])

  return (
    <div className="min-h-screen bg-black">
      <div className="flex items-center justify-center p-8">
        <Card className="w-full max-w-6xl bg-white">
          <CardHeader>
            <CardTitle>Chaveamento do Torneio</CardTitle>
          </CardHeader>
          <CardContent>
            {bracket ? (
              <div className="space-y-4">
                {/* Visualização do bracket será implementada aqui */}
                <p>Bracket será exibido aqui</p>
              </div>
            ) : (
              <p className="text-center text-gray-500">
                Nenhum chaveamento disponível
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
