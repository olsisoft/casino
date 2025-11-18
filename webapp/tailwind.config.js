/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#FFF1E6',
          100: '#FFE4CC',
          200: '#FFC999',
          300: '#FFAD66',
          400: '#FF9233',
          500: '#FF7700',
          600: '#CC5F00',
          700: '#994700',
          800: '#663000',
          900: '#331800',
        },
        secondary: {
          50: '#E6F5FF',
          100: '#CCE6FF',
          200: '#99CCFF',
          300: '#66B3FF',
          400: '#3399FF',
          500: '#0080FF',
          600: '#0066CC',
          700: '#004D99',
          800: '#003366',
          900: '#001A33',
        },
        success: {
          500: '#00FF80',
          600: '#00CC66',
        },
        error: {
          500: '#FF0000',
          600: '#CC0000',
        },
        warning: {
          500: '#FFD700',
        },
        dark: {
          primary: '#0A0D14',
          secondary: '#111827',
          tertiary: '#1F2937',
          card: '#1A1F2E',
          elevated: '#252B3D',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Poppins', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'glow': '0 0 20px rgba(255, 119, 0, 0.5)',
        'glow-lg': '0 0 40px rgba(255, 119, 0, 0.6)',
      },
      animation: {
        'spin-slow': 'spin 3s linear infinite',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'bounce-slow': 'bounce 2s infinite',
      },
      backgroundImage: {
        'gradient-primary': 'linear-gradient(135deg, #FF7700 0%, #FF9233 50%, #FFAD66 100%)',
        'gradient-secondary': 'linear-gradient(135deg, #0080FF 0%, #3399FF 50%, #66B3FF 100%)',
        'gradient-success': 'linear-gradient(135deg, #00FF80 0%, #33FF99 50%, #66FFB3 100%)',
        'gradient-dark': 'linear-gradient(180deg, #0A0D14 0%, #111827 100%)',
      },
    },
  },
  plugins: [],
}
