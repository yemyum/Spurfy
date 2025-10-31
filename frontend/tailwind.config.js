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
        spurfyBlue: '#91B2FF',
        spurfyLogo: '#54DAFF',
        spurfyColor: '#9EC5FF',
        spurfyNavy: '#7FA3DA',
        spurfyAI: '#67F3EC',
      },
    },
  },
  plugins: [],
}