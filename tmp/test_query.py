# -*- coding: utf-8 -*-
import urllib.request, json

BASE_URL = 'http://localhost:8088'
COMPANY_ID = 5

query_types = [
    'medical_all', 'medical_insurance', 'medical_record',
    'medical_order', 'medical_image', 'medical_surgery', 'medical_exam',
]

passed = 0
failed = 0
for qt in query_types:
    data = json.dumps({'companyId': COMPANY_ID, 'queryType': qt, 'queryParams': {'name': '刘亮', 'idCard': '432503198706012770'}}, ensure_ascii=False).encode('utf-8')
    req = urllib.request.Request(BASE_URL + '/magic/api/external/medical/query', data, {'Content-Type': 'application/json; charset=utf-8'})
    try:
        resp = json.loads(urllib.request.urlopen(req, timeout=10).read().decode('utf-8'))
        if resp.get('code') == '0':
            records = resp.get('data', {}).get('records', [])
            fee = resp.get('fee', '?')
            print(f'[PASS] {qt}: fee={fee}, records={len(records)}')
            passed += 1
        else:
            print(f'[FAIL] {qt}: {resp.get("msg", "unknown")}')
            failed += 1
    except Exception as e:
        print(f'[FAIL] {qt}: {str(e)[:80]}')
        failed += 1

print(f'\nResults: {passed} passed, {failed} failed, {len(query_types)} total')
