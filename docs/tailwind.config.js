/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: 'var(--bg)',
        surface: 'var(--surface)',
        'surface-variant': 'var(--surface-variant)',
        primary: 'var(--primary)',
        'primary-dim': 'var(--primary-dim)',
        'on-background': 'var(--on-bg)',
        'on-surface': 'var(--on-surface)',
        cursor: 'var(--cursor)',
        border: 'var(--border)',
        error: 'var(--error)',
        prompt: 'var(--prompt)',
      },
      fontFamily: {
        mono: ['"JetBrains Mono"', 'monospace'],
        sans: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
