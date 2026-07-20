import re, os

BASE = r'D:\work\proj2\RuoYi-Vue'

# Fix OpenMedicalQueryController
path = os.path.join(BASE, r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\OpenMedicalQueryController.java')
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()
orig = content

# 1. Remove body reading lines and change authenticate to resolveCompany
content = content.replace(
    '        String body = HttpHelper.getBodyString(request);\n        if (StringUtils.isEmpty(body))\n        {\n            body = toJson(params);\n        }\n        BizInsuranceCompany company = authenticate(request, body);',
    '        BizInsuranceCompany company = resolveCompany(request);')

# 2. If the first replace didn't match, try alternate text
if content == orig:
    # Check if the text uses different whitespace
    for line in orig.split('\n'):
        if 'HttpHelper.getBodyString' in line:
            print('Found HttpHelper at line: ' + repr(line))
            break

# 3. Remove authenticate + isValidTimestamp + sha256 methods
for method_name in ['authenticate', 'isValidTimestamp', 'sha256']:
    pattern = r'\n    private (BizInsuranceCompany|boolean|String) ' + method_name + r'.*?\n    }'
    content = re.sub(pattern, '', content, count=1, flags=re.DOTALL)

# 4. Remove unused imports/fields
content = content.replace('\nimport java.security.MessageDigest;', '')
content = content.replace('\nimport java.security.NoSuchAlgorithmException;', '')
content = content.replace('\nimport com.ruoyi.common.utils.http.HttpHelper;', '')
content = content.replace('\nimport java.nio.charset.StandardCharsets;', '')
content = content.replace('\nimport com.fasterxml.jackson.core.JsonProcessingException;', '')
content = content.replace('\nimport com.fasterxml.jackson.databind.ObjectMapper;', '')
content = content.replace('\n\nimport com.ruoyi.business.service.IBizInsuranceCompanyService;', '\nimport com.ruoyi.business.service.IBizInsuranceCompanyService;')
content = content.replace('\n    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000L;', '')
content = content.replace('\n    @Autowired\n    private ObjectMapper objectMapper;', '')

# 5. Add resolveCompany method before toJson
content = content.replace('    private String toJson',
    '    private BizInsuranceCompany resolveCompany(HttpServletRequest request)\n    {\n        String appKey = trim(request.getHeader("X-App-Key"));\n        if (StringUtils.isEmpty(appKey))\n        {\n            return null;\n        }\n        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);\n        if (company == null || !"\u0030".equals(company.getStatus()))\n        {\n            return null;\n        }\n        return company;\n    }\n\n    private String toJson')

if content != orig:
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Updated OpenMedicalQueryController.java')
else:
    print('ERROR: No changes were made to OpenMedicalQueryController.java')
