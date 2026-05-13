import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const repoRoot = resolve(__dirname, '..', '..')

const read = (path) => readFileSync(resolve(repoRoot, path), 'utf8')

const openController = read('RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/OpenMedicalQueryController.java')
const companyController = read('RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/business/CompanyMedicalQueryController.java')
const securityConfig = read('RuoYi-Vue/ruoyi-framework/src/main/java/com/ruoyi/framework/config/SecurityConfig.java')

assert.match(openController, /@RequestMapping\("\/open\/api\/medical"\)/, 'open medical API must be mounted under /open/api/medical')
assert.match(openController, /@Anonymous/, 'open medical API must bypass admin JWT auth and use its own signature auth')
assert.match(openController, /@PostMapping\("\/query"\)/, 'open medical API must expose POST /query')
assert.match(openController, /X-App-Key/, 'open medical API must validate X-App-Key')
assert.match(openController, /X-Timestamp/, 'open medical API must validate X-Timestamp')
assert.match(openController, /X-Nonce/, 'open medical API must validate X-Nonce')
assert.match(openController, /X-Sign/, 'open medical API must validate X-Sign')
assert.match(openController, /selectBizInsuranceCompanyByAppKey/, 'open medical API must resolve company by AppKey')
assert.match(openController, /selectBizQueryPriceByQueryType/, 'open medical API must use configured query type pricing')
assert.match(openController, /deductBalance/, 'open medical API must deduct account balance after a priced query')
assert.match(openController, /insertBizQueryLog/, 'open medical API must write query logs')
assert.match(openController, /insertBizFeeFlow/, 'open medical API must write fee flow records')
assert.match(openController, /maskIdCard/, 'open medical API must mask returned ID card data')

assert.doesNotMatch(companyController, /@PostMapping\("\/query"\)/, 'company portal must not keep the old manual medical query endpoint')
assert.match(securityConfig, /\/open\/api\/\*\*/, 'Spring Security must allow signed open API requests through to the controller')

console.log('Open API backend contract checks passed.')
