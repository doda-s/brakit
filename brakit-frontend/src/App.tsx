import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { LoginPage } from '@/pages/LoginPage'
import { SignupPage } from '@/pages/SignupPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { CreateTournamentPage } from '@/pages/CreateTournamentPage'
import { EditTournamentPage } from '@/pages/EditTournamentPage'
import { TournamentDetailPage } from '@/pages/TournamentDetailPage'
import { TeamDetailPage } from '@/pages/TeamDetailPage'
import { ArbiterPanelPage } from '@/pages/ArbiterPanelPage'
import { BracketPreviewPage } from '@/pages/BracketPreviewPage'
import { RankingPreviewPage } from '@/pages/RankingPreviewPage'
import { ProtectedRoute } from '@/pages/ProtectedRoute'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />

          {/* Public preview routes */}
          <Route path="/preview/bracket/:id" element={<BracketPreviewPage />} />
          <Route path="/preview/ranking/:id" element={<RankingPreviewPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/tournaments/create"
              element={<CreateTournamentPage />}
            />
            <Route path="/tournaments/:id/edit" element={<EditTournamentPage />} />
            <Route path="/tournaments/:id" element={<TournamentDetailPage />} />
            <Route
              path="/tournaments/:id/teams/:teamId"
              element={<TeamDetailPage />}
            />
            <Route
              path="/tournaments/:id/arbiter"
              element={<ArbiterPanelPage />}
            />
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App
