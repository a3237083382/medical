import re

with open('D:\\work\\proj2\\demo_realtime_query.py', 'r', encoding='utf-8') as f:
    lines = f.readlines()

# Find section 3 boundaries
sec3_start = None
sec4_start = None
for i, line in enumerate(lines):
    if '3. AppKey' in line or '3. AppKey' in line:
        sec3_start = i
    if '4. 嵌入式' in line:
        sec4_start = i

if sec3_start is not None and sec4_start is not None:
    # Keep lines before section 3, add new section, keep lines from section 4
    new_lines = lines[:sec3_start]
    new_lines.append('# ============= 3. X-App-Key 接口（简化鉴权）\n')
    new_lines.append('print("说明：直接使用 X-App-Key 头鉴权，无需 HMAC 签名")\n')
    new_lines.append('\n')
    new_lines.append('sub("3a. 医疗数据查询（/api/v1/medical/query）")\n')
    new_lines.append('r = json_req(f"{BASE_URL}/api/v1/medical/query", {\n')
    new_lines.append('    "queryType": "medical_insurance",\n')
    new_lines.append('    "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}\n')
    new_lines.append('}, {"X-App-Key": APP_KEY})\n')
    new_lines.append('print(json.dumps(r, ensure_ascii=False, indent=2)[:600])\n')
    new_lines.append('\n')
    new_lines.append('sub("3b. 余额查询（/company/embed/medical/usage）")\n')
    new_lines.append('r = json_req(f"{BASE_URL}/company/embed/medical/usage", headers={"X-App-Key": APP_KEY})\n')
    new_lines.append('print(json.dumps(r, ensure_ascii=False, indent=2))\n')
    new_lines.append('\n')
    new_lines.extend(lines[sec4_start:])
    
    with open('D:\\work\\proj2\\demo_realtime_query.py', 'w', encoding='utf-8') as f:
        f.writelines(new_lines)
    print('Demo script updated')
else:
    print(f'Section 3 at {sec3_start}, Section 4 at {sec4_start}')
