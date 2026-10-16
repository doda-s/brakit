import React from 'react'

interface Props {
  title: string
  subtitle?: string
  error?: string
  children: React.ReactNode
}

// Moldura dos telões: fundo escuro, alto contraste e tipografia grande (RNF02)
export const TvLayout: React.FC<Props> = ({ title, subtitle, error, children }) => (
  <div className="min-h-screen bg-gray-950 p-6 text-white md:p-10">
    <header className="mb-8 flex items-end justify-between gap-4 border-b border-gray-700 pb-4">
      <div>
        <p className="text-lg font-semibold uppercase tracking-widest text-yellow-300">
          {title}
        </p>
        {subtitle && <h1 className="text-4xl font-extrabold md:text-6xl">{subtitle}</h1>}
      </div>
      <span className="flex items-center gap-2 text-lg text-gray-300">
        <span className="h-3 w-3 animate-pulse rounded-full bg-red-500" aria-hidden="true" />
        Ao vivo
      </span>
    </header>
    {error ? (
      <p role="alert" className="text-2xl text-red-300">
        {error}
      </p>
    ) : (
      children
    )}
  </div>
)
