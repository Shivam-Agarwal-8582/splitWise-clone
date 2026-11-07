/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#ecfdf5',
          100: '#d1fae5',
          200: '#a7f3d0',
          300: '#6ee7b7',
          400: '#34d399',
          500: '#10b981', // Splitwise emerald green
          600: '#059669',
          700: '#047857',
          800: '#065f46',
          950: '#022c22',
        },
        slate: {
          850: '#151e2e',
          900: '#0f172a',
          950: '#020617'
        }
      }
    },
  },
  plugins: [],
}

