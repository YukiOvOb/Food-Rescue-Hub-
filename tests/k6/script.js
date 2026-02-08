import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

const rawBaseUrl = (__ENV.BASE_URL || __ENV.STAGING_URL || '').trim();

if (!rawBaseUrl) {
  throw new Error(
    'BASE_URL/STAGING_URL is empty. Pass -e BASE_URL=http://<host>:<port> (or set STAGING_URL in CI).'
  );
}

const baseUrl = rawBaseUrl.replace(/\/+$/, '');
if (!/^https?:\/\/[^/\s]+/i.test(baseUrl)) {
  throw new Error(`Invalid BASE_URL/STAGING_URL: "${rawBaseUrl}"`);
}

const endpoints = ['/api/hello', '/api/listings'];

export const options = {
  vus: Number(__ENV.K6_VUS || 10),
  duration: __ENV.K6_DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500', 'p(99)<3000'],
    checks: ['rate>0.95'],
  },
};

export default function () {
  const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
  const url = `${baseUrl}${endpoint}`;
  const res = http.get(url, { tags: { endpoint } });

  // Print once so CI logs prove we hit the endpoint.
  if (exec.vu.idInTest === 1 && exec.vu.iterationInInstance === 0) {
    console.log(`k6 target=${baseUrl} sample=${endpoint} status=${res.status}`);
  }

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 2000ms': (r) => r.timings.duration < 2000,
  });

  sleep(0.3);
}
