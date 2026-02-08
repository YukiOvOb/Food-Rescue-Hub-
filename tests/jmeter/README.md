# JMeter Load Test Usage

This test plan (`tests/jmeter/load-test.jmx`) supports both:
- CI sanity load test on `localhost`
- deployment load test on your EC2/public domain

## 1) Run against deployed host (recommended for report evidence)

```bash
jmeter -n \
  -t tests/jmeter/load-test.jmx \
  -l results.jtl \
  -e -o report \
  -JHOST=<your-ec2-ip-or-domain> \
  -JPORT=8080 \
  -JPROTOCOL=http \
  -JMAX_RESPONSE_MS=5000
```

Examples:
- `-JHOST=3.25.101.50`
- `-JHOST=api.yourdomain.com -JPROTOCOL=https -JPORT=443`

## 2) CI workflow target selection

`jmeter-load-performance-test` resolves target in this order:
1. `workflow_dispatch` input `jmeter_target_host` / `jmeter_target_port` / `jmeter_target_protocol`
2. repository variables `JMETER_TARGET_HOST` / `JMETER_TARGET_PORT` / `JMETER_TARGET_PROTOCOL`
3. fallback: `localhost:8080` over `http`

If target host is not `localhost`/`127.0.0.1`, the job runs in deployed mode and does not start local Docker services.

## 3) Evidence to include (lecturer-proof)

- JMeter HTML dashboard (`report/index.html` + screenshots of Summary, Throughput, and Response Times Over Time)
- Raw results (`results.jtl`)
- Runtime metrics artifact:
  - `jmeter-target.txt` (proves tested host)
  - `system-metrics-local.log` (local mode) or `system-metrics-remote.log` (remote mode when EC2 SSH secrets are configured)

For EC2, also include CloudWatch CPU/Memory screenshots during the same time window as the load test.
