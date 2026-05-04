/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        severity: {
          p0: '#dc2626',
          p1: '#ea580c',
          p2: '#ca8a04',
          p3: '#65a30d',
          p4: '#16a34a',
        }
      }
    },
  },
  plugins: [],
}