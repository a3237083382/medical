const EMBED_MODES = ["iframe", "webview", "browser"]
const DEFAULT_TARGET = "/company/dashboard"

export function resolveCompanyEmbedMode(query = {}) {
  const mode = Array.isArray(query.embedMode) ? query.embedMode[0] : query.embedMode
  return EMBED_MODES.includes(mode) ? mode : "portal"
}

export function resolveCompanyLoginTarget(query = {}) {
  const target = pickPath(query.target) || pickPath(query.redirect)
  if (!target || !target.startsWith("/company/")) {
    return DEFAULT_TARGET
  }
  if (target === "/company/login" || target === "/company/embed") {
    return DEFAULT_TARGET
  }
  return target
}

function pickPath(value) {
  if (Array.isArray(value)) {
    return value[0]
  }
  return typeof value === "string" ? value : ""
}
