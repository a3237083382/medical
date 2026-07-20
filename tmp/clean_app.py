import os

BASE = r'D:\work\proj2\RuoYi-Vue'

def remove_lines_containing(rel_path, patterns):
    path = os.path.join(BASE, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    before = len(lines)
    lines = [l for l in lines if not any(p in l for p in patterns)]
    after = len(lines)
    if before != after:
        with open(path, 'w', encoding='utf-8') as f:
            f.writelines(lines)
        print(f'  Removed {before-after} lines from {os.path.basename(path)}')
    else:
        print(f'  Nothing to remove in {os.path.basename(path)}')

def replace_text(rel_path, old, new):
    path = os.path.join(BASE, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    if old in content:
        content = content.replace(old, new)
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'  Replaced text in {os.path.basename(path)}')
    else:
        print(f'  Pattern NOT found in {os.path.basename(path)}')

# 1. BizInsuranceCompany.java
remove_lines_containing(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\BizInsuranceCompany.java', ['getAppSecret', 'setAppSecret'])
replace_text(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\BizInsuranceCompany.java',
    '/** AppSecret */\n    private String appSecret;\n\n', '')

# 2. Mapper XML
replace_text(r'ruoyi-business\src\main\resources\mapper\business\BizInsuranceCompanyMapper.xml',
    '        <result property="appSecret" column="app_secret"/>\n        ', '')
replace_text(r'ruoyi-business\src\main\resources\mapper\business\BizInsuranceCompanyMapper.xml',
    '               app_key, app_secret, balance,', '               app_key, balance,')

# 3. DataInitializer
remove_lines_containing(r'ruoyi-business\src\main\java\com\ruoyi\business\config\DataInitializer.java', ['setAppSecret'])

# 4. BizInsuranceCompanyServiceImpl
remove_lines_containing(r'ruoyi-business\src\main\java\com\ruoyi\business\service\impl\BizInsuranceCompanyServiceImpl.java', ['AppSecret', 'appSecret'])

# 5. BizInsuranceCompanyController
remove_lines_containing(r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\BizInsuranceCompanyController.java', ['setAppSecret', 'getAppSecret'])

# 6. CompanyCredentialExport
remove_lines_containing(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\CompanyCredentialExport.java', ['getAppSecret', 'setAppSecret'])
replace_text(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\CompanyCredentialExport.java',
    '    @Excel(name = "AppSecret")\n    private String appSecret;\n\n', '')

# 7. CompanyWebConfig
remove_lines_containing(r'ruoyi-business\src\main\java\com\ruoyi\business\config\CompanyWebConfig.java', ['signAuthInterceptor', 'SignAuthInterceptor'])

# 8. Delete files
files_to_delete = [
    r'ruoyi-business\src\main\java\com\ruoyi\business\util\SignUtil.java',
    r'ruoyi-business\src\main\java\com\ruoyi\business\config\SignAuthInterceptor.java',
    r'ruoyi-business\src\test\java\com\ruoyi\business\config\SignAuthInterceptorTest.java',
    r'scripts\sign_api_request.py',
    r'scripts\SignSample.java',
    r'scripts\sign-sample.py',
    r'scripts\sign-sample.sh',
]
for f in files_to_delete:
    p = os.path.join(BASE, f)
    if os.path.exists(p):
        os.remove(p)
        print(f'  Deleted {f}')

# 9. OpenMedicalQueryController - rewrite query method and remove HMAC auth
path = os.path.join(BASE, r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\OpenMedicalQueryController.java')
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove authenticate method
content = content.replace(
    '''        BizInsuranceCompany company = authenticate(request, body);
        if (company == null)
        {
            return AjaxResult.error(401, "INVALID_SIGNATURE");
        }
        if (!"0".equals(company.getStatus()))
        {
            return AjaxResult.error(403, "COMPANY_DISABLED");
        }''',
    '''        BizInsuranceCompany company = resolveCompany(request);
        if (company == null)
        {
            return AjaxResult.error(401, "INVALID_APP_KEY");
        }'''
)

# Remove body parsing code
content = content.replace(
    '''        String body = HttpHelper.getBodyString(request);
        if (StringUtils.isEmpty(body))
        {
            body = toJson(params);
        }
        BizInsuranceCompany company = authenticate(request, body);''',
    '''        BizInsuranceCompany company = resolveCompany(request);'''
)

# Add resolveCompany method and remove authenticate etc.
# First find where to insert resolveCompany
old_auth = '''    private BizInsuranceCompany authenticate(HttpServletRequest request, String body)
    {
        String appKey = trim(request.getHeader("X-App-Key"));
        String timestamp = trim(request.getHeader("X-Timestamp"));
        String nonce = trim(request.getHeader("X-Nonce"));
        String sign = trim(request.getHeader("X-Sign"));
        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(timestamp)
                || StringUtils.isEmpty(nonce) || StringUtils.isEmpty(sign))
        {
            return null;
        }
        if (!isValidTimestamp(timestamp))
        {
            return null;
        }
        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null || StringUtils.isEmpty(company.getAppSecret()))
        {
            return null;
        }
        String expected = sha256(appKey + timestamp + nonce + body + company.getAppSecret());
        return sign.equalsIgnoreCase(expected) ? company : null;
    }

    private boolean isValidTimestamp(String timestamp)
    {
        try
        {
            long value = Long.parseLong(timestamp);
            return Math.abs(System.currentTimeMillis() - value) <= SIGN_EXPIRE_MILLIS;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }'''

new_auth = '''    private BizInsuranceCompany resolveCompany(HttpServletRequest request)
    {
        String appKey = trim(request.getHeader("X-App-Key"));
        if (StringUtils.isEmpty(appKey))
        {
            return null;
        }
        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);
        if (company == null || !"0".equals(company.getStatus()))
        {
            return null;
        }
        return company;
    }'''

content = content.replace(old_auth, new_auth)

# Remove sha256, maskName, maskIdCard, toJson methods
for method in ['sha256', 'maskName', 'maskIdCard', 'toJson']:
    # Find method start and end
    import re
    pattern = r'    private String ' + method + r'.*?\n    }'
    content = re.sub(pattern, '    // removed', content, flags=re.DOTALL)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('  Updated OpenMedicalQueryController.java')

# 10. ApiMedicalQueryController - add X-App-Key auth
path2 = os.path.join(BASE, r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\ApiMedicalQueryController.java')
with open(path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

old_resolve = '''    private Long resolveCompanyId(HttpServletRequest request)
    {
        Object companyId = request.getAttribute("companyId");
        if (companyId instanceof Long value)
        {
            return value;
        }
        Object company = request.getAttribute("company");
        if (company instanceof BizInsuranceCompany value)
        {
            return value.getId();
        }
        return null;
    }'''

new_resolve = '''    private Long resolveCompanyId(HttpServletRequest request)
    {
        String appKey = request.getHeader("X-App-Key");
        if (com.ruoyi.common.utils.StringUtils.isEmpty(appKey))
        {
            return null;
        }
        com.ruoyi.business.service.IBizInsuranceCompanyService companyService = 
            com.ruoyi.business.service.impl.BizInsuranceCompanyServiceImpl.getThis();
        com.ruoyi.business.domain.BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey.trim());
        if (company == null || !"0".equals(company.getStatus()))
        {
            return null;
        }
        return company.getId();
    }'''

content2 = content2.replace(old_resolve, new_resolve)

with open(path2, 'w', encoding='utf-8') as f:
    f.write(content2)
print('  Updated ApiMedicalQueryController.java')

print('\nAll changes complete!')
