# Insurance Company Embedded Query Page

This directory contains the independently deployed query page embedded in an
insurance company's own system.

## Baseline

- Entry file: `index.html`
- Imported from the user-provided `index(1).html` prototype.
- Import date: 2026-08-14
- Baseline SHA-256: `DEE74446AFEC70FF7287370ADE1B0763EA5EC279181B3B8972CAA046A7C9767A`
- The original attachment is kept unchanged outside this repository.

The page now uses a light responsive layout and the real single-person
precision-delayed query workflow. Batch and history interfaces are not exposed
in the page yet.

## Backend CORS

The embedded API only accepts configured browser origins. Set the backend
environment variable `EMBEDDED_CORS_ALLOWED_ORIGIN_PATTERNS` to a comma-separated
list of deployment origins before production use. Localhost origins are enabled
by default for development only.

## API Base URL

`config.js` provides the runtime API base URL. It points to
`http://localhost:8088` for local development. Replace that value with the
deployed backend origin when publishing the embedded page; do not add a trailing
slash. `index.html` also supports setting `window.EMBEDDED_API_BASE` before its
application script runs.

## Project Documents

- `../docs/保险公司嵌入式查询页面设计.md`
- `../docs/保险公司嵌入式查询接口清单.md`
- `../docs/保险公司嵌入式查询数据库设计.md`
- `../docs/保险公司嵌入式查询实施计划.md`
