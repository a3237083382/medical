import Cookies from 'js-cookie'

const CompanyTokenKey = 'Company-Token'
const CompanyInfoKey = 'Company-Info'
const CompanyEmbedModeKey = 'Company-Embed-Mode'

export function getCompanyToken() {
  return sessionStorage.getItem(CompanyTokenKey) || Cookies.get(CompanyTokenKey)
}

export function setCompanyToken(token) {
  sessionStorage.setItem(CompanyTokenKey, token)
  return token
}

export function removeCompanyToken() {
  sessionStorage.removeItem(CompanyTokenKey)
  sessionStorage.removeItem(CompanyInfoKey)
  sessionStorage.removeItem(CompanyEmbedModeKey)
  localStorage.removeItem("companyToken")
  localStorage.removeItem("companyInfo")
  return Cookies.remove(CompanyTokenKey)
}

export function getCompanyInfo() {
  return JSON.parse(sessionStorage.getItem(CompanyInfoKey) || localStorage.getItem("companyInfo") || "{}")
}

export function setCompanyInfo(info) {
  sessionStorage.setItem(CompanyInfoKey, JSON.stringify(info || {}))
}

export function getCompanyEmbedMode() {
  return sessionStorage.getItem(CompanyEmbedModeKey) || "portal"
}

export function setCompanyEmbedMode(mode) {
  sessionStorage.setItem(CompanyEmbedModeKey, mode || "portal")
}
