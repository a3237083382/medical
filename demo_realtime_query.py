# 湖南省医疗信息实时查询平台 — 实时查询接口演示脚本
# 使用方法：python demo_realtime_query.py [base_url]
# 默认 base_url = http://localhost:8088

import urllib.request, json, hashlib, hmac, time, sys

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8088"

# ============= 测试账号信息 =============
# 由 DataInitializer 自动创建
APP_KEY = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"
APP_SECRET = "x1y2z3w4v5u6t7s8r9q0p1o2n3m4l5k6j7i8h7g6f5e4d3c2b1a0"
COMPANY_ID = 5

def json_req(url, data=None, headers=None):
    """发送 JSON 请求并打印结果"""
    if data is not None:
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
    else:
        body = None
    req = urllib.request.Request(url, data=body, headers=headers or {})
    req.add_header("Content-Type", "application/json; charset=utf-8")
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": e.code, "error": e.read().decode("utf-8", errors="replace")}
    except Exception as e:
        return {"error": str(e)}

def section(title):
    print(f"\n{'='*70}")
    print(f"  {title}")
    print(f"{'='*70}")

def sub(title):
    print(f"\n--- {title} ---")

# ============= 1. MagicAPI 直连接口 =============
section("1. MagicAPI 直连接口（/magic/api/external/）")
print("说明：无需签名校验，直接使用 companyId 查询，适用于内部调试和演示")

sub("1a. 医保信息查询（medical_insurance）")
r = json_req(f"{BASE_URL}/magic/api/external/medical/query", {
    "companyId": COMPANY_ID,
    "queryType": "medical_insurance",
    "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}
})
print(json.dumps(r, ensure_ascii=False, indent=2)[:800])

sub("1b. 电子病历查询（medical_record）")
r = json_req(f"{BASE_URL}/magic/api/external/medical/query", {
    "companyId": COMPANY_ID,
    "queryType": "medical_record",
    "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}
})
print(json.dumps(r, ensure_ascii=False, indent=2)[:800])

sub("1c. 余额查询")
r = json_req(f"{BASE_URL}/magic/api/external/balance/query?companyId={COMPANY_ID}")
print(json.dumps(r, ensure_ascii=False, indent=2))

sub("1d. 价格查询")
r = json_req(f"{BASE_URL}/magic/api/external/price/query?queryType=medical_insurance")
print(json.dumps(r, ensure_ascii=False, indent=2))

# ============= 2. 无匹配数据场景 =============
section("2. 无匹配数据场景")
sub("查询不存在的姓名")
r = json_req(f"{BASE_URL}/magic/api/external/medical/query", {
    "companyId": COMPANY_ID,
    "queryType": "medical_insurance",
    "queryParams": {"name": "不存在的人", "idCard": "110101199001011234"}
})
print(f"  resultStatus: {r.get('data', {}).get('resultStatus', 'N/A')}")
print(f"  data: {r.get('data', {}).get('data', 'N/A')}")

# ============= 3. AppKey 签名接口（SignAuthInterceptor） =============
# ============= 3. X-App-Key 接口（简化鉴权）
print("说明：直接使用 X-App-Key 头鉴权，无需 HMAC 签名")

sub("3a. 医疗数据查询（/api/v1/medical/query）")
r = json_req(f"{BASE_URL}/api/v1/medical/query", {
    "queryType": "medical_insurance",
    "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}
}, {"X-App-Key": APP_KEY})
print(json.dumps(r, ensure_ascii=False, indent=2)[:600])

sub("3b. 余额查询（/company/embed/medical/usage）")
r = json_req(f"{BASE_URL}/company/embed/medical/usage", headers={"X-App-Key": APP_KEY})
print(json.dumps(r, ensure_ascii=False, indent=2))

section("4. 嵌入式单页接口（/company/embed/medical/query）")
print("说明：仅需 X-App-Key 头，适用于嵌入式单页调用")

sub("4a. 查询类型列表")
r = json_req(f"{BASE_URL}/company/embed/medical/query-types", headers={"X-App-Key": APP_KEY})
print(json.dumps(r, ensure_ascii=False, indent=2)[:500])

sub("4b. 体检信息查询")
r = json_req(f"{BASE_URL}/company/embed/medical/query", {
    "queryType": "medical_exam",
    "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}
}, headers={"X-App-Key": APP_KEY})
print(json.dumps(r, ensure_ascii=False, indent=2)[:800])

sub("4c. 本月额度查询")
r = json_req(f"{BASE_URL}/company/embed/medical/usage", headers={"X-App-Key": APP_KEY})
print(json.dumps(r, ensure_ascii=False, indent=2))

# ============= 5. 所有查询类型演示 =============
section("5. 全部 7 种查询类型演示")
print("通过 MagicAPI 接口展示各类型的演示数据\n")

query_types = [
    ("medical_all", "医疗大数据"),
    ("medical_insurance", "医保信息"),
    ("medical_record", "电子病历"),
    ("medical_order", "医嘱信息"),
    ("medical_image", "影像信息"),
    ("medical_surgery", "近期手术"),
    ("medical_exam", "体检信息"),
]

for qt, qn in query_types:
    r = json_req(f"{BASE_URL}/magic/api/external/medical/query", {
        "companyId": COMPANY_ID,
        "queryType": qt,
        "queryParams": {"name": "刘亮", "idCard": "432503198706012770"}
    })
    data = r.get("data", {})
    inner = data
    diag = inner.get("diagnosis", "N/A")
    records = inner.get("records", [])
    count = len(records) if isinstance(records, list) else (len(records) if isinstance(records, dict) else 0)
    print(f"  [{qt:22s}] {qn:10s}  诊断: {diag:12s}  记录数: {count}")

# ============= 6. 价格一览 =============
section("6. 各查询类型价格")
for qt, qn in query_types:
    r = json_req(f"{BASE_URL}/magic/api/external/price/query?queryType={qt}")
    fee = r.get("fee", "N/A")
    print(f"  {qn:10s} ({qt:22s}): {fee} 元")

print(f"\n{'='*70}")
print("  演示完成！")
print(f"{'='*70}")
