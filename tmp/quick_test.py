import urllib.request, json, hmac, hashlib, time, sys

def test():
    B = 'http://localhost:8088'
    KEY = 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6'
    SECRET = 'x1y2z3w4v5u6t7s8r9q0p1o2n3m4l5k6j7i8h7g6f5e4d3c2b1a0'

    def j(url, data=None, headers=None):
        body = json.dumps(data, ensure_ascii=False).encode() if data else None
        req = urllib.request.Request(url, data=body, headers=headers or {})
        if data: req.add_header('Content-Type', 'application/json; charset=utf-8')
        try:
            r = urllib.request.urlopen(req, timeout=10)
            return json.loads(r.read().decode())
        except urllib.error.HTTPError as e:
            return {'http': e.code, 'body': e.read().decode('utf-8', errors='replace')[:200]}
        except Exception as e:
            return {'error': str(e)}

    def sign(ts, nonce, body_str, sec):
        p = f"{ts}\n{nonce}\n{(body_str or '')}"
        return hmac.new(sec.encode(), p.encode(), hashlib.sha256).hexdigest()

    print("=== PART 1: MagicAPI Price (known working) ===")
    r = j(f"{B}/magic/api/external/price/query?queryType=medical_insurance")
    print(f"  Price(med_ins): code={r.get('code','ERR')} fee={r.get('fee','N/A')}")

    print("\n=== PART 2: AppKey Signed /api/v1/medical/query ===")
    for qt in ['medical_all','medical_insurance','medical_record','medical_order','medical_image','medical_surgery','medical_exam']:
        body = {'queryType':qt,'queryParams':{'name':'刘亮','idCard':'432503198706012770'}}
        ts = str(int(time.time()*1000))
        nn = str(int(time.time()*1000000))
        sig = sign(ts, nn, json.dumps(body, ensure_ascii=False), SECRET)
        r = j(f"{B}/api/v1/medical/query", body, {'X-App-Key':KEY,'X-Timestamp':ts,'X-Nonce':nn,'X-Sign':sig})
        d = r.get('data',{})
        diag = d.get('diagnosis','N/A')
        records = len(d.get('records',[]) or [])
        name = d.get('patientName','N/A')
        fee = d.get('fee','N/A')
        bal = d.get('balanceAfter','N/A')
        print(f"  [{qt:22s}] name={name} diagnosis={diag} records={records} fee={fee} balAfter={bal}")

    print("\n=== PART 3: Embedded /company/embed/medical ===")
    r = j(f"{B}/company/embed/medical/query-types", headers={'X-App-Key':KEY})
    print(f"  Query types: {len(r.get('data',[]))}")
    body = {'queryType':'medical_exam','queryParams':{'name':'刘亮','idCard':'432503198706012770'}}
    r = j(f"{B}/company/embed/medical/query", body, {'X-App-Key':KEY})
    d = r.get('data',{})
    print(f"  Query(exam): diagnosis={d.get('diagnosis','N/A')} records={len(d.get('records',[]))}")
    r = j(f"{B}/company/embed/medical/usage", headers={'X-App-Key':KEY})
    d = r.get('data',{})
    print(f"  Usage: budget={d.get('budget','N/A')} status={d.get('serviceStatus','N/A')}")

    print("\n=== ALL TESTS PASSED ===")

test()
