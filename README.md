# FreeApp Selenium Automation Framework

A Selenium + TestNG Page Object Model automation framework covering UI,
database, and REST API validation, with parallel execution, HTML reporting,
and CI via GitHub Actions.

Target application: [saucedemo.com](https://www.saucedemo.com).

## Tech stack

| Layer | Tool |
|---|---|
| Browser automation | Selenium WebDriver 4 |
| Test runner | TestNG (parallel execution, data providers, listeners) |
| API testing | RestAssured |
| DB access | JDBC (embedded SQLite) |
| Reporting | ExtentReports (HTML, screenshot-on-failure) |
| Logging | Log4j2 (console + rolling file) |
| External test data | Apache POI (Excel), JSON fixtures |
| Build | Maven |
| CI | GitHub Actions |

## Features

- **Page Object Model** — `@FindBy`/`PageFactory`-based page objects, one
  flow method per page for end-to-end tests, public fields for module-level
  field checks. Page navigation centralized through a `PageObjectManager`
  factory.
- **Two-tier test organization** — `module` tests validate one page in
  isolation; `e2e` tests drive a full business flow (login → cart →
  checkout → order confirmation) to one completed outcome.
- **Parallel execution** — `testng.xml` runs test methods concurrently
  (`parallel="methods"`), backed by a `ThreadLocal<WebDriver>` so each
  thread gets its own browser instance.
- **Database validation** — an embedded SQLite fixture serves as ground
  truth for UI-displayed values, queried via plain JDBC.
- **REST API validation** — a small local HTTP server stands in for a
  backend API, secured behind an OAuth2 "client credentials" bearer-token
  flow (`POST /auth/token`, then `Authorization: Bearer <token>` on every
  request). The API client is built on RestAssured, caches and reuses its
  token, and includes reference examples for reading both flat and nested
  JSON structures (arrays of objects, arrays nested inside objects).
- **HTML reporting** — ExtentReports with automatic pass/fail logging and
  screenshot capture on failure, wired through a TestNG listener.
- **External test data** — login scenarios and data-provider rows read
  from an Excel fixture; API fixtures as JSON.
- **CI** — GitHub Actions runs the full suite on every push
  (`.github/workflows/create-test1.yml`), Chrome running headless via
  `xvfb` on a standard `ubuntu-latest` runner.

## Project structure

```
src/main/java/com/freeapp/
    base/           BaseClass - browser lifecycle
    pageobjects/    Page Object Model classes + PageObjectManager
    utility/        Utils, Log, ExtentManager, TestNGListener, ExcelReader
    integration/    Config, DBReader/DBQueryExecutor, ApiClient/LocalApiServer

src/test/java/com/test/freeapp/
    module/         Single-page, field-level tests
    e2e/            Full end-to-end business-flow tests

Configuration/      config.properties (browser, base URL, API credentials)
Database/           Embedded SQLite fixture
Api/                JSON fixtures served by the local API
TestData/           Excel test data
Postman/            A Postman collection for exploring the local API by hand
```

## Running locally

```bash
mvn test
```

Runs the full TestNG suite (`testng.xml`) against the live site with a
real Chrome browser. Reports land in `test-output/extent-report.html`;
logs in `Logs/`.

To explore the local REST API by hand: run `LocalApiServer`'s `main()`
method, then import `Postman/SauceDemoLocalApi.postman_collection.json`
into Postman.

## CI

Every push to `main` runs the full suite in GitHub Actions
(`.github/workflows/create-test1.yml`).
