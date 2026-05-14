import hashlib
import hmac
import json
import time
import uuid
from urllib import request

APP_KEY = "your-app-key"
APP_SECRET = "your-app-secret"
BASE_URL = "http://localhost:8088"


def sign(timestamp, nonce, body):
    payload = f"{timestamp}\n{nonce}\n{body}".encode("utf-8")
    return hmac.new(APP_SECRET.encode("utf-8"), payload, hashlib.sha256).hexdigest()


def post_medical_query():
    body = json.dumps({
        "queryType": "medical_all",
        "queryParams": {
            "name": "张三",
            "idCard": "430102199001011234"
        }
    }, ensure_ascii=False, separators=(",", ":"))
    timestamp = str(int(time.time() * 1000))
    nonce = uuid.uuid4().hex
    req = request.Request(
        BASE_URL + "/api/v1/medical/query",
        data=body.encode("utf-8"),
        headers={
            "Content-Type": "application/json;charset=utf-8",
            "X-App-Key": APP_KEY,
            "X-Timestamp": timestamp,
            "X-Nonce": nonce,
            "X-Sign": sign(timestamp, nonce, body),
        },
        method="POST",
    )
    with request.urlopen(req) as resp:
        print(resp.read().decode("utf-8"))


if __name__ == "__main__":
    post_medical_query()

