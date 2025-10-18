import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,      // 포트 5173으로 고정
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 백엔드 주소
        changeOrigin: true,
        secure: false,
      }
    },

    historyApiFallback: true,
  }
});