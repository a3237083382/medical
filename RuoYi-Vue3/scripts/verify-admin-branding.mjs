import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8')

for (const envFile of ['.env.development', '.env.production', '.env.staging']) {
  assert.match(
    read(envFile),
    /VITE_APP_TITLE\s*=\s*医疗数据管理系统/,
    `${envFile} should set the app title to 医疗数据管理系统`
  )
}

const navbar = read('src/layout/components/Navbar.vue')
assert(!navbar.includes('<ruo-yi-git'), 'navbar should not render the source link')
assert(!navbar.includes('<ruo-yi-doc'), 'navbar should not render the docs link')
assert(!navbar.includes('RuoYiGit'), 'navbar should not import the source component')
assert(!navbar.includes('RuoYiDoc'), 'navbar should not import the docs component')
assert(navbar.includes('管理员'), 'navbar should display 管理员 as the nickname')

const permissionStore = read('src/store/modules/permission.js')
for (const title of ['首页', '系统管理', '系统监控', '系统工具', '若依官网']) {
  assert(
    permissionStore.includes(title),
    `permission store should explicitly hide ${title} from admin navigation`
  )
}

const tagsView = read('src/layout/components/TagsView/index.vue')
assert(tagsView.includes('hiddenAdminTagPaths'), 'tags view should explicitly hide the home tag')

const settings = read('src/settings.js')
assert(!settings.includes('RuoYi. All Rights Reserved.'), 'settings footer should not keep the RuoYi brand')
assert(settings.includes('医疗数据管理系统'), 'settings footer should use the medical data management system name')

assert(!existsSync(new URL('../src/views/index.vue', import.meta.url)), 'admin home view should be removed')

const routerConfig = read('src/router/index.js')
assert(!routerConfig.includes("redirect: '/index'"), 'admin root route should not redirect to /index')
assert(routerConfig.includes("redirect: '/business/company'"), 'admin fallback should redirect to the first business page')
assert(routerConfig.includes("path: '/noRedirect'"), 'noRedirect placeholder should redirect to a real business page')
assert(!routerConfig.includes("component: () => import('@/views/index')"), 'admin home component route should be removed')
assert(!routerConfig.includes("name: 'Index'"), 'admin Index route should be removed')
assert(!routerConfig.includes("title: '首页'"), 'admin home title should be removed from router config')

const breadcrumb = read('src/components/Breadcrumb/index.vue')
assert(!breadcrumb.includes('title: "首页"'), 'breadcrumb should not prepend 首页')
assert(!breadcrumb.includes('isDashboard'), 'breadcrumb should not keep dashboard/home special handling')

const unauthorized = read('src/views/error/401.vue')
assert(!unauthorized.includes('回首页'), '401 page should not link back to 首页')
