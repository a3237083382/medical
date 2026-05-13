import assert from "node:assert/strict"
import { resolveCompanyLoginTarget, resolveCompanyEmbedMode } from "../src/utils/companyLoginTarget.js"

assert.equal(resolveCompanyLoginTarget({ target: "/company/fee-flow" }), "/company/fee-flow")
assert.equal(resolveCompanyLoginTarget({ target: "/company/query" }), "/company/query")
assert.equal(resolveCompanyLoginTarget({ redirect: "/company/query-log" }), "/company/query-log")
assert.equal(resolveCompanyLoginTarget({ target: "/login" }), "/company/dashboard")
assert.equal(resolveCompanyLoginTarget({ target: "https://example.com" }), "/company/dashboard")
assert.equal(resolveCompanyLoginTarget({ target: "/company/login" }), "/company/dashboard")

assert.equal(resolveCompanyEmbedMode({ embedMode: "iframe" }), "iframe")
assert.equal(resolveCompanyEmbedMode({ embedMode: "webview" }), "webview")
assert.equal(resolveCompanyEmbedMode({ embedMode: "portal" }), "portal")
assert.equal(resolveCompanyEmbedMode({}), "portal")
