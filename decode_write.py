import base64
# Read base64 from file, decode, write HTML
with open('D:\\work\\proj2\\html_b64.txt', 'r') as f:
    b64_data = f.read().strip()
decoded = base64.b64decode(b64_data).decode('utf-8')
with open('D:\\work\\proj2\\RuoYi-Vue\\ruoyi-ui\\public\\prototypes\\medical-query-single-page.html', 'w', encoding='utf-8') as f:
    f.write(decoded)
print('Written:', len(decoded), 'chars')
