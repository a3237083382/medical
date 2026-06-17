import urllib.request, json
appKey = "7c50b647e3b14da4aa8d8a7bf34b7b7e"
url = "http://localhost:8088/company/embed/medical/query"
body = json.dumps({"queryType":"medical_insurance","queryParams":{"name":"刘亮","idCard":"432503198706012770"}}).encode("utf-8")
req = urllib.request.Request(url, data=body, headers={"X-App-Key": appKey, "Content-Type": "application/json; charset=utf-8"})
resp = urllib.request.urlopen(req)
result = json.loads(resp.read().decode("utf-8"))
data = result.get("data", {})
inner = data.get("data", {})
print("resultStatus:", data.get("resultStatus"))
print("patientName:", inner.get("patientName", "N/A"))
print("diagnosis:", inner.get("diagnosis", "N/A"))
print("records:", len(inner.get("records", [])))
