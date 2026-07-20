import re
with open('D:\\work\\proj2\\demo_realtime_query.py', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix Section 2
content = content.replace(
    'r.get(\"data\", {}).get(\"resultStatus\", \"N/A\")',
    '(r.get(\"data\") or {}).get(\"resultStatus\", \"N/A\")'
)
content = content.replace(
    'r.get(\"data\", {}).get(\"data\", \"N/A\")',
    '(r.get(\"data\") or {}).get(\"data\", \"N/A\")'
)

# Fix Section 5
content = content.replace(
    'inner = data.get(\"data\", {})',
    'inner = data'
)

with open('D:\\work\\proj2\\demo_realtime_query.py', 'w', encoding='utf-8') as f:
    f.write(content)

print('DONE')
