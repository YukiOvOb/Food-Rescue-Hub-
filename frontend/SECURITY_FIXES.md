Recommended fixes for Snyk vulnerabilities

Preferred fix (applies to this repo):
- Remove `react-scripts` because this project uses Vite and `react-scripts` is unused. That eliminates the transitive paths introducing `nth-check` and `webpack-dev-server` vulnerabilities.aud

What I changed:
- Removed `react-scripts` from `dependencies` in `package.json`.

Alternative if you must keep `react-scripts`:
- Use package manager overrides to force patched sub-dependencies.

NPM (v8+) `overrides` example to add to `package.json`:

```
"overrides": {
  "nth-check": "2.0.1",
  "webpack-dev-server": "5.2.1"
}
```

Yarn (classic) `resolutions` example (add to `package.json`):

```
"resolutions": {
  "nth-check": "2.0.1",
  "webpack-dev-server": "5.2.1"
}
```

Notes:
- Forcing `webpack-dev-server` to v5 may be incompatible with `react-scripts@5.x` (which expects webpack-dev-server v4). If you need live reload via CRA, upgrading `react-scripts` to a version that supports webpack-dev-server v5 (if available) is safer.
- Forcing `nth-check` to v2 is usually safe for CSS parsers, but test your app.

Commands to apply and verify (preferred path used here):

1) Remove `react-scripts` locally and install:

```bash
cd frontend
npm uninstall react-scripts
npm install
```

2) Run an audit and Snyk (if you use Snyk CLI):

```bash
npm audit --audit-level=moderate
# if you use Snyk CLI
snyk test
```

3) Verify the vulnerable packages are gone or updated:

```bash
# show where nth-check is used
npm ls nth-check || true
# show webpack-dev-server version
npm ls webpack-dev-server || true
```

If you cannot remove `react-scripts`, add the `overrides` shown above, then run `npm install` and re-run `npm audit` / `snyk test`.

Additional suggestion:
- Consider migrating fully to Vite (you already have Vite) and remove CRA artifacts to avoid maintaining `react-scripts`.

If you want, I can run the `npm install` and audit in `frontend` now and share the results. Let me know which option you prefer (remove `react-scripts` or keep and force overrides).