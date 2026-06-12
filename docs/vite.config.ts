import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  // El "base" debe coincidir EXACTAMENTE con el nombre de tu repositorio en GitHub.
  // Si tu repositorio se llama "launchers", déjalo así. Si se llama distinto, cámbialo.
  base: '/launchers/',
})
