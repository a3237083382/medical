import re, os

BASE = r'D:\work\proj2\RuoYi-Vue'

# Fix OpenMedicalQueryController
path = os.path.join(BASE, r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\OpenMedicalQueryController.java')
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove body reading lines
content = content.replace(
    '        String body = HttpHelper.getBodyString(request);\n        if (StringUtils.isEmpty(body))\n        {\n            body = toJson(params);\n        }\n        BizInsuranceCompany company = authenticate(request, body);',
    '        BizInsuranceCompany company = resolveCompany(request);'
)

# Remove authenticate method
content = content.replace(
    '    private BizInsuranceCompany authenticate(HttpServletRequest request, String body)\n    {\n        String appKey = trim(request.getHeader("X-App-Key"));\n        String timestamp = trim(request.getHeader("X-Timestamp"));\n        String nonce = trim(request.getHeader("X-Nonce"));\n        String sign = trim(request.getHeader("X-Sign"));\n        if (StringUtils.isEmpty(appKey) || StringUtils.isEmpty(timestamp)\n                || StringUtils.isEmpty(nonce) || StringUtils.isEmpty(sign))\n        {\n            return null;\n        }\n        if (!isValidTimestamp(timestamp))\n        {\n            return null;\n        }\n        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);\n        if (company == null || StringUtils.isEmpty(company.getAppSecret()))\n        {\n            return null;\n        }\n        String expected = sha256(appKey + timestamp + nonce + body + company.getAppSecret());\n        return sign.equalsIgnoreCase(expected) ? company : null;\n    }\n\n    private boolean isValidTimestamp(String timestamp)\n    {\n        try\n        {\n            long value = Long.parseLong(timestamp);\n            return Math.abs(System.currentTimeMillis() - value) <= SIGN_EXPIRE_MILLIS;\n        }\n        catch (NumberFormatException e)\n        {\n            return false;\n        }\n    }',
    '')
if 'resolveCompany' not in content:
    print('ERROR: authenticate method removal did NOT match! Looking for exact text...')
    # Find the method
    idx = content.find('private BizInsuranceCompany authenticate')
    if idx >= 0:
        print(f'Found authenticate at position {idx}')
        content = content[:idx]

# Find if there are still getAppSecret calls
if 'getAppSecret()' in content:
    # Replace remaining getAppSecret calls
    content = content.replace('company.getAppSecret()', '"" /* removed */')
    print('Replaced remaining getAppSecret() calls')

# Remove unused imports
content = content.replace('import java.security.MessageDigest;\n', '')
content = content.replace('import java.security.NoSuchAlgorithmException;\n', '')
content = content.replace('import com.fasterxml.jackson.core.JsonProcessingException;\n', '')
content = content.replace('import com.fasterxml.jackson.databind.ObjectMapper;\n', '')
content = content.replace('    @Autowired\n    private ObjectMapper objectMapper;\n\n', '')
content = content.replace('import com.ruoyi.common.utils.http.HttpHelper;\n', '')
content = content.replace('import java.nio.charset.StandardCharsets;\n', '')
content = content.replace('    private static final long SIGN_EXPIRE_MILLIS = 5 * 60 * 1000L;\n    ', '')

# Add resolveCompany method - find a good insertion point
if '// resolved' not in content:
    # Insert resolveCompany after recordLog
    old_end = '\n    private String toJson'
    if old_end in content:
        content = content.replace(old_end, 
'\n    private BizInsuranceCompany resolveCompany(HttpServletRequest request)\n    {\n        String appKey = trim(request.getHeader("X-App-Key"));\n        if (StringUtils.isEmpty(appKey))\n        {\n            return null;\n        }\n        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey);\n        if (company == null || !"0".equals(company.getStatus()))\n        {\n            return null;\n        }\n        return company;\n    }\n\n    private String toJson')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Fixed OpenMedicalQueryController.java')

# Fix ApiMedicalQueryController
path2 = os.path.join(BASE, r'ruoyi-admin\src\main\java\com\ruoyi\web\controller\business\ApiMedicalQueryController.java')
with open(path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

# Add import
content2 = content2.replace(
    'import com.ruoyi.business.service.IMedicalQueryService;',
    'import com.ruoyi.business.service.IMedicalQueryService;\nimport com.ruoyi.business.service.IBizInsuranceCompanyService;'
)

# Change constructor
content2 = content2.replace(
    '    private final IMedicalQueryService medicalQueryService;\n\n    public ApiMedicalQueryController(IMedicalQueryService medicalQueryService)\n    {\n        this.medicalQueryService = medicalQueryService;\n    }',
    '    private final IMedicalQueryService medicalQueryService;\n    private final IBizInsuranceCompanyService companyService;\n\n    public ApiMedicalQueryController(IMedicalQueryService medicalQueryService, IBizInsuranceCompanyService companyService)\n    {\n        this.medicalQueryService = medicalQueryService;\n        this.companyService = companyService;\n    }'
)

# Replace resolveCompanyId
content2 = content2.replace(
    '    private Long resolveCompanyId(HttpServletRequest request)\n    {\n        Object companyId = request.getAttribute("companyId");\n        if (companyId instanceof Long value)\n        {\n            return value;\n        }\n        Object company = request.getAttribute("company");\n        if (company instanceof BizInsuranceCompany value)\n        {\n            return value.getId();\n        }\n        return null;\n    }',
    '    private Long resolveCompanyId(HttpServletRequest request)\n    {\n        String appKey = request.getHeader("X-App-Key");\n        if (org.apache.commons.lang3.StringUtils.isEmpty(appKey))\n        {\n            return null;\n        }\n        BizInsuranceCompany company = companyService.selectBizInsuranceCompanyByAppKey(appKey.trim());\n        if (company == null || !"0".equals(company.getStatus()))\n        {\n            return null;\n        }\n        return company.getId();\n    }'
)

with open(path2, 'w', encoding='utf-8') as f:
    f.write(content2)
print('Fixed ApiMedicalQueryController.java')

print('\nDone!')
