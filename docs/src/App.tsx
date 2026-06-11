import { Routes, Route } from 'react-router-dom'
import Layout from './components/layout/Layout'
import Home from './pages/Home'
import Reviews from './pages/Reviews'
import Changelog from './pages/Changelog'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="resenas" element={<Reviews />} />
        <Route path="version" element={<Changelog />} />
      </Route>
    </Routes>
  )
}

export default App
