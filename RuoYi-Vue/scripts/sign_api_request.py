import argparse
import hashlib
import hmac
import time
import uuid
import urllib.request


def build_sign(timestamp, nonce, body, app_secret):
    payload = f"{timestamp}\n{nonce}\n{body}".encode("utf-8")
    return hmac.new(app_secret.encode("utf-8"), payload, hashlib.sha256).hexdigest()


def main():
    parser = argparse.ArgumentParser(description="Call a signed /api/v1 endpoint.")
    parser.add_argument("app_key")
    parser.add_argument("app_secret")
    parser.add_argument("method")
    parser.add_argument("url")
    parser.add_argument("body", nargs="?", default="")
    args = parser.parse_args()

    timestamp = str(int(time.time() * 1000))
    nonce = uuid.uuid4().hex
    sign = build_sign(timestamp, nonce, args.body, args.app_secret)
    headers = {
        "Content-Type": "application/json",
        "X-App-Key": args.app_key,
        "X-Timestamp": timestamp,
        "X-Nonce": nonce,
        "X-Sign": sign,
    }

    data = args.body.encode("utf-8") if args.body else None
    request = urllib.request.Request(args.url, data=data, headers=headers, method=args.method.upper())
    with urllib.request.urlopen(request, timeout=15) as response:
        print(response.status)
        print(response.read().decode("utf-8"))


if __name__ == "__main__":
    main()
