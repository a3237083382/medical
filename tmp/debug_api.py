import urllib.request, json, hmac, hashlib, time

B = "http://localhost:8088"
KEY = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"
SEC = "x1y2z3w4v5u6t7s8r9q0p1o2n3m4l5k6j7i8h7g6f5e4d3c2b1a0"

def sign(ts, nn, body, sec):
    p = f"{ts}\n{nn}\n{body or ''}"
    return hmac.new(sec.encode(), p.encode(), hashlib.sha256).hexdigest()

# Test 1: Signed API - raw response
print("=== RAW RESPONSE: Signed API /api/v1/medical/query ===")
body = {"queryType":"medical_insurance","queryParams":{"name":"\u5218\u4eae","idCard":"432503198706012770"}}
ts = str(int(time.time()*1000))
nn = str(int(time.time()*1000000))
sig = sign(ts, nn, json.dumps(body, ensure_ascii=False), SEC)
req = urllib.request.Request(
    f"{B}/api/v1/medical/query",
    json.dumps(body, ensure_ascii=False).encode(),
    {"X-App-Key": KEY, "X-Timestamp": ts, "X-Nonce": nn, "X-Sign": sig, "Content-Type": "application/json"}
)
try:
    r = urllib.request.urlopen(req, timeout=10)
    resp = json.loads(r.read().decode())
    print(json.dumps(resp, ensure_ascii=False, indent=2)[:2000])
except Exception as e:
    print(f"ERROR: {e}")

# Test 2: MagicAPI medical query - raw response
print("\n=== RAW RESPONSE: MagicAPI /external/medical/query ===")
body2 = {"companyId":5,"queryType":"medical_insurance","queryParams":{"name":"\u5218\u4eae","idCard":"432503198706012770"}}
try:
    r = urllib.request.urlopen(
        f"{B}/magic/api/external/medical/query",
        json.dumps(body2, ensure_ascii=False).encode(),
        timeout=10
    )
    resp2 = json.loads(r.read().decode())
    print(json.dumps(resp2, ensure_ascii=False, indent=2)[:1000])
except Exception as e:
    print(f"ERROR: {e}")

# Test 3: Embedded endpoint with X-App-Key
print("\n=== RAW RESPONSE: Embedded /company/embed/medical/query ===")
body3 = {"queryType":"medical_exam","queryParams":{"name":"\u5218\u4eae","idCard":"432503198706012770"}}
req3 = urllib.request.Request(
    f"{B}/company/embed/medical/query",
    json.dumps(body3, ensure_ascii=False).encode(),
    {"X-App-Key": KEY, "Content-Type": "application/json"}
)
try:
    r = urllib.request.urlopen(req3, timeout=10)
    resp3 = json.loads(r.read().decode())
    print(json.dumps(resp3, ensure_ascii=False, indent=2)[:1000])
except Exception as e:
    print(f"ERROR: {e}")
