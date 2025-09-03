export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        logo: ['Sangju-Gotgam'],
        body: ['Pretendard', 'sans-serif'],
        point: ['Mungyeong-Gamhong-Apple'],
      },
      colors: {
        spurfyBlue: '#80A6FF',
        spurfyLogo: '#67C7FF',
        spurfyColor: '#9EC5FF',
        spurfyNavy: '#7FA3DA',
        spurfyAI: '#67F3EC',
      },
    },
  },
  plugins: [],
}