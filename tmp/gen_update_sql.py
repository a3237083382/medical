import json
with open('D:/work/proj2/tmp/update_medical.ms', 'r', encoding='utf8') as f:
    mc = f.read()
with open('D:/work/proj2/tmp/update_balance.ms', 'r', encoding='utf8') as f:
    bc = f.read()
with open('D:/work/proj2/tmp/create_price.ms', 'r', encoding='utf8') as f:
    pc = f.read()
def esc(s):
    return s.replace("'", "''")
sql_lines = []
sql_lines.append('USE ry_vue;')
sql_lines.append("UPDATE magic_api_file SET file_content = '" + esc(mc) + "' WHERE file_path = 'magic-api/api/\u5bf9\u5916\u63a5\u53e3/\u5916\u90e8\u533b\u7597\u5927\u6570\u636e\u6a21\u62df\u67e5\u8be2.ms';")
sql_lines.append("UPDATE magic_api_file SET file_content = '" + esc(bc) + "' WHERE file_path = 'magic-api/api/\u5bf9\u5916\u63a5\u53e3/\u5916\u90e8\u4f59\u989d\u67e5\u8be2.ms';")
sql_lines.append("INSERT IGNORE INTO magic_api_file (file_path, file_content) VALUES ('magic-api/api/\u5bf9\u5916\u63a5\u53e3/\u5916\u90e8\u4ef7\u683c\u67e5\u8be2.ms', '" + esc(pc) + "');")
sql_lines.append('SELECT COUNT(*) as updated FROM magic_api_file WHERE file_path LIKE \'%.ms\';')
sql = '\n'.join(sql_lines)
with open('D:/work/proj2/tmp/run_update.sql', 'w', encoding='utf8') as f:
    f.write(sql)
print('SQL generated, length:', len(sql))
