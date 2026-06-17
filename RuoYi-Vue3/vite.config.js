import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

const baseUrl = process.env.VITE_PROXY_TARGET || 'http://localhost:8088' // 鍚庣鎺ュ彛

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env
  return {
    // 閮ㄧ讲鐢熶骇鐜鍜屽紑鍙戠幆澧冧笅鐨刄RL銆?
    // 榛樿鎯呭喌涓嬶紝vite 浼氬亣璁句綘鐨勫簲鐢ㄦ槸琚儴缃插湪涓€涓煙鍚嶇殑鏍硅矾寰勪笂
    // 渚嬪 https://www.ruoyi.vip/銆傚鏋滃簲鐢ㄨ閮ㄧ讲鍦ㄤ竴涓瓙璺緞涓婏紝浣犲氨闇€瑕佺敤杩欎釜閫夐」鎸囧畾杩欎釜瀛愯矾寰勩€備緥濡傦紝濡傛灉浣犵殑搴旂敤琚儴缃插湪 https://www.ruoyi.vip/admin/锛屽垯璁剧疆 baseUrl 涓?/admin/銆?
    base: VITE_APP_ENV === 'production' ? '/' : '/',
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 璁剧疆璺緞
        '~': path.resolve(__dirname, './'),
        // 璁剧疆鍒悕
        '@': path.resolve(__dirname, './src')
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    // 鎵撳寘閰嶇疆
    build: {
      // https://vite.dev/config/build-options.html
      sourcemap: command === 'build' ? false : 'inline',
      outDir: 'dist',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]'
        }
      }
    },
    // vite 鐩稿叧閰嶇疆
    server: {
      port: 3000,
      host: true,
      open: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        '/dev-api': {
          target: baseUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
        '/open/api': {
          target: baseUrl,
          changeOrigin: true
        },
         // springdoc proxy
         '^/v3/api-docs/(.*)': {
          target: baseUrl,
          changeOrigin: true,
        }
      }
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  }
})
