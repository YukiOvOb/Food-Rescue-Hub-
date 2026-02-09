# CodeQL Configuration

This repository uses GitHub's CodeQL analysis for automated security scanning and code quality checks.

## Overview

CodeQL is GitHub's powerful semantic code analysis engine that helps identify security vulnerabilities and code quality issues across multiple programming languages.

## Configuration Files

### 1. Workflow File
**Location:** `.github/workflows/codeql-analysis.yml`

This GitHub Actions workflow file defines when and how CodeQL analysis runs:
- **Triggers:**
  - On push to `main` branch
  - On pull requests to `main` branch
  - Weekly scheduled scan (every Monday at 00:00 UTC)

- **Languages Analyzed:**
  - **Java** (Backend - Spring Boot)
  - **JavaScript/TypeScript** (Frontend - React/Vite)
  - **Python** (AI Service)

### 2. Configuration File
**Location:** `.github/codeql/codeql-config.yml`

Custom configuration that defines:
- **Path Exclusions:** Test files, build artifacts, dependencies
- **Query Suites:** `security-extended` and `security-and-quality`

## How It Works

1. **Java Analysis:** Requires compilation, so the workflow builds the backend using Maven
2. **JavaScript/TypeScript Analysis:** No compilation needed, analyzed directly
3. **Python Analysis:** Sets up Python environment and installs dependencies for better analysis accuracy

## Viewing Results

1. Navigate to the **Security** tab in the GitHub repository
2. Click on **Code scanning** to see CodeQL alerts
3. Review and address any identified vulnerabilities or code quality issues

## Running Locally

While CodeQL primarily runs in GitHub Actions, you can also run it locally using the CodeQL CLI:

```bash
# Install CodeQL CLI from https://github.com/github/codeql-cli-binaries/releases

# Create a CodeQL database
codeql database create codeql-db --language=python --source-root=./ai_service

# Run analysis
codeql database analyze codeql-db --format=sarif-latest --output=results.sarif

# View results
codeql sarif analyze results.sarif
```

## Customization

To modify the CodeQL configuration:

1. **Add/Remove Languages:** Edit the `language` matrix in `.github/workflows/codeql-analysis.yml`
2. **Change Query Suites:** Modify the `queries` section in `.github/codeql/codeql-config.yml`
3. **Exclude Additional Paths:** Add patterns to `paths-ignore` in `.github/codeql/codeql-config.yml`

## Resources

- [CodeQL Documentation](https://codeql.github.com/docs/)
- [CodeQL Query Reference](https://codeql.github.com/codeql-query-help/)
- [GitHub Code Scanning](https://docs.github.com/en/code-security/code-scanning)
