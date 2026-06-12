import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // El "base" debe coincidir EXACTAMENTE con el nombre de tu repositorio en GitHub.
  // Si tu repositorio se llama "launchers", déjalo así. Si se llama distinto, cámbialo.
  base: '/launchers/',
})
