# Technical Gherkin Test Cases
## Insurance Premium Calculation & Rate Management
**Derived from:** Business Requirements v0.2 + Event Storming Analysis
**QA Version:** 1.0

---

## Coverage Report (Executive Summary)

| Epic | Feature | Business Scenarios Covered | Happy | Edge | Negative | Total Technical Scenarios |
|---|---|---|---|---|---|---|
| 1 | Premium Calculation | US-01, US-02, US-03, US-04 | 6 | 8 | 9 | 23 |
| 2 | Calculation History | US-05, US-06 | 2 | 3 | 4 | 9 |
| 3 | Rate Table & Product Management | US-07, US-08, US-09, US-10 | 4 | 4 | 6 | 14 |
| 4 | Agent Authentication | US-11 | 1 | 2 | 3 | 6 |
| **Total** | | **11 US** | **13** | **17** | **22** | **52** |

---

## Traceability Matrix

| Technical Scenario ID | User Story | Business Rule | Case Type |
|---|---|---|---|
| TC-CALC-001 | US-01, US-03, US-04 | BR-03 (cache hit) | Happy |
| TC-CALC-002 | US-01, US-03, US-04 | BR-03 (cache miss → load) | Happy |
| TC-CALC-003 | US-01, US-04 | — | Happy |
| TC-CALC-004 | US-02 | BR-01 (age lower bound) | Edge |
| TC-CALC-005 | US-02 | BR-01 (age upper bound) | Edge |
| TC-CALC-006 | US-02 | BR-02 (insured amount lower bound) | Edge |
| TC-CALC-007 | US-02 | BR-02 (insured amount upper bound) | Edge |
| TC-CALC-008 | US-02 | BR-01 | Negative |
| TC-CALC-009 | US-02 | BR-01 | Negative |
| TC-CALC-010 | US-02 | BR-02 | Negative |
| TC-CALC-011 | US-02 | BR-02 | Negative |
| TC-CALC-012 | US-02 | BR-04 (payment period) | Negative |
| TC-CALC-013 | US-03 | BR-03 (rate not found) | Negative |
| TC-CALC-014 | US-03 | BR-03 (cache expired) | Edge |
| TC-CALC-015 | US-02 | BR-01 + BR-02 (multiple violations) | Edge |
| TC-CALC-016 | US-04 | — (record persistence) | Happy |
| TC-CALC-017 | US-04 | — (record save failure) | Negative |
| TC-CALC-018 | US-02 | BR-01 (age = 0) | Edge |
| TC-CALC-019 | US-03 | BR-03 (cache expired → reload success) | Edge |
| TC-CALC-020 | US-03 | BR-03 (cache expired → reload failure) | Negative |
| TC-CALC-021 | US-01 | — (missing required fields) | Negative |
| TC-CALC-022 | US-01 | — (concurrent requests, same agent) | Edge |
| TC-CALC-023 | US-01 | — (concurrent requests, different agents) | Edge |
| TC-HIST-001 | US-05 | — | Happy |
| TC-HIST-002 | US-05 | — (no records found) | Edge |
| TC-HIST-003 | US-05 | — (invalid query conditions) | Negative |
| TC-HIST-004 | US-05 | — (retrieval failure) | Negative |
| TC-HIST-005 | US-06 | — | Negative |
| TC-HIST-006 | US-05 | — (pagination boundary) | Edge |
| TC-HIST-007 | US-05 | — (date range boundary) | Edge |
| TC-HIST-008 | US-06 | — (tampered agent ID in request) | Negative |
| TC-HIST-009 | US-05 | — (unauthenticated query attempt) | Negative |
| TC-RATE-001 | US-07 | — | Happy |
| TC-RATE-002 | US-08 | — | Happy |
| TC-RATE-003 | US-09 | — | Happy |
| TC-RATE-004 | US-10 | — | Happy |
| TC-RATE-005 | US-07 | — (duplicate product) | Negative |
| TC-RATE-006 | US-08 | — (product not registered) | Negative |
| TC-RATE-007 | US-08 | — (duplicate rate table for product) | Negative |
| TC-RATE-008 | US-09 | — (rate table not found) | Negative |
| TC-RATE-009 | US-09 | — (duplicate version) | Negative |
| TC-RATE-010 | US-10 | — (version not found) | Negative |
| TC-RATE-011 | US-09 | — (version at boundary) | Edge |
| TC-RATE-012 | US-10 | — (rate entry boundary values) | Edge |
| TC-RATE-013 | US-10 | — (duplicate rate entry key) | Negative |
| TC-RATE-014 | US-08 | — (rate table registered, cache state) | Edge |
| TC-RATE-015 | US-09 | — (new version does not invalidate old) | Edge |
| TC-RATE-016 | US-10 | — (empty rate entries list) | Edge |
| TC-AUTH-001 | US-11 | — | Happy |
| TC-AUTH-002 | US-11 | — (wrong credentials) | Negative |
| TC-AUTH-003 | US-11 | — (account locked / not found) | Negative |
| TC-AUTH-004 | US-11 | — (expired token reuse) | Negative |
| TC-AUTH-005 | US-11 | — (token boundary: just-expired) | Edge |
| TC-AUTH-006 | US-11 | — (token boundary: still-valid) | Edge |

---

## Feature: Premium Calculation

> Covers US-01, US-02, US-03, US-04

---

### TC-CALC-001 — Happy: Full calculation flow with rate cache hit

```gherkin
Scenario: [TC-CALC-001] Authenticated agent submits valid request and rate is served from cache
  Given agent "A001" is authenticated with a valid session token
  And a rate table for product "PROD-01" exists in the rate cache with key "PROD-01|AGE:30|PERIOD:ANNUAL"
  And the cached rate entry has not expired
  When agent "A001" submits a premium calculation request with:
    | field          | value   |
    | product_id     | PROD-01 |
    | age            | 30      |
    | insured_amount | 500000  |
    | payment_period | ANNUAL  |
  Then the system records a rate cache hit for key "PROD-01|AGE:30|PERIOD:ANNUAL"
  And the annual premium is calculated using the cached rate
  And the monthly premium is calculated as annual_premium / 12
  And the response contains:
    | field           | present |
    | annual_premium  | true    |
    | monthly_premium | true    |
    | calculation_id  | true    |
  And a calculation record is persisted with status "COMPLETED"
  And the response HTTP status is 200
```

---

### TC-CALC-002 — Happy: Full calculation flow with rate cache miss then successful load

```gherkin
Scenario: [TC-CALC-002] Rate is not in cache; system loads from rate source and populates cache
  Given agent "A001" is authenticated with a valid session token
  And no entry exists in the rate cache for key "PROD-02|AGE:45|PERIOD:MONTHLY"
  And the rate source contains a valid rate for product "PROD-02", age 45, payment period "MONTHLY"
  When agent "A001" submits a premium calculation request with:
    | field          | value   |
    | product_id     | PROD-02 |
    | age            | 45      |
    | insured_amount | 1000000 |
    | payment_period | MONTHLY |
  Then the system records a rate cache miss for key "PROD-02|AGE:45|PERIOD:MONTHLY"
  And the system loads the rate from the rate source
  And the rate is written to the rate cache with key "PROD-02|AGE:45|PERIOD:MONTHLY"
  And the annual premium is calculated using the loaded rate
  And the monthly premium is calculated using the loaded rate
  And a calculation record is persisted with status "COMPLETED"
  And the response HTTP status is 200
```

---

### TC-CALC-003 — Happy: Calculation result returned to agent with all required fields

```gherkin
Scenario: [TC-CALC-003] Calculation result contains annual premium, monthly premium, and calculation ID
  Given agent "A001" is authenticated with a valid session token
  And all inputs are valid for product "PROD-01", age 35, insured amount 750000, payment period "ANNUAL"
  And the applicable rate is available (cache hit or miss resolved)
  When agent "A001" submits the premium calculation request
  Then the response body contains a non-null "annual_premium"
  And the response body contains a non-null "monthly_premium"
  And the response body contains a non-null "calculation_id"
  And "monthly_premium" equals "annual_premium" divided by 12 within rounding tolerance
  And the calculation record identified by "calculation_id" can be retrieved from the data store
  And the response HTTP status is 200
```

---

### TC-CALC-004 — Edge: Age at exact lower boundary is accepted

```gherkin
Scenario: [TC-CALC-004] Age equal to the minimum permitted value passes validation
  Given agent "A001" is authenticated with a valid session token
  And the minimum permitted age for product "PROD-01" is <min_age>
  And the rate source contains a rate for product "PROD-01", age <min_age>
  When agent "A001" submits a premium calculation request with age <min_age> and all other inputs valid
  Then the request is not rejected with "AGE_OUT_OF_RANGE"
  And the calculation proceeds to rate retrieval
  And the response HTTP status is 200

  Examples:
    | min_age |
    | 0       |
    | 1       |
```

---

### TC-CALC-005 — Edge: Age at exact upper boundary is accepted

```gherkin
Scenario: [TC-CALC-005] Age equal to the maximum permitted value passes validation
  Given agent "A001" is authenticated with a valid session token
  And the maximum permitted age for product "PROD-01" is <max_age>
  And the rate source contains a rate for product "PROD-01", age <max_age>
  When agent "A001" submits a premium calculation request with age <max_age> and all other inputs valid
  Then the request is not rejected with "AGE_OUT_OF_RANGE"
  And the calculation proceeds to rate retrieval
  And the response HTTP status is 200

  Examples:
    | max_age |
    | 70      |
    | 99      |
```

---

### TC-CALC-006 — Edge: Insured amount at exact lower boundary is accepted

```gherkin
Scenario: [TC-CALC-006] Insured amount equal to the minimum permitted value passes validation
  Given agent "A001" is authenticated with a valid session token
  And the minimum permitted insured amount for product "PROD-01" is <min_amount>
  When agent "A001" submits a premium calculation request with insured_amount <min_amount> and all other inputs valid
  Then the request is not rejected with "INSURED_AMOUNT_OUT_OF_RANGE"
  And the calculation proceeds to rate retrieval
  And the response HTTP status is 200

  Examples:
    | min_amount |
    | 100000     |
```

---

### TC-CALC-007 — Edge: Insured amount at exact upper boundary is accepted

```gherkin
Scenario: [TC-CALC-007] Insured amount equal to the maximum permitted value passes validation
  Given agent "A001" is authenticated with a valid session token
  And the maximum permitted insured amount for product "PROD-01" is <max_amount>
  When agent "A001" submits a premium calculation request with insured_amount <max_amount> and all other inputs valid
  Then the request is not rejected with "INSURED_AMOUNT_OUT_OF_RANGE"
  And the calculation proceeds to rate retrieval
  And the response HTTP status is 200

  Examples:
    | max_amount |
    | 10000000   |
```

---

### TC-CALC-008 — Negative: Age below minimum is rejected

```gherkin
Scenario: [TC-CALC-008] Age one unit below the minimum permitted value triggers AGE_OUT_OF_RANGE rejection
  Given agent "A001" is authenticated with a valid session token
  And the minimum permitted age for product "PROD-01" is <min_age>
  When agent "A001" submits a premium calculation request with age <below_min> and all other inputs valid
  Then the system emits an "AGE_OUT_OF_RANGE" rejection event
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response body contains error code "AGE_OUT_OF_RANGE"
  And the response HTTP status is 422

  Examples:
    | min_age | below_min |
    | 1       | 0         |
    | 18      | 17        |
```

---

### TC-CALC-009 — Negative: Age above maximum is rejected

```gherkin
Scenario: [TC-CALC-009] Age one unit above the maximum permitted value triggers AGE_OUT_OF_RANGE rejection
  Given agent "A001" is authenticated with a valid session token
  And the maximum permitted age for product "PROD-01" is <max_age>
  When agent "A001" submits a premium calculation request with age <above_max> and all other inputs valid
  Then the system emits an "AGE_OUT_OF_RANGE" rejection event
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response body contains error code "AGE_OUT_OF_RANGE"
  And the response HTTP status is 422

  Examples:
    | max_age | above_max |
    | 70      | 71        |
    | 99      | 100       |
```

---

### TC-CALC-010 — Negative: Insured amount below minimum is rejected

```gherkin
Scenario: [TC-CALC-010] Insured amount below the minimum permitted value triggers INSURED_AMOUNT_OUT_OF_RANGE rejection
  Given agent "A001" is authenticated with a valid session token
  And the minimum permitted insured amount for product "PROD-01" is 100000
  When agent "A001" submits a premium calculation request with insured_amount 99999 and all other inputs valid
  Then the system emits an "INSURED_AMOUNT_OUT_OF_RANGE" rejection event
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response body contains error code "INSURED_AMOUNT_OUT_OF_RANGE"
  And the response HTTP status is 422
```

---

### TC-CALC-011 — Negative: Insured amount above maximum is rejected

```gherkin
Scenario: [TC-CALC-011] Insured amount above the maximum permitted value triggers INSURED_AMOUNT_OUT_OF_RANGE rejection
  Given agent "A001" is authenticated with a valid session token
  And the maximum permitted insured amount for product "PROD-01" is 10000000
  When agent "A001" submits a premium calculation request with insured_amount 10000001 and all other inputs valid
  Then the system emits an "INSURED_AMOUNT_OUT_OF_RANGE" rejection event
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response body contains error code "INSURED_AMOUNT_OUT_OF_RANGE"
  And the response HTTP status is 422
```

---

### TC-CALC-012 — Negative: Invalid payment period is rejected

```gherkin
Scenario: [TC-CALC-012] An unrecognised or unsupported payment period triggers INVALID_PAYMENT_PERIOD rejection
  Given agent "A001" is authenticated with a valid session token
  When agent "A001" submits a premium calculation request with:
    | field          | value      |
    | product_id     | PROD-01    |
    | age            | 30         |
    | insured_amount | 500000     |
    | payment_period | <invalid>  |
  Then the system emits an "INVALID_PAYMENT_PERIOD" rejection event
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response body contains error code "INVALID_PAYMENT_PERIOD"
  And the response HTTP status is 422

  Examples:
    | invalid        |
    | WEEKLY         |
    | QUARTERLY      |
    | ""             |
    | null           |
    | ANNUAL_INVALID |
```

---

### TC-CALC-013 — Negative: Rate not found in cache or source triggers rejection

```gherkin
Scenario: [TC-CALC-013] No rate exists in cache or rate source for the given inputs; calculation is rejected
  Given agent "A001" is authenticated with a valid session token
  And no entry exists in the rate cache for key "PROD-03|AGE:55|PERIOD:ANNUAL"
  And the rate source contains no rate for product "PROD-03", age 55, payment period "ANNUAL"
  When agent "A001" submits a premium calculation request with:
    | field          | value   |
    | product_id     | PROD-03 |
    | age            | 55      |
    | insured_amount | 500000  |
    | payment_period | ANNUAL  |
  Then the system records a rate cache miss
  And the system attempts to load the rate from the rate source
  And the rate source returns no result
  And the system emits a "RATE_NOT_FOUND" rejection event
  And no calculation record is persisted
  And the response body contains error code "RATE_NOT_FOUND"
  And the response HTTP status is 422
```

---

### TC-CALC-014 — Edge: Expired cache entry triggers reload from rate source

```gherkin
Scenario: [TC-CALC-014] An expired cache entry is treated as a cache miss and rate is reloaded
  Given agent "A001" is authenticated with a valid session token
  And the rate cache contains an entry for key "PROD-01|AGE:40|PERIOD:ANNUAL" that has expired
  And the rate source contains a valid current rate for product "PROD-01", age 40, payment period "ANNUAL"
  When agent "A001" submits a premium calculation request with:
    | field          | value   |
    | product_id     | PROD-01 |
    | age            | 40      |
    | insured_amount | 500000  |
    | payment_period | ANNUAL  |
  Then the system records a rate cache expired condition
  And the system loads the rate from the rate source
  And the rate cache is refreshed with the newly loaded rate
  And the premium is calculated using the refreshed rate
  And a calculation record is persisted with status "COMPLETED"
  And the response HTTP status is 200
```

---

### TC-CALC-015 — Edge: Multiple simultaneous validation failures return all error codes

```gherkin
Scenario: [TC-CALC-015] Request with both age out of range and insured amount out of range returns both rejection codes
  Given agent "A001" is authenticated with a valid session token
  And the minimum permitted age for product "PROD-01" is 1
  And the minimum permitted insured amount for product "PROD-01" is 100000
  When agent "A001" submits a premium calculation request with:
    | field          | value |
    | product_id     | PROD-01 |
    | age            | 0     |
    | insured_amount | 50000 |
    | payment_period | ANNUAL |
  Then the response body contains error code "AGE_OUT_OF_RANGE"
  And the response body contains error code "INSURED_AMOUNT_OUT_OF_RANGE"
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response HTTP status is 422
```

---

### TC-CALC-016 — Happy: Calculation record is persisted with correct agent association

```gherkin
Scenario: [TC-CALC-016] Completed calculation record is saved and linked to the requesting agent
  Given agent "A001" is authenticated with a valid session token
  And a valid premium calculation request is submitted and processed successfully
  When the premium calculation is completed
  Then a calculation record is persisted in the data store
  And the calculation record contains:
    | field           | expected_value        |
    | agent_id        | A001                  |
    | product_id      | PROD-01               |
    | status          | COMPLETED             |
    | annual_premium  | non-null, positive    |
    | monthly_premium | non-null, positive    |
    | created_at      | current timestamp     |
  And the "Premium Calculation Completed" event is emitted
```

---

### TC-CALC-017 — Negative: Calculation record save failure is surfaced to agent

```gherkin
Scenario: [TC-CALC-017] If the calculation record cannot be saved, the agent receives an error response
  Given agent "A001" is authenticated with a valid session token
  And all inputs are valid and the rate is successfully retrieved
  And the data store is unavailable or returns a write error
  When agent "A001" submits the premium calculation request
  Then the premiums are calculated in memory
  And the attempt to persist the calculation record fails
  And the agent receives an error response indicating the record could not be saved
  And the response HTTP status is 500
  And no partial calculation record is visible in the data store
```

---

### TC-CALC-018 — Edge: Age of zero is handled according to boundary rule

```gherkin
Scenario: [TC-CALC-018] Age value of zero is evaluated against the configured minimum age boundary
  Given agent "A001" is authenticated with a valid session token
  And the configured minimum permitted age for product "PROD-01" is <min_age>
  When agent "A001" submits a premium calculation request with age 0 and all other inputs valid
  Then the outcome is <expected_outcome>

  Examples:
    | min_age | expected_outcome                          |
    | 0       | calculation proceeds, HTTP 200            |
    | 1       | AGE_OUT_OF_RANGE rejection, HTTP 422      |
```

---

### TC-CALC-019 — Edge: Expired cache entry reload succeeds and result is correct

```gherkin
Scenario: [TC-CALC-019] After cache expiry and successful reload, the premium is computed from the refreshed rate
  Given agent "A001" is authenticated with a valid session token
  And the rate cache entry for "PROD-01|AGE:30|PERIOD:ANNUAL" expired 1 second ago
  And the rate source returns rate value 0.0025 for that key
  When agent "A001" submits a valid premium calculation request for product "PROD-01", age 30, insured amount 500000, payment period ANNUAL
  Then the annual premium equals 500000 * 0.0025 = 1250
  And the monthly premium equals 1250 / 12 within rounding tolerance
  And the cache entry for "PROD-01|AGE:30|PERIOD:ANNUAL" is refreshed with TTL reset
  And the response HTTP status is 200
```

---

### TC-CALC-020 — Negative: Expired cache entry reload fails; calculation is rejected

```gherkin
Scenario: [TC-CALC-020] After cache expiry, if the rate source also fails to return a rate, the calculation is rejected
  Given agent "A001" is authenticated with a valid session token
  And the rate cache entry for "PROD-01|AGE:30|PERIOD:ANNUAL" has expired
  And the rate source is unavailable or returns no rate for that key
  When agent "A001" submits a valid premium calculation request for product "PROD-01", age 30, insured amount 500000, payment period ANNUAL
  Then the system records a rate cache expired condition
  And the system attempts to reload from the rate source
  And the rate source returns no usable rate
  And the system emits a "RATE_NOT_FOUND" rejection event
  And no calculation record is persisted
  And the response HTTP status is 422
```

---

### TC-CALC-021 — Negative: Request with missing required fields is rejected before validation

```gherkin
Scenario: [TC-CALC-021] A premium calculation request missing mandatory fields is rejected at the input level
  Given agent "A001" is authenticated with a valid session token
  When agent "A001" submits a premium calculation request with the following fields absent:
    | missing_field  |
    | product_id     |
    | age            |
    | insured_amount |
    | payment_period |
  Then the response body contains a field-level validation error for each missing field
  And no rate retrieval is attempted
  And no calculation record is persisted
  And the response HTTP status is 400
```

---

### TC-CALC-022 — Edge: Concurrent requests from the same agent produce independent calculation records

```gherkin
Scenario: [TC-CALC-022] Two simultaneous calculation requests from the same agent each produce a distinct calculation record
  Given agent "A001" is authenticated with a valid session token
  And the rate cache contains valid rates for both requests
  When agent "A001" submits two concurrent premium calculation requests:
    | request | product_id | age | insured_amount | payment_period |
    | REQ-1   | PROD-01    | 30  | 500000         | ANNUAL         |
    | REQ-2   | PROD-01    | 35  | 750000         | MONTHLY        |
  Then two distinct calculation records are persisted
  And each record has a unique "calculation_id"
  And each record is associated with agent "A001"
  And neither record contains data from the other request
```

---

### TC-CALC-023 — Edge: Concurrent requests from different agents do not share state

```gherkin
Scenario: [TC-CALC-023] Simultaneous requests from different agents produce isolated calculation records
  Given agent "A001" and agent "A002" are both authenticated with valid session tokens
  And the rate cache contains valid rates for both requests
  When "A001" and "A002" simultaneously submit premium calculation requests for the same product and inputs
  Then two distinct calculation records are persisted
  And the record for "A001" is associated only with agent "A001"
  And the record for "A002" is associated only with agent "A002"
  And neither agent can see the other's calculation record in the response
```

---

## Feature: Calculation History

> Covers US-05, US-06

---

### TC-HIST-001 — Happy: Authenticated agent retrieves own calculation records

```gherkin
Scenario: [TC-HIST-001] Authenticated agent queries history and matching records are returned
  Given agent "A001" is authenticated with a valid session token
  And the data store contains 3 calculation records associated with agent "A001"
  And the query conditions specify product "PROD-01" with no date filter
  When agent "A001" submits a calculation history query with those conditions
  Then the response contains exactly the calculation records belonging to agent "A001" matching the query
  And each returned record contains:
    | field           |
    | calculation_id  |
    | product_id      |
    | annual_premium  |
    | monthly_premium |
    | created_at      |
  And the response HTTP status is 200
```

---

### TC-HIST-002 — Edge: Query returns empty result when no matching records exist

```gherkin
Scenario: [TC-HIST-002] Authenticated agent queries history but no records match the conditions
  Given agent "A001" is authenticated with a valid session token
  And the data store contains no calculation records for agent "A001" matching the query conditions
  When agent "A001" submits a calculation history query
  Then the response contains an empty records list
  And the response does not contain an error
  And the response HTTP status is 200
```

---

### TC-HIST-003 — Negative: Query with invalid conditions is rejected

```gherkin
Scenario: [TC-HIST-003] A history query with structurally invalid conditions is rejected before data retrieval
  Given agent "A001" is authenticated with a valid session token
  When agent "A001" submits a calculation history query with invalid conditions:
    | condition       | invalid_value        |
    | date_from       | "not-a-date"         |
    | date_to         | "32/13/2024"         |
    | product_id      | ""                   |
  Then the response body contains a validation error describing the invalid condition
  And no data store query is executed
  And the response HTTP status is 400
```

---

### TC-HIST-004 — Negative: Data store retrieval failure is communicated to agent

```gherkin
Scenario: [TC-HIST-004] When the data store fails during history retrieval, the agent receives a failure response
  Given agent "A001" is authenticated with a valid session token
  And the query conditions are valid
  And the data store is unavailable or returns a read error
  When agent "A001" submits a calculation history query
  Then the agent receives an error response indicating the retrieval failed
  And the response HTTP status is 500
  And no partial data is returned
```

---

### TC-HIST-005 — Negative: Agent cannot access another agent's calculation records

```gherkin
Scenario: [TC-HIST-005] Agent attempts to retrieve records belonging to a different agent and is rejected
  Given agent "A001" is authenticated with a valid session token
  And calculation records exist in the data store for agent "A002"
  When agent "A001" submits a calculation history query specifying agent_id "A002"
  Then the system emits an "UNAUTHORISED_ACCESS" rejection event
  And no records belonging to "A002" are returned
  And the response body contains error code "UNAUTHORISED_ACCESS"
  And the response HTTP status is 403
```

---

### TC-HIST-006 — Edge: Pagination boundary — last page returns correct subset

```gherkin
Scenario: [TC-HIST-006] History query with pagination returns the correct final page without overflow
  Given agent "A001" is authenticated with a valid session token
  And the data store contains 25 calculation records for agent "A001"
  And the query specifies page_size 10 and page_number 3
  When agent "A001" submits the paginated calculation history query
  Then the response contains exactly 5 records (records 21–25)
  And the response indicates this is the last page
  And the response HTTP status is 200
```

---

### TC-HIST-007 — Edge: Date range boundary — records on boundary dates are included

```gherkin
Scenario: [TC-HIST-007] History query with date range includes records created exactly on the boundary dates
  Given agent "A001" is authenticated with a valid session token
  And calculation records exist with created_at values:
    | calculation_id | created_at          |
    | CALC-10        | 2024-01-01T00:00:00 |
    | CALC-11        | 2024-01-15T12:00:00 |
    | CALC-12        | 2024-01-31T23:59:59 |
    | CALC-13        | 2024-02-01T00:00:00 |
  When agent "A001" queries history with date_from "2024-01-01" and date_to "2024-01-31"
  Then the response contains CALC-10, CALC-11, and CALC-12
  And the response does not contain CALC-13
  And the response HTTP status is 200
```

---

### TC-HIST-008 — Negative: Tampered agent ID in request header is rejected

```gherkin
Scenario: [TC-HIST-008] Request where the agent ID in the payload does not match the authenticated session is rejected
  Given agent "A001" is authenticated with a valid session token bound to agent_id "A001"
  When a history query request is submitted with agent_id "A002" in the request body while using "A001"'s session token
  Then the system detects the agent ID mismatch
  And the system emits an "UNAUTHORISED_ACCESS" rejection event
  And no records are returned
  And the response body contains error code "UNAUTHORISED_ACCESS"
  And the response HTTP status is 403
```

---

### TC-HIST-009 — Negative: Unauthenticated history query is rejected

```gherkin
Scenario: [TC-HIST-009] A calculation history query submitted without a valid session token is rejected
  Given no valid session token is present in the request
  When a calculation history query is submitted for agent "A001"
  Then the system rejects the request before any data access
  And the response body contains error code "UNAUTHENTICATED"
  And the response HTTP status is 401
```

---

## Feature: Rate Table and Product Management

> Covers US-07, US-08, US-09, US-10

---

### TC-RATE-001 — Happy: Product is successfully registered

```gherkin
Scenario: [TC-RATE-001] A new product is registered and becomes available for rate table association
  Given the actor is authenticated with a role permitted to register products
  And no product with code "PROD-NEW" exists in the system
  When the actor submits a product registration request with:
    | field        | value    |
    | product_code | PROD-NEW |
    | product_name | New Term |
  Then the product "PROD-NEW" is persisted in the product registry
  And the product is available as a target for rate table registration
  And the response HTTP status is 201
```

---

### TC-RATE-002 — Happy: Rate table is successfully registered for an existing product

```gherkin
Scenario: [TC-RATE-002] A rate table is registered against a previously registered product
  Given the actor is authenticated with a role permitted to register rate tables
  And product "PROD-01" is registered in the system
  And no rate table exists for product "PROD-01"
  When the actor submits a rate table registration request for product "PROD-01" with table_name "PROD-01-BASE"
  Then a rate table "PROD-01-BASE" is persisted and associated with product "PROD-01"
  And the rate table is available for version addition
  And the response HTTP status is 201
```

---

### TC-RATE-003 — Happy: New version is added to an existing rate table

```gherkin
Scenario: [TC-RATE-003] A new rate table version is added without affecting the existing version
  Given the actor is authenticated with a role permitted to manage rate table versions
  And rate table "PROD-01-BASE" exists with version "V1" containing rate entries
  When the actor submits a request to add version "V2" to rate table "PROD-01-BASE"
  Then version "V2" is persisted under rate table "PROD-01-BASE"
  And version "V1" remains intact and unchanged
  And the response HTTP status is 201
```

---

### TC-RATE-004 — Happy: Rate entries are registered under a rate table version

```gherkin
Scenario: [TC-RATE-004] Rate entries are successfully registered under a specific rate table version
  Given the actor is authenticated with a role permitted to register rate entries
  And rate table "PROD-01-BASE" exists with version "V2" containing no rate entries
  When the actor submits rate entries for version "V2":
    | age | payment_period | rate   |
    | 30  | ANNUAL         | 0.0025 |
    | 30  | MONTHLY        | 0.0028 |
    | 31  | ANNUAL         | 0.0026 |
  Then all 3 rate entries are persisted under version "V2" of rate table "PROD-01-BASE"
  And each entry is retrievable by its key (age + payment_period)
  And the response HTTP status is 201
```

---

### TC-RATE-005 — Negative: Duplicate product registration is rejected

```gherkin
Scenario: [TC-RATE-005] Attempting to register a product with a code that already exists is rejected
  Given the actor is authenticated with a role permitted to register products
  And product "PROD-01" is already registered in the system
  When the actor submits a product registration request with product_code "PROD-01"
  Then the system rejects the request with error code "PRODUCT_ALREADY_EXISTS"
  And the existing product "PROD-01" record is not modified
  And the response HTTP status is 409
```

---

### TC-RATE-006 — Negative: Rate table registration for a non-existent product is rejected

```gherkin
Scenario: [TC-RATE-006] Attempting to register a rate table for a product that does not exist is rejected
  Given the actor is authenticated with a role permitted to register rate tables
  And no product with code "PROD-GHOST" exists in the system
  When the actor submits a rate table registration request for product "PROD-GHOST"
  Then the system rejects the request with error code "PRODUCT_NOT_FOUND"
  And no rate table is created
  And the response HTTP status is 404
```

---

### TC-RATE-007 — Negative: Duplicate rate table for the same product is rejected

```gherkin
Scenario: [TC-RATE-007] Attempting to register a second rate table for a product that already has one is rejected
  Given the actor is authenticated with a role permitted to register rate tables
  And product "PROD-01" is registered
  And rate table "PROD-01-BASE" already exists for product "PROD-01"
  When the actor submits a rate table registration request for product "PROD-01" with table_name "PROD-01-BASE"
  Then the system rejects the request with error code "RATE_TABLE_ALREADY_EXISTS"
  And the existing rate table is not modified
  And the response HTTP status is 409
```

---

### TC-RATE-008 — Negative: Adding a version to a non-existent rate table is rejected

```gherkin
Scenario: [TC-RATE-008] Attempting to add a version to a rate table that does not exist is rejected
  Given the actor is authenticated with a role permitted to manage rate table versions
  And no rate table with name "PROD-GHOST-TABLE" exists
  When the actor submits a request to add version "V1" to rate table "PROD-GHOST-TABLE"
  Then the system rejects the request with error code "RATE_TABLE_NOT_FOUND"
  And no version is created
  And the response HTTP status is 404
```

---

### TC-RATE-009 — Negative: Duplicate version on the same rate table is rejected

```gherkin
Scenario: [TC-RATE-009] Attempting to add a version identifier that already exists on a rate table is rejected
  Given the actor is authenticated with a role permitted to manage rate table versions
  And rate table "PROD-01-BASE" already has version "V1"
  When the actor submits a request to add version "V1" to rate table "PROD-01-BASE"
  Then the system rejects the request with error code "RATE_TABLE_VERSION_ALREADY_EXISTS"
  And the existing version "V1" and its entries are not modified
  And the response HTTP status is 409
```

---

### TC-RATE-010 — Negative: Rate entry registration for a non-existent version is rejected

```gherkin
Scenario: [TC-RATE-010] Attempting to register rate entries under a version that does not exist is rejected
  Given the actor is authenticated with a role permitted to register rate entries
  And rate table "PROD-01-BASE" exists but has no version "V99"
  When the actor submits rate entries targeting version "V99" of rate table "PROD-01-BASE"
  Then the system rejects the request with error code "RATE_TABLE_VERSION_NOT_FOUND"
  And no rate entries are persisted
  And the response HTTP status is 404
```

---

### TC-RATE-011 — Edge: Adding a version with a boundary version identifier succeeds

```gherkin
Scenario: [TC-RATE-011] Version identifiers at naming boundaries are accepted
  Given the actor is authenticated with a role permitted to manage rate table versions
  And rate table "PROD-01-BASE" exists with no versions
  When the actor adds a version with identifier "<version_id>"
  Then the version is persisted successfully
  And the response HTTP status is 201

  Examples:
    | version_id                              |
    | V1                                      |
    | 1                                       |
    | 2024-01-01                              |
    | A                                       |
```

---

### TC-RATE-012 — Edge: Rate entry with boundary rate values is accepted

```gherkin
Scenario: [TC-RATE-012] Rate entries with minimum and maximum valid rate values are persisted correctly
  Given the actor is authenticated with a role permitted to register rate entries
  And rate table "PROD-01-BASE" version "V1" exists
  When the actor registers rate entries with boundary rate values:
    | age | payment_period | rate        |
    | 30  | ANNUAL         | 0.0001      |
    | 31  | ANNUAL         | 9.9999      |
  Then both entries are persisted without error
  And each entry is retrievable with its exact rate value preserved
  And the response HTTP status is 201
```

---

### TC-RATE-013 — Negative: Duplicate rate entry key within the same version is rejected

```gherkin
Scenario: [TC-RATE-013] Attempting to register a rate entry with a key that already exists in the version is rejected
  Given the actor is authenticated with a role permitted to register rate entries
  And rate table "PROD-01-BASE" version "V1" already contains a rate entry for age 30, payment period ANNUAL
  When the actor submits a rate entry registration for age 30, payment period ANNUAL under version "V1"
  Then the system rejects the request with error code "RATE_ENTRY_ALREADY_EXISTS"
  And the existing rate entry is not overwritten
  And the response HTTP status is 409
```

---

### TC-RATE-014 — Edge: Registering a rate table does not pre-populate the rate cache

```gherkin
Scenario: [TC-RATE-014] After a rate table is registered, the rate cache contains no entry for it until a calculation triggers a load
  Given the actor is authenticated with a role permitted to register rate tables
  And product "PROD-04" is registered
  When the actor registers rate table "PROD-04-BASE" for product "PROD-04"
  Then the rate table is persisted
  And the rate cache contains no entry for any key under "PROD-04"
  And the response HTTP status is 201
```

---

### TC-RATE-015 — Edge: Adding a new rate table version does not invalidate or alter the previous version's cache entries

```gherkin
Scenario: [TC-RATE-015] Adding version V2 to a rate table leaves V1 cache entries intact
  Given rate table "PROD-01-BASE" version "V1" has entries loaded into the rate cache
  And the actor is authenticated with a role permitted to manage rate table versions
  When the actor adds version "V2" to rate table "PROD-01-BASE"
  Then version "V2" is persisted
  And the rate cache entries for version "V1" remain present and unchanged
  And the response HTTP status is 201
```

---

### TC-RATE-016 — Edge: Registering an empty rate entries list is handled gracefully

```gherkin
Scenario: [TC-RATE-016] Submitting a rate entry registration request with an empty entries list is rejected or returns a clear response
  Given the actor is authenticated with a role permitted to register rate entries
  And rate table "PROD-01-BASE" version "V1" exists
  When the actor submits a rate entry registration request with an empty entries list
  Then the system returns an error code "RATE_ENTRIES_EMPTY" or equivalent
  And no entries are written to the data store
  And the response HTTP status is 400
```

---

## Feature: Agent Authentication

> Covers US-11

---

### TC-AUTH-001 — Happy: Agent authenticates successfully and receives a valid session token

```gherkin
Scenario: [TC-AUTH-001] Agent provides correct credentials and receives a session token
  Given agent "A001" exists in the system with valid credentials
  When agent "A001" submits an authentication request with correct username and password
  Then the system verifies the agent's identity successfully
  And the response contains a session token
  And the session token is valid for subsequent requests
  And the response HTTP status is 200
```

---

### TC-AUTH-002 — Negative: Authentication fails with incorrect credentials

```gherkin
Scenario: [TC-AUTH-002] Agent provides wrong password and authentication is denied
  Given agent "A001" exists in the system
  When agent "A001" submits an authentication request with an incorrect password
  Then the system rejects the authentication attempt
  And no session token is issued
  And the response body contains error code "AUTHENTICATION_FAILED"
  And the response HTTP status is 401
```

---

### TC-AUTH-003 — Negative: Authentication fails for an unknown or locked agent account

```gherkin
Scenario: [TC-AUTH-003] Authentication attempt for a non-existent or locked agent is rejected
  Given the agent account "<agent_state>" in the system
  When the actor submits an authentication request for that agent
  Then the system rejects the authentication attempt
  And no session token is issued
  And the response body contains an appropriate error code
  And the response HTTP status is 401

  Examples:
    | agent_state                        |
    | does not exist                     |
    | exists but is locked               |
    | exists but is deactivated          |
```

---

### TC-AUTH-004 — Negative: Reuse of an expired session token is rejected

```gherkin
Scenario: [TC-AUTH-004] A request submitted with an expired session token is rejected before any business logic executes
  Given agent "A001" previously authenticated and received session token "TOKEN-EXP"
  And token "TOKEN-EXP" has passed its expiry time
  When agent "A001" submits a premium calculation request using token "TOKEN-EXP"
  Then the system rejects the request due to expired token
  And no calculation logic is executed
  And no calculation record is persisted
  And the response body contains error code "TOKEN_EXPIRED"
  And the response HTTP status is 401
```

---

### TC-AUTH-005 — Edge: Token that expires exactly at request time is rejected

```gherkin
Scenario: [TC-AUTH-005] A session token whose expiry timestamp equals the current server time is treated as expired
  Given agent "A001" holds session token "TOKEN-BOUNDARY" with expiry equal to the current server timestamp
  When agent "A001" submits a request using token "TOKEN-BOUNDARY"
  Then the system treats the token as expired
  And the request is rejected with error code "TOKEN_EXPIRED"
  And the response HTTP status is 401
```

---

### TC-AUTH-006 — Edge: Token that expires one second in the future is accepted

```gherkin
Scenario: [TC-AUTH-006] A session token with one second remaining before expiry is accepted
  Given agent "A001" holds session token "TOKEN-VALID" with expiry set to current server time plus 1 second
  And all other request inputs are valid
  When agent "A001" submits a premium calculation request using token "TOKEN-VALID"
  Then the system accepts the token as valid
  And the calculation proceeds normally
  And the response HTTP status is 200
```

---

## Coverage Report (Detailed)

### Epic 1 — Premium Calculation

| Scenario ID | User Story | Scenario Description | Case Type | Validation Layer |
|---|---|---|---|---|
| TC-CALC-001 | US-01, US-03, US-04 | Full flow — cache hit | Happy | Integration |
| TC-CALC-002 | US-01, US-03, US-04 | Full flow — cache miss → load | Happy | Integration |
| TC-CALC-003 | US-01, US-04 | Result fields and record persistence | Happy | Integration |
| TC-CALC-004 | US-02 | Age at lower boundary accepted | Edge | Unit |
| TC-CALC-005 | US-02 | Age at upper boundary accepted | Edge | Unit |
| TC-CALC-006 | US-02 | Insured amount at lower boundary accepted | Edge | Unit |
| TC-CALC-007 | US-02 | Insured amount at upper boundary accepted | Edge | Unit |
| TC-CALC-008 | US-02 | Age below minimum rejected | Negative | Unit |
| TC-CALC-009 | US-02 | Age above maximum rejected | Negative | Unit |
| TC-CALC-010 | US-02 | Insured amount below minimum rejected | Negative | Unit |
| TC-CALC-011 | US-02 | Insured amount above maximum rejected | Negative | Unit |
| TC-CALC-012 | US-02 | Invalid payment period rejected | Negative | Unit |
| TC-CALC-013 | US-03 | Rate not found in cache or source | Negative | Integration |
| TC-CALC-014 | US-03 | Expired cache triggers reload | Edge | Integration |
| TC-CALC-015 | US-02 | Multiple simultaneous validation failures | Edge | Unit |
| TC-CALC-016 | US-04 | Record persisted with correct agent link | Happy | Integration |
| TC-CALC-017 | US-04 | Record save failure surfaced to agent | Negative | Integration |
| TC-CALC-018 | US-02 | Age = 0 boundary behaviour | Edge | Unit |
| TC-CALC-019 | US-03 | Expired cache reload — correct premium | Edge | Integration |
| TC-CALC-020 | US-03 | Expired cache reload failure → rejection | Negative | Integration |
| TC-CALC-021 | US-01 | Missing required fields rejected | Negative | Unit |
| TC-CALC-022 | US-01, US-04 | Concurrent requests same agent | Edge | Integration |
| TC-CALC-023 | US-01, US-04 | Concurrent requests different agents | Edge | Integration |

---

### Epic 2 — Calculation History

| Scenario ID | User Story | Scenario Description | Case Type | Validation Layer |
|---|---|---|---|---|
| TC-HIST-001 | US-05 | Authenticated agent retrieves own records | Happy | Integration |
| TC-HIST-002 | US-05 | No matching records — empty result | Edge | Integration |
| TC-HIST-003 | US-05 | Invalid query conditions rejected | Negative | Unit |
| TC-HIST-004 | US-05 | Data store failure communicated | Negative | Integration |
| TC-HIST-005 | US-06 | Access to another agent's records rejected | Negative | Integration |
| TC-HIST-006 | US-05 | Pagination last page boundary | Edge | Integration |
| TC-HIST-007 | US-05 | Date range boundary inclusion | Edge | Integration |
| TC-HIST-008 | US-06 | Tampered agent ID in request rejected | Negative | Integration |
| TC-HIST-009 | US-05 | Unauthenticated query rejected | Negative | Integration |

---

### Epic 3 — Rate Table and Product Management

| Scenario ID | User Story | Scenario Description | Case Type | Validation Layer |
|---|---|---|---|---|
| TC-RATE-001 | US-07 | Product registered successfully | Happy | Integration |
| TC-RATE-002 | US-08 | Rate table registered for existing product | Happy | Integration |
| TC-RATE-003 | US-09 | New version added without affecting old | Happy | Integration |
| TC-RATE-004 | US-10 | Rate entries registered under version | Happy | Integration |
| TC-RATE-005 | US-07 | Duplicate product rejected | Negative | Unit |
| TC-RATE-006 | US-08 | Rate table for non-existent product rejected | Negative | Unit |
| TC-RATE-007 | US-08 | Duplicate rate table for product rejected | Negative | Unit |
| TC-RATE-008 | US-09 | Version added to non-existent table rejected | Negative | Unit |
| TC-RATE-009 | US-09 | Duplicate version on same table rejected | Negative | Unit |
| TC-RATE-010 | US-10 | Rate entries for non-existent version rejected | Negative | Unit |
| TC-RATE-011 | US-09 | Boundary version identifiers accepted | Edge | Unit |
| TC-RATE-012 | US-10 | Boundary rate values accepted | Edge | Unit |
| TC-RATE-013 | US-10 | Duplicate rate entry key rejected | Negative | Unit |
| TC-RATE-014 | US-08 | Rate table registration does not pre-populate cache | Edge | Integration |
| TC-RATE-015 | US-09 | New version does not invalidate V1 cache | Edge | Integration |
| TC-RATE-016 | US-10 | Empty rate entries list rejected | Edge | Unit |

---

### Epic 4 — Agent Authentication

| Scenario ID | User Story | Scenario Description | Case Type | Validation Layer |
|---|---|---|---|---|
| TC-AUTH-001 | US-11 | Successful authentication returns token | Happy | Integration |
| TC-AUTH-002 | US-11 | Wrong credentials rejected | Negative | Integration |
| TC-AUTH-003 | US-11 | Unknown or locked account rejected | Negative | Integration |
| TC-AUTH-004 | US-11 | Expired token reuse rejected | Negative | Integration |
| TC-AUTH-005 | US-11 | Token expiry exactly at request time rejected | Edge | Unit |
| TC-AUTH-006 | US-11 | Token with 1 second remaining accepted | Edge | Unit |

---

## Open Questions Affecting Test Coverage

> The following items from the BRD's open questions section directly constrain or block test completeness. These are recorded here for stakeholder resolution.

| OQ Reference | Impact on Testing | Blocked Scenarios |
|---|---|---|
| OQ-01 — Role TBC for product/rate management | Cannot write authentication/authorisation tests for US-07 through US-10 until the permitted role is defined. TC-RATE-001 through TC-RATE-016 use placeholder "actor authenticated with permitted role". | TC-RATE-001 to TC-RATE-016 (authorisation layer) |
| Age range thresholds not specified in BR-01 | Boundary tests TC-CALC-004, TC-CALC-005, TC-CALC-008, TC-CALC-009, TC-CALC-018 use placeholder values. Exact values must be confirmed per product. | TC-CALC-004, TC-CALC-005, TC-CALC-008, TC-CALC-009, TC-CALC-018 |
| Insured amount range thresholds not specified in BR-02 | Boundary tests TC-CALC-006, TC-CALC-007, TC-CALC-010, TC-CALC-011 use placeholder values. | TC-CALC-006, TC-CALC-007, TC-CALC-010, TC-CALC-011 |
| Cache TTL value not specified | TC-CALC-014, TC-CALC-019, TC-CALC-020 cannot specify exact expiry durations. | TC-CALC-014, TC-CALC-019, TC-CALC-020 |
| Token expiry duration not specified | TC-AUTH-004, TC-AUTH-005, TC-AUTH-006 cannot specify exact token lifetime. | TC-AUTH-004, TC-AUTH-005, TC-AUTH-006 |
| Multiple rate tables per product not clarified | TC-RATE-007 assumes one rate table per product based on board evidence; if multiple are permitted, this scenario must be revised. | TC-RATE-007 |