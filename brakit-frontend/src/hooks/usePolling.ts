import { useCallback, useEffect, useRef, useState } from 'react'

// Carrega dados e os atualiza periodicamente (RNF03). O backend ainda não oferece
// WebSocket/SSE, então usamos polling; `reload` força uma atualização imediata.
export function usePolling<T>(
  fetcher: () => Promise<T>,
  intervalMs: number,
  enabled = true,
) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<unknown>(null)
  const [isLoading, setIsLoading] = useState(true)
  const fetcherRef = useRef(fetcher)
  fetcherRef.current = fetcher

  const reload = useCallback(async () => {
    try {
      setData(await fetcherRef.current())
      setError(null)
    } catch (err) {
      setError(err)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!enabled) return
    let active = true
    const tick = () => {
      if (active && !document.hidden) void reload()
    }
    void reload()
    const timer = window.setInterval(tick, intervalMs)
    return () => {
      active = false
      window.clearInterval(timer)
    }
  }, [reload, intervalMs, enabled])

  return { data, error, isLoading, reload }
}
