import Cookies from 'js-cookie'

const CompanyTokenKey = 'Company-Token'

export function getCompanyToken() {
  return Cookies.get(CompanyTokenKey)
}

export function setCompanyToken(token) {
  return Cookies.set(CompanyTokenKey, token)
}

export function removeCompanyToken() {
  return Cookies.remove(CompanyTokenKey)
}
