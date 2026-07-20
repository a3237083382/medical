import os, re

BASE = r'D:\work\proj2\RuoYi-Vue'

def fix_file(rel_path, replacements):
    path = os.path.join(BASE, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    for old, new in replacements:
        if old in content:
            content = content.replace(old, new)
            print('  Fixed: ' + os.path.basename(path))
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

def remove_lines(rel_path, patterns):
    path = os.path.join(BASE, rel_path)
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    new_lines = [l for l in lines if not any(p in l for p in patterns)]
    with open(path, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
    if len(lines) != len(new_lines):
        print('  Cleaned: ' + os.path.basename(path))

# 1. Domain
fix_file(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\BizInsuranceCompany.java', [
    ('/** AppSecret */\n    private String appSecret;\n\n', ''),
])
remove_lines(r'ruoyi-business\src\main\java\com\ruoyi\business\domain\BizInsuranceCompany.java', ['getAppSecret', 'setAppSecret'])

# 2. Mapper XML
fix_file(r'ruoyi-business\src\main\resources\mapper\business\BizInsuranceCompanyMapper.xml', [
    ('<result property="appSecret" column="app_secret"/>\n        ', ''),
    ('               app_key, app_secret, balance,', '               app_key, balance,'),
])

# 3. DataInitializer
remove_lines(r'ruoyi-business\src\main\java\com\ruoyi\business\config\DataInitializer.java', ['setAppSecret'])

# 4. BizInsuranceCompanyServiceImpl
remove_lines(r'ruoyi-business\src\main\java\com\ruoyi\business\service\impl\BizInsuranceCompanyServiceImpl.java', ['appSecret', 'setAppSecret', 'getAppSecret'])

print('All done')
