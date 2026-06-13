import { Routes, Route } from 'react-router-dom'
import Layout from './components/layout/Layout'
import Home from './pages/Home'
import Reviews from './pages/Reviews'
import Versions from './pages/Versions'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="resenas" element={<Reviews />} />
        <Route path="versiones" element={<Versions />} />
      </Route>
    </Routes>
  )
}

export default App
