import assert from "node:assert/strict"
import { readFileSync } from "node:fs"
import { dirname, resolve } from "node:path"
import { fileURLToPath } from "node:url"

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..")

function read(path) {
  return readFileSync(resolve(root, path), "utf8")
}

const router = read("src/router/index.js")
const layout = read("src/layout/company/index.vue")
const queryPage = read("src/views/company/query.vue")
const portalApi = read("src/api/business/portal.js")
const viteConfig = read("vite.config.js")

assert.match(router, /path:\s*['"]query['"]/)
assert.match(router, /import\(['"]@\/views\/company\/query['"]\)/)

assert.match(layout, /\/company\/query/)
assert.match(layout, /\u63a5\u53e3\u63a5\u5165/)
assert.doesNotMatch(layout, /\u533b\u7597\u67e5\u8be2/)

for (const text of [
  "X-App-Key",
  "X-Timestamp",
  "X-Nonce",
  "X-Sign",
  "Postman",
  "generateSignature",
  "POST /open/api/medical/query",
  "AppSecret"
]) {
  assert.match(queryPage, new RegExp(text))
}

for (const text of ["submitMedicalQuery", "patientName"]) {
  assert.doesNotMatch(queryPage, new RegExp(text))
}

assert.match(portalApi, /url:\s*['"]\/company\/api\/medical\/query-types['"]/)
assert.doesNotMatch(portalApi, /url:\s*['"]\/company\/api\/medical\/query['"]/)
assert.match(viteConfig, /['"]\/open\/api['"]/)
