# k6 Smoke/Perf Test

Script: `tests/k6/script.js`

## Local run

```bash
k6 run --summary-export=k6-summary.json -e BASE_URL=http://localhost:8080 tests/k6/script.js
```

## Deployed run

```bash
k6 run --summary-export=k6-summary.json -e BASE_URL=http://<ec2-ip-or-domain>:8080 tests/k6/script.js
```

## What this script enforces

- Real HTTP requests to:
  - `/api/hello`
  - `/api/listings`
- One-time log line proving target + status in runtime logs.
- Thresholds:
  - `http_req_failed < 5%`
  - `http_req_duration p95 < 1500ms`
  - `http_req_duration p99 < 3000ms`
  - `checks > 95%`
