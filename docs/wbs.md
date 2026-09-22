# Work Breakdown Structure
## Insurance Premium Calculation & Rate Management
**Version:** 1.0 | **Derived from:** BRD v0.2

---

## WBS Overview

```
Insurance Premium Calculation & Rate Management
├── Epic 1 — Premium Calculation
├── Epic 2 — Calculation History
├── Epic 3 — Rate Table & Product Management
├── Epic 4 — Agent Authentication
└── Epic 5 — Platform & Cross-Cutting Concerns
```

---

## Epic 1 — Premium Calculation

**Goal:** An authenticated agent can submit a calculation request, receive validated inputs, retrieve a rate, and obtain annual and monthly premiums. A calculation record is persisted on success.

---

### Story 1.1 — Submit a Premium Calculation Request (US-01)

> As an Agent, I want to submit a premium calculation request so that I can obtain the annual and monthly premium for a product.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 1.1.1 | Design `POST /calculations` API contract (request/response schema, HTTP status codes) | Design | Must align with input fields: age, insured amount, payment period, product ID |
| 1.1.2 | Implement calculation request command handler (accept and route the command) | Backend | Entry point for the calculation aggregate; must enforce agent identity from auth context |
| 1.1.3 | Write integration test: happy-path calculation request accepted and routed | Test | Depends on 1.1.2, 1.4.x (auth), 1.2.x (validation) |
| 1.1.4 | Write contract test for `POST /calculations` request schema | Test | Depends on 1.1.1 |

**Dependencies:** Epic 4 (authentication must be resolved before this story is testable end-to-end); Story 1.2 (validation); Story 1.3 (rate retrieval); Story 1.4 (result persistence).

---

### Story 1.2 — Validate Calculation Inputs (US-02)

> As an Agent, I want my calculation inputs to be validated before any premium is computed so that I receive clear feedback if my inputs are outside acceptable bounds.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 1.2.1 | Define validation rules for age range (lower/upper bound) — **resolve OQ on thresholds** | Analysis | BR-01; thresholds are an open question — values must be confirmed before implementation |
| 1.2.2 | Define validation rules for insured amount range | Analysis | BR (insured amount); same open-question dependency as 1.2.1 |
| 1.2.3 | Define valid payment period enumeration | Analysis | BR (payment period); valid values must be confirmed — see open questions |
| 1.2.4 | Implement age range validator; emit `Age Out of Range Rejected` event on failure | Backend | Depends on 1.2.1; rejection event must be a named domain event |
| 1.2.5 | Implement insured amount range validator; emit `Insured Amount Out of Range Rejected` event | Backend | Depends on 1.2.2 |
| 1.2.6 | Implement payment period validator; emit `Invalid Payment Period Rejected` event | Backend | Depends on 1.2.3 |
| 1.2.7 | Implement validation orchestration: all three validators run before proceeding to rate retrieval | Backend | Short-circuit vs. collect-all behaviour must be decided (open question) |
| 1.2.8 | Map rejection events to API error responses (HTTP 422 + structured error body) | Backend | Depends on 1.2.4–1.2.6; error codes must be stable for client consumption |
| 1.2.9 | Unit tests: age out of range (below min, above max, boundary values) | Test | Depends on 1.2.4 |
| 1.2.10 | Unit tests: insured amount out of range (below min, above max, boundary values) | Test | Depends on 1.2.5 |
| 1.2.11 | Unit tests: invalid payment period (unknown value, null, empty) | Test | Depends on 1.2.6 |
| 1.2.12 | Integration test: all three rejections returned correctly via API | Test | Depends on 1.2.8 |

**Dependencies:** Threshold values from business (blocking for 1.2.4–1.2.6); Story 1.1 (command handler must exist to wire validators into).

---

### Story 1.3 — Retrieve Rate from Cache or Source (US-03)

> As the Premium Calculation process, I want to retrieve the applicable rate from the rate cache so that the premium can be computed without unnecessary delay.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 1.3.1 | Define rate lookup key structure (product ID + age + insured amount + payment period, or subset — confirm with business) | Analysis | Key structure must be agreed before cache and source implementations diverge |
| 1.3.2 | Implement rate cache lookup; detect and handle `Rate Cache Hit` | Backend | Cache technology choice is a technical constraint (see constraints table below) |
| 1.3.3 | Implement cache miss path: load rate from rate source (database / rate table store) | Backend | Depends on Epic 3 rate table storage being available; `Rate Cache Miss` event |
| 1.3.4 | Implement cache write-through on miss | Backend | Depends on 1.3.3; TTL/expiry policy must be defined |
| 1.3.5 | Implement cache expiry detection; treat expired entry as a miss and reload | Backend | `Rate Cache Expired` event; expiry duration is an open question |
| 1.3.6 | Implement rate-not-found path; emit `Rate Not Found Rejected` event | Backend | Depends on 1.3.3; must propagate as a named rejection |
| 1.3.7 | Map `Rate Not Found Rejected` to API error response | Backend | Depends on 1.3.6 |
| 1.3.8 | Unit tests: cache hit returns rate without calling source | Test | Depends on 1.3.2 |
| 1.3.9 | Unit tests: cache miss triggers source load and cache write | Test | Depends on 1.3.3, 1.3.4 |
| 1.3.10 | Unit tests: expired cache entry triggers reload | Test | Depends on 1.3.5 |
| 1.3.11 | Unit tests: rate not found returns correct rejection | Test | Depends on 1.3.6 |
| 1.3.12 | Performance test: cache hit latency under target SLA | Test | SLA value is an open question; test rig depends on 1.3.2 |

**Dependencies:** Epic 3 (rate tables must be registered and stored before the source-load path can be tested end-to-end); cache infrastructure provisioned under Epic 5.

---

### Story 1.4 — Calculate Premiums and Persist Result (US-04)

> As an Agent, I want the calculation result to be saved and returned to me so that I have a record of the trial calculation.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 1.4.1 | Implement annual premium calculation formula (rate × insured amount, or formula confirmed by business) | Backend | Formula must be confirmed; rate retrieved from Story 1.3 |
| 1.4.2 | Implement monthly premium derivation from annual premium | Backend | Conversion factor must be confirmed (open question: is it simply annual ÷ 12?) |
| 1.4.3 | Implement `Calculation Record` persistence (write to calculation record store) | Backend | Record must include: agent ID, product, inputs, annual premium, monthly premium, timestamp |
| 1.4.4 | Emit `Premium Calculation Completed` domain event after successful save | Backend | Event is marked as inferred on the board — confirm it is required |
| 1.4.5 | Implement API response: return annual premium, monthly premium, and calculation record ID | Backend | Depends on 1.4.1–1.4.3 |
| 1.4.6 | Unit tests: premium calculation formula correctness (known inputs → known outputs) | Test | Depends on 1.4.1, 1.4.2 |
| 1.4.7 | Integration test: full happy path — request → validate → retrieve rate → calculate → save → return | Test | Depends on all of Epic 1 |
| 1.4.8 | Data schema design for `Calculation Record` table/collection | Design | Must support query patterns required by Epic 2 |

**Dependencies:** Stories 1.1–1.3 must be complete; Epic 4 (agent ID must be available in context); Epic 5 (database provisioned).

---

## Epic 2 — Calculation History

**Goal:** An authenticated agent can query their own past calculation records. Access to another agent's records is blocked.

---

### Story 2.1 — Query Calculation History (US-05) *(Inferred)*

> As an Agent, I want to query my past calculation records so that I can review or reuse previous trial calculations.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 2.1.1 | Confirm query conditions with business (date range, product, status — board is silent) | Analysis | Open question; query parameters cannot be implemented until confirmed |
| 2.1.2 | Design `GET /calculations` API contract (query parameters, pagination, response schema) | Design | Depends on 2.1.1; pagination strategy must be decided |
| 2.1.3 | Implement query handler: validate query conditions | Backend | Invalid query conditions must return a structured error |
| 2.1.4 | Implement query handler: fetch records scoped to authenticated agent ID | Backend | Agent ID scoping is the primary access-control mechanism; depends on Epic 4 |
| 2.1.5 | Implement response: return matched calculation records | Backend | Depends on 2.1.4 |
| 2.1.6 | Implement failure path: inform agent when retrieval fails (system error) | Backend | Distinguish between "no records found" (empty result) and "retrieval failed" (error) |
| 2.1.7 | Unit tests: query returns only records belonging to the requesting agent | Test | Depends on 2.1.4 |
| 2.1.8 | Unit tests: empty result set returned when no records match | Test | Depends on 2.1.4 |
| 2.1.9 | Unit tests: retrieval failure returns correct error response | Test | Depends on 2.1.6 |
| 2.1.10 | Integration test: authenticated agent queries and receives their own records | Test | Depends on 2.1.5, Epic 4 |

**Dependencies:** Epic 4 (authentication); Story 1.4.8 (calculation record schema must support query patterns); Story 2.2 (access control must be in place before this story goes to production).

---

### Story 2.2 — Reject Unauthorised Access to Records (US-06) *(Inferred)*

> As an Agent, I want the system to prevent me from accessing calculation records that do not belong to me so that data is protected.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 2.2.1 | Implement ownership check: compare requesting agent ID against record owner ID | Backend | Must execute on every record retrieval; not optional |
| 2.2.2 | Emit `Unauthorised Access Rejected` event and return HTTP 403 when ownership check fails | Backend | Depends on 2.2.1; must not leak existence of the record (consider returning 404 — confirm with security) |
| 2.2.3 | Unit test: agent A cannot retrieve agent B's records | Test | Depends on 2.2.1 |
| 2.2.4 | Unit test: unauthenticated request is rejected before ownership check | Test | Depends on Epic 4 |
| 2.2.5 | Security review: confirm 403 vs. 404 response strategy for unauthorised record access | Analysis | Information-leakage concern; decision must be documented |

**Dependencies:** Epic 4 (agent identity must be in request context); Story 2.1 (query handler must call ownership check).

---

## Epic 3 — Rate Table & Product Management

**Goal:** Authorised users can register products, register rate tables, add rate table versions, and register rate entries so that agents can perform premium calculations.

> ⚠️ **Blocking open question:** The actor role for all stories in this epic is TBC (OQ-01). No implementation should begin until the role is confirmed and authentication/authorisation for that role is designed.

---

### Story 3.1 — Register a Product (US-07) *(To Be Confirmed)*

> As a [Role TBC], I want to register a product so that rate tables can be associated with it.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 3.1.1 | Confirm actor role and authorisation model for product registration (OQ-01) | Analysis | **Blocking** — no implementation until resolved |
| 3.1.2 | Define product data model (product ID, name, status, timestamps) | Design | Depends on 3.1.1 |
| 3.1.3 | Design `POST /products` API contract | Design | Depends on 3.1.1, 3.1.2 |
| 3.1.4 | Implement product registration command handler | Backend | Depends on 3.1.2, 3.1.3 |
| 3.1.5 | Implement duplicate product detection and rejection | Backend | Depends on 3.1.4; rejection event name TBC |
| 3.1.6 | Unit tests: product registered successfully | Test | Depends on 3.1.4 |
| 3.1.7 | Unit tests: duplicate product rejected | Test | Depends on 3.1.5 |

**Dependencies:** OQ-01 resolution; Epic 5 (database); Story 3.2 (product must exist before a rate table can be registered).

---

### Story 3.2 — Register a Rate Table (US-08) *(To Be Confirmed)*

> As a [Role TBC], I want to register a rate table for a product so that agents can perform premium calculations for that product.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 3.2.1 | Define rate table data model (rate table ID, product ID FK, status, effective date) | Design | Depends on 3.1.2 (product must exist) |
| 3.2.2 | Design `POST /products/{productId}/rate-tables` API contract | Design | Depends on 3.2.1 |
| 3.2.3 | Implement rate table registration command handler | Backend | Depends on 3.2.1, 3.2.2; must validate product exists |
| 3.2.4 | Implement rejection when product does not exist | Backend | Depends on 3.2.3 |
| 3.2.5 | Unit tests: rate table registered for valid product | Test | Depends on 3.2.3 |
| 3.2.6 | Unit tests: registration rejected when product not found | Test | Depends on 3.2.4 |

**Dependencies:** Story 3.1 (product must be registered first); OQ-01 resolution.

---

### Story 3.3 — Add a New Rate Table Version (US-09)

> As a [Role TBC], I want to add a new version to an existing rate table so that updated rates take effect without removing historical records.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 3.3.1 | Define rate table version data model (version ID, rate table ID FK, version number/label, effective date, status) | Design | Versioning scheme (sequential integer, semantic, date-based) must be decided |
| 3.3.2 | Design `POST /rate-tables/{rateTableId}/versions` API contract | Design | Depends on 3.3.1 |
| 3.3.3 | Implement version creation command handler | Backend | Depends on 3.3.1, 3.3.2; must validate parent rate table exists |
| 3.3.4 | Implement version activation/effective-date logic (when does a new version become active?) | Backend | Business rule is silent — open question; must be confirmed before implementation |
| 3.3.5 | Implement cache invalidation trigger when a new version is activated | Backend | Depends on 3.3.4; must invalidate relevant rate cache entries (Story 1.3) |
| 3.3.6 | Unit tests: new version added to existing rate table | Test | Depends on 3.3.3 |
| 3.3.7 | Unit tests: cache invalidated when new version activates | Test | Depends on 3.3.5 |
| 3.3.8 | Unit tests: historical versions remain accessible after new version added | Test | Depends on 3.3.3 |

**Dependencies:** Story 3.2 (rate table must exist); Story 1.3 (cache invalidation integration); OQ-01 resolution.

---

### Story 3.4 — Register Rate Entries (US-10) *(Inferred)*

> As a [Role TBC], I want to register individual rate entries within a rate table version so that the correct rate can be retrieved for any valid combination of inputs.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 3.4.1 | Define rate entry data model (entry ID, version ID FK, age, insured amount band, payment period, rate value) | Design | Key dimensions must match Story 1.3.1 lookup key structure |
| 3.4.2 | Design `POST /rate-table-versions/{versionId}/entries` API contract (single and bulk) | Design | Bulk import is likely required for real rate tables — confirm with business |
| 3.4.3 | Implement rate entry registration command handler | Backend | Depends on 3.4.1, 3.4.2; must validate parent version exists |
| 3.4.4 | Implement duplicate entry detection (same key combination within a version) | Backend | Depends on 3.4.3 |
| 3.4.5 | Implement bulk entry import (CSV or JSON batch) | Backend | Depends on 3.4.2; bulk import is a separate command from single-entry |
| 3.4.6 | Unit tests: single rate entry registered successfully | Test | Depends on 3.4.3 |
| 3.4.7 | Unit tests: duplicate entry rejected | Test | Depends on 3.4.4 |
| 3.4.8 | Integration test: rate entries registered → rate retrievable via cache path | Test | Depends on 3.4.3, Story 1.3 |

**Dependencies:** Story 3.3 (version must exist); Story 1.3.1 (lookup key structure must be agreed before entry schema is finalised); OQ-01 resolution.

---

## Epic 4 — Agent Authentication

**Goal:** Agent identity is verified before any calculation or history query is permitted. Unauthenticated or failed-authentication requests are blocked.

> ⚠️ **Open question:** The board marks authentication as *待定* (to be confirmed). The mechanism (JWT, session, OAuth, internal SSO) is not specified. This epic cannot be fully scoped until OQ on auth mechanism is resolved.

---

### Story 4.1 — Authenticate an Agent (US-11) *(To Be Confirmed)*

> As an Agent, I want my identity to be verified before I can perform any calculation or query so that only authorised agents can access the system.

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 4.1.1 | Confirm authentication mechanism with architecture team (JWT / OAuth2 / internal SSO / other) | Analysis | **Blocking** — all other auth tasks depend on this decision |
| 4.1.2 | Design authentication flow and token/session lifecycle | Design | Depends on 4.1.1; include token expiry and refresh strategy |
| 4.1.3 | Implement authentication endpoint or integrate with existing identity provider | Backend | Depends on 4.1.1, 4.1.2 |
| 4.1.4 | Implement authentication middleware: validate token/session on every protected request | Backend | Depends on 4.1.3; must inject agent ID into request context for downstream use |
| 4.1.5 | Implement failed-authentication response: block request, return HTTP 401 | Backend | Depends on 4.1.4 |
| 4.1.6 | Unit tests: valid credentials → authentication succeeds, agent ID in context | Test | Depends on 4.1.4 |
| 4.1.7 | Unit tests: invalid/expired credentials → request blocked with 401 | Test | Depends on 4.1.5 |
| 4.1.8 | Integration test: unauthenticated request to `POST /calculations` is rejected | Test | Depends on 4.1.4, Story 1.1 |
| 4.1.9 | Integration test: unauthenticated request to `GET /calculations` is rejected | Test | Depends on 4.1.4, Story 2.1 |

**Dependencies:** OQ on auth mechanism (blocking); Epic 1 and Epic 2 depend on this epic being complete before end-to-end flows can be tested.

---

## Epic 5 — Platform & Cross-Cutting Concerns

**Goal:** Provide the infrastructure, observability, and shared services that all other epics depend on.

---

### Story 5.1 — Data Store Provisioning

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 5.1.1 | Select and provision database for calculation records and rate tables | Infrastructure | Technology choice (relational vs. document) must consider query patterns from Epic 2 |
| 5.1.2 | Define and apply database schema migrations for all entities | Backend | Depends on 1.4.8, 3.1.2, 3.2.1, 3.3.1, 3.4.1 |
| 5.1.3 | Configure database connection pooling and credentials management | Infrastructure | Credentials must not be hardcoded; use secrets manager |

---

### Story 5.2 — Rate Cache Infrastructure

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 5.2.1 | Select and provision cache technology (e.g., Redis) | Infrastructure | Must support TTL-based expiry to implement `Rate Cache Expired` |
| 5.2.2 | Define cache key naming convention and TTL policy | Design | Depends on Story 1.3.1 (lookup key structure) and business confirmation of expiry duration |
| 5.2.3 | Configure cache connection and failover behaviour | Infrastructure | System must degrade gracefully if cache is unavailable (fall through to source) |

---

### Story 5.3 — Observability & Audit Logging

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 5.3.1 | Define structured log schema for all domain events (calculation completed, rejections, cache events, auth events) | Design | Log schema must be agreed before implementation begins |
| 5.3.2 | Implement structured logging at all domain event emission points | Backend | Depends on 5.3.1; covers all rejection events, `Premium Calculation Completed`, cache hit/miss/expired |
| 5.3.3 | Implement audit log for unauthorised access attempts | Backend | Depends on Story 2.2; security requirement — must be tamper-evident |
| 5.3.4 | Configure log aggregation and alerting | Infrastructure | Tooling choice (ELK, Datadog, CloudWatch, etc.) depends on hosting environment |

---

### Story 5.4 — API Gateway & Routing

#### Tasks

| ID | Task | Type | Constraint / Note |
|---|---|---|---|
| 5.4.1 | Configure API gateway or reverse proxy routing for all endpoints | Infrastructure | Must enforce authentication middleware from Story 4.1.4 at the gateway layer |
| 5.4.2 | Configure rate limiting on calculation endpoint | Infrastructure | Rate limit values are an open question; prevents abuse of the calculation engine |
| 5.4.3 | Configure TLS termination | Infrastructure | All traffic must be encrypted in transit |

---

## Dependency Map

```
Epic 4 (Auth)
    └──► Epic 1, Story 1.1 (Calculation Request)
              └──► Story 1.2 (Validation)
                        └──► Story 1.3 (Rate Retrieval)
                                  └──► Story 1.4 (Calculate & Persist)
                                            └──► Epic 2, Story 2.1 (Query History)
                                                          └──► Story 2.2 (Access Control)

Epic 3, Story 3.1 (Register Product)
    └──► Story 3.2 (Register Rate Table)
              └──► Story 3.3 (Add Version)
                        └──► Story 3.4 (Register Entries)
                                  └──► Story 1.3 (Rate Retrieval — source load path)

Epic 5, Story 5.1 (Data Store)
    └──► Story 1.4.3 (Persist Calculation Record)
    └──► Epic 3 (all rate table storage)

Epic 5, Story 5.2 (Cache Infrastructure)
    └──► Story 1.3 (Rate Cache)
    └──► Story 3.3.5 (Cache Invalidation)
```

---

## Task-to-Technical-Constraint Mapping

| Task(s) | Technical Constraint | Risk if Unresolved |
|---|---|---|
| 1.2.1, 1.2.2, 1.2.3 | Business must confirm age range thresholds, insured amount bounds, and valid payment period values before validators can be coded | Validators will be built with placeholder values and will require rework |
| 1.3.1 | Rate lookup key structure must be agreed between Epic 1 (retrieval) and Epic 3 (storage) teams before either implements their data model | Cache keys and rate entry schema will diverge, causing lookup failures |
| 1.3.2, 5.2.1 | Cache technology must be selected and provisioned; must support TTL-based expiry natively | Without TTL support, `Rate Cache Expired` logic must be implemented in application code, increasing complexity |
| 1.3.5, 5.2.2 | Cache TTL/expiry duration is an open question; must be confirmed by business | Cache may hold stale rates for an unacceptably long period, or invalidate too aggressively and degrade performance |
| 1.4.1, 1.4.2 | Premium calculation formula and annual-to-monthly conversion factor must be confirmed by business | Incorrect formula produces wrong premiums; a silent defect with regulatory risk |
| 1.4.4 | `Premium Calculation Completed` event is marked as inferred on the board; must be confirmed as a required domain event | If not required, downstream consumers built against this event will have no trigger |
| 2.2.2 | Security decision required: return HTTP 403 (confirms record exists) vs. HTTP 404 (hides existence) on unauthorised access | Information leakage risk if 403 is used without deliberate decision |
| 3.1.1–3.4.x | Actor role for all Epic 3 operations is TBC (OQ-01); authorisation model cannot be designed until role is confirmed | All Epic 3 implementation is blocked |
| 3.3.4 | Version activation / effective-date logic is not specified on the board; business rule must be confirmed | New rate table versions may activate immediately (breaking live calculations) or never (rates never update) |
| 3.4.2, 3.4.5 | Bulk rate entry import requirement is unconfirmed; real rate tables likely contain hundreds of entries | Without bulk import, rate table setup will be operationally infeasible |
| 4.1.1–4.1.x | Authentication mechanism is marked *待定*; JWT, OAuth2, session, or SSO integration each have materially different implementation paths | All of Epic 1, Epic 2, and Epic 3 access control cannot be finalised until auth mechanism is chosen |
| 5.1.1 | Database technology choice (relational vs. document) affects query capability for Epic 2 history queries and join patterns for Epic 3 rate table hierarchy | Wrong choice requires migration under time pressure |
| 5.2.3 | Cache failover behaviour must be defined: if cache is unavailable, should the system fall through to the rate source or reject the request? | Undefined failover causes inconsistent behaviour in production incidents |
| 5.4.2 | Rate limiting values on the calculation endpoint are undefined | Without rate limiting, the calculation engine is vulnerable to abuse or accidental overload |

---

## Open Questions Register (WBS View)

> These items are blockers or risks to specific tasks. Each must be assigned an owner and a resolution deadline before the sprint in which the dependent task is scheduled.

| OQ ID | Question | Blocks | Owner |
|---|---|---|---|
| OQ-01 | What role registers products, rate tables, versions, and entries? What is their authorisation model? | All of Epic 3 | Product Owner |
| OQ-02 | What are the permitted age range bounds (min/max)? | 1.2.1, 1.2.4, 1.2.9 | Business Analyst |
| OQ-03 | What are the permitted insured amount bounds (min/max)? | 1.2.2, 1.2.5, 1.2.10 | Business Analyst |
| OQ-04 | What are the valid payment period values? | 1.2.3, 1.2.6, 1.2.11 | Business Analyst |
| OQ-05 | What is the premium calculation formula? Is monthly premium simply annual ÷ 12? | 1.4.1, 1.4.2 | Business Analyst |
| OQ-06 | What is the rate lookup key structure (which input dimensions determine the rate)? | 1.3.1, 3.4.1 | Tech Lead + Business Analyst |
| OQ-07 | What is the cache TTL / expiry duration? | 5.2.2, 1.3.5 | Architect + Business |
| OQ-08 | What authentication mechanism is in use (JWT, OAuth2, SSO)? | All of Epic 4 | Architect |
| OQ-09 | Should validation collect all errors before rejecting, or short-circuit on first failure? | 1.2.7 | Product Owner |
| OQ-10 | When does a new rate table version become active (immediately on registration, or on a specified effective date)? | 3.3.4, 3.3.5 | Business Analyst |
| OQ-11 | Is `Premium Calculation Completed` a required domain event with downstream consumers? | 1.4.4 | Product Owner |
| OQ-12 | Should unauthorised record access return HTTP 403 or HTTP 404? | 2.2.2 | Security Lead |
| OQ-13 | Is bulk rate entry import required? What format (CSV, JSON)? | 3.4.2, 3.4.5 | Product Owner |