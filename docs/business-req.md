# Business Requirements Document

## Insurance Premium Calculation & Rate Management

**Derived exclusively from Event Storming whiteboard analysis**
**Version:** 0.2 — Draft for stakeholder review
**Status:** ⚠️ Contains open questions where the board is silent or ambiguous

---

## Table of Contents

1. [Glossary](#glossary)
2. [User Stories](#user-stories)
3. [Business Rules](#business-rules)
4. [Open Questions](#open-questions)
5. [Business-Level Gherkin (BDD Scenarios)](#business-level-gherkin-bdd-scenarios)

---

## Glossary

> Terms are taken verbatim from the sticky-note vocabulary. No term has been invented beyond what appears on the board.

| Term (中文) | Term (English) | Definition (from board) |
|---|---|---|
| 業務員 | Agent | The actor who initiates premium calculation requests and queries calculation history. Identity must be verified before access is granted. |
| 試算請求 | Premium Calculation Request | The initiating command submitted to trigger a premium calculation. |
| 試算輸入 | Calculation Input | The set of values provided as part of a premium calculation request, subject to validation. |
| 年繳保費 | Annual Premium | The calculated premium amount expressed on an annual payment basis. |
| 月繳保費 | Monthly Premium | The calculated premium amount expressed on a monthly payment basis. |
| 費率 | Rate | The rate value used to compute a premium. Retrieved from the rate cache or loaded from the rate source. |
| 費率表 | Rate Table | A structured set of rates associated with a product. Must be registered before rates can be retrieved. |
| 費率表版本 | Rate Table Version | A distinct version of a Rate Table. A new version can be added to an existing Rate Table. |
| 費率明細 | Rate Entry / Rate Entries | Individual line items within a Rate Table. |
| 費率快取 | Rate Cache | A temporary store of rate data used to serve calculation requests without reloading from the rate source. |
| 費率快取命中 | Rate Cache Hit | The condition in which a requested rate is found in the Rate Cache. |
| 費率快取未命中 | Rate Cache Miss | The condition in which a requested rate is not found in the Rate Cache. |
| 費率快取逾期失效 | Rate Cache Expired | The condition in which a Rate Cache entry is no longer valid due to expiry. |
| 商品 | Product | An insurance product for which a Rate Table can be registered. |
| 被保投保 | Payment Period | The payment period arrangement for an insured policy. Must be valid for a calculation to proceed. |
| 保額 / 明細 | Insured Amount | The coverage amount submitted as part of a calculation request. Subject to range validation. |
| 年齡 | Age | The age of the insured submitted as part of a calculation request. Subject to range validation. |
| 試算紀錄 | Calculation Record | A persisted record of a completed premium calculation. |
| 試算紀錄查詢 | Calculation History Query | A request to retrieve one or more Calculation Records. |
| 保費試算完成 | Premium Calculation Completed | The terminal successful outcome of a full premium calculation flow. *(Inferred on board — see Open Questions)* |
| 非本人 | Unauthorised Party | A party attempting to access resources they are not permitted to access. |

---

## User Stories

> Actor labels are taken from the board. Where the board marks an event as *待定* (to be confirmed) or *推論* (inferred), the corresponding story is flagged accordingly.

---

### Epic 1 — Premium Calculation

---

**US-01 — Request a Premium Calculation**

> As an **Agent**,
> I want to submit a premium calculation request,
> so that I can obtain the annual and monthly premium for a product.

*Acceptance criteria summary:* The request must be accepted, inputs validated, the applicable rate retrieved, and both annual and monthly premiums calculated and returned. A calculation record must be saved upon completion.

---

**US-02 — Receive Validated Calculation Inputs**

> As an **Agent**,
> I want my calculation inputs to be validated before any premium is computed,
> so that I receive clear feedback if my inputs are outside acceptable bounds.

*Acceptance criteria summary:* If age is out of range, the request is rejected with an age-out-of-range rejection. If the insured amount is out of range, the request is rejected with an insured-amount-out-of-range rejection. If the payment period is invalid, the request is rejected with an invalid-payment-period rejection. Only fully valid inputs proceed to rate retrieval and calculation.

---

**US-03 — Retrieve a Rate for Calculation**

> As the **Premium Calculation process**,
> I want to retrieve the applicable rate from the rate cache,
> so that the premium can be computed without unnecessary delay.

*Acceptance criteria summary:* If the rate is found in the cache, it is used immediately. If the rate is not found in the cache (cache miss), the rate is loaded from the rate source and the cache is written. If no rate can be found at all, the calculation request is rejected with a rate-not-found rejection.

---

**US-04 — Save and Return a Calculation Result**

> As an **Agent**,
> I want the calculation result to be saved and returned to me,
> so that I have a record of the trial calculation and can share it with the customer.

*Acceptance criteria summary:* After both annual and monthly premiums are calculated, a calculation record is saved and the result is returned to the agent. The premium calculation is then marked as completed.

---

### Epic 2 — Calculation History

---

**US-05 — Query Calculation History** *(Board: 推論 — inferred)*

> As an **Agent**,
> I want to query my past calculation records,
> so that I can review or reuse previous trial calculations.

*Acceptance criteria summary:* The agent must be authenticated. The query conditions must be validated. If records are found, they are returned. If the retrieval fails, the agent is informed of the failure.

---

**US-06 — Be Rejected When Accessing Another Agent's Records** *(Board: 推論 — inferred)*

> As an **Agent**,
> I want the system to prevent me from accessing calculation records that do not belong to me,
> so that customer and colleague data is protected.

*Acceptance criteria summary:* Any attempt by an agent to access records belonging to another party results in an unauthorised-access rejection.

---

### Epic 3 — Rate Table & Product Management

---

**US-07 — Register a Product** *(Board: 待定 — to be confirmed)*

> As a **\[Role TBC — see OQ-01\]**,
> I want to register a product,
> so that rate tables can subsequently be associated with it.

---

**US-08 — Register a Rate Table** *(Board: 待定 — to be confirmed)*

> As a **\[Role TBC — see OQ-01\]**,
> I want to register a rate table for a product,
> so that agents can perform premium calculations for that product.

---

**US-09 — Add a New Rate Table Version**

> As a **\[Role TBC — see OQ-01\]**,
> I want to add a new version to an existing rate table,
> so that updated rates take effect for future calculations without removing historical records.

---

**US-10 — Register Rate Entries** *(Board: 推論 — inferred)*

> As a **\[Role TBC — see OQ-01\]**,
> I want to register individual rate entries within a rate table version,
> so that the correct rate can be retrieved for any valid combination of calculation inputs.

---

### Epic 4 — Agent Authentication

---

**US-11 — Authenticate as an Agent** *(Board: 待定 — to be confirmed)*

> As an **Agent**,
> I want my identity to be verified before I can perform any calculation or query,
> so that only authorised agents can access the system.

*Acceptance criteria summary:* Successful authentication allows the agent to proceed. Failed authentication blocks all subsequent actions.

---

## Business Rules

> Rules are derived only from rejection events, policy stickies, and aggregate boundaries visible on the board. Where the board is silent on a threshold or condition, an open question is raised.

---

### BR-01 — Age Range Validation

A premium calculation request **must be rejected** if the age of the insured falls outside the permitted range.

- Rejection event: **Age Out of Range Rejected**
- **Maximum age: 70 years old** *(confirmed by stakeholder)*
- ⚠️ *The minimum permitted age has not been confirmed. See OQ-02.*
- **Error response on rejection:**
  - HTTP Status: **400 Bad Request**
  - Error Code: **`AGE_OUT_OF_RANGE`**
  - Error Message: **`"投保年齡超出承保範圍，最高承保年齡為 70 歲。"`**
    *(English: "The insured's age exceeds the permitted range. The maximum insurable age is 70.")*

---

### BR-02 — Insured Amount Range Validation

A premium calculation request **must be rejected** if the insured amount falls outside the permitted range.

- Rejection event: **Insured Amount Out of Range Rejected**
- ⚠️ *The board does not state the minimum or maximum insured amount. See OQ-03.*

---

### BR-03 — Payment Period Validity

A premium calculation request **must be rejected** if the payment period is not a legally or contractually valid arrangement for the product.

- Rejection event: **Invalid Payment Period Rejected**
- ⚠️ *The board does not enumerate valid payment periods. See OQ-04.*

---

### BR-04 — Rate Must Exist Before Calculation

A premium calculation **cannot proceed** if no applicable rate can be found for the given inputs.

- Rejection event: **Rate Not Found**
- The system must attempt to retrieve the rate from the rate cache first. On a cache miss, the rate is loaded from the rate source and the cache is written before the calculation continues.

---

### BR-05 — Rate Cache Expiry

A rate cache entry that has expired **must not** be used for a calculation. An expired cache entry triggers a cache miss, causing the rate to be reloaded.

- ⚠️ *The board does not state the cache expiry duration or the trigger for expiry. See OQ-05.*

---

### BR-06 — Input Validation Before Rate Retrieval

Calculation inputs (age, insured amount, payment period) **must be validated** before any rate retrieval is attempted. A request that fails validation must be rejected immediately without proceeding to rate lookup or calculation.

- Supporting event: **Calculation Input Validated** precedes **Rate Retrieved** in the event flow.

---

### BR-07 — Calculation Record Must Be Saved

A calculation record **must be saved** after both the annual and monthly premiums have been calculated and before the result is returned to the agent.

- Supporting events: **Calculation Record Saved** → **Calculation Result Returned**.

---

### BR-08 — Agent Authentication Required

An agent **must be authenticated** before performing a premium calculation or querying calculation history.

- Rejection event: **Agent Authentication Failed** / **Unauthorised Access Rejected**
- ⚠️ *The board marks Agent Authentication as 待定 (to be confirmed). See OQ-06.*

---

### BR-09 — Access Restricted to Own Records

An agent **must not** be permitted to retrieve calculation records belonging to another party.

- Rejection event: **Unauthorised Access Rejected**
- ⚠️ *The board does not define how record ownership is established. See OQ-07.*

---

### BR-10 — Rate Table Must Be Registered Before Use

A rate must not be retrievable for a product unless a rate table has been registered for that product and at least one rate table version with rate entries exists.

- Supporting events: **Product Registered** → **Rate Table Registered** → **Rate Table Version Added** → **Rate Entries Registered**
- ⚠️ *Product Registered and Rate Table Registered are marked 待定. See OQ-08.*

---

### BR-11 — Both Annual and Monthly Premiums Must Be Calculated

A premium calculation is only considered complete when **both** the annual premium and the monthly premium have been calculated.

- Supporting events: **Annual Premium Calculated** and **Monthly Premium Calculated** both appear before **Calculation Record Saved**.
- ⚠️ *The board does not state the relationship between annual and monthly premium (e.g., monthly = annual ÷ 12 with loading). See OQ-09.*

---

## Open Questions

> Raised wherever the board is silent, ambiguous, marked 待定, or marked 推論.

| # | Question | Originating Board Element | Impact |
|---|---|---|---|
| OQ-01 | Who is the actor that registers products, rate tables, rate table versions, and rate entries? The board does not show an actor sticky for these commands. | US-07, US-08, US-09, US-10 | Actor definition; access control scope |
| OQ-02 | ~~What are the minimum and maximum permitted ages for a calculation request?~~ The maximum age has been confirmed as **70 years old**. What is the **minimum** permitted age? | BR-01 / AgeOutOfRangeRejected | Validation rule threshold (lower bound only) |
| OQ-03 | What are the minimum and maximum permitted insured amounts? | BR-02 / InsuredAmountOutOfRangeRejected | Validation rule threshold |
| OQ-04 | What payment period values are considered valid? Is validity per-product or universal? | BR-03 / InvalidPaymentPeriodRejected | Validation rule enumeration |
| OQ-05 | What is the expiry duration of a rate cache entry? What event or condition triggers expiry? | BR-05 / RateCacheExpired | Cache lifecycle policy |
| OQ-06 | Agent authentication is marked 待定. Is authentication a prerequisite for every action in the system, or only for history queries? Does authentication rely on an external identity system? | BR-08 / AgentAuthenticated | Scope of authentication; external dependency |
| OQ-07 | How is ownership of a calculation record established? Is it by the agent who submitted the request? | BR-09 / UnauthorizedAccessRejected | Access control rule |
| OQ-08 | Product registration and rate table registration are marked 待定. Are these performed inside this system or managed by an external system? | BR-10 / ProductRegistered, RateTableRegistered | Bounded context boundary; external system dependency |
| OQ-09 | What is the business relationship between the annual premium and the monthly premium? Are they calculated independently from the rate, or is one derived from the other (e.g., with a payment loading factor)? | BR-11 / AnnualPremiumCalculated, MonthlyPremiumCalculated | Calculation rule |
| OQ-10 | The event flow on the board places RateNotFound *before* CalculationInputValidated. Is this the intended business sequence, or is it a board layout artefact? (Logically, validation would precede rate lookup per BR-06.) | Event flow timeline | Process sequence; potential contradiction |
| OQ-11 | What information is included in a Calculation History Query? Can an agent filter by product, date range, or other criteria? | US-05 / CalculationHistoryQueryRequested | Query scope and conditions |
| OQ-12 | What constitutes a Calculation History Retrieval failure? Is it a system unavailability, or can it also be a business condition (e.g., no records found)? | CalculationHistoryRetrievalFailed | Failure classification; user-facing message |
| OQ-13 | PremiumCalculationCompleted is marked as inferred (推論). Is this a distinct business milestone (e.g., triggering a downstream notification or report), or is it simply equivalent to CalculationResultReturned? | PremiumCalculationCompleted | Event necessity; downstream policy triggers |
| OQ-14 | Is there a policy that automatically invalidates or rewrites the rate cache when a new rate table version is added? The board shows RateTableVersionAdded but no explicit cache-invalidation policy sticky. | RateTableVersionAdded / RateCacheExpired | Cache consistency rule |

---

## Business-Level Gherkin (BDD Scenarios)

> Written in the business language of the stickies. Scenarios marked ⚠️ contain placeholder values pending resolution of open questions.

---

### Feature: Premium Calculation

---

```gherkin
Feature: Premium Calculation
  As an Agent
  I want to submit a premium calculation request
  So that I can obtain the annual and monthly premium for a product

  Background:
    Given the Agent has been authenticated
    And a Rate Table with at least one Rate Table Version and Rate Entries
      has been registered for the Product

  # ── Happy Path ──────────────────────────────────────────────────────────────

  Scenario: Successful premium calculation with rate cache hit
    Given the Agent submits a Premium Calculation Request
      with an Age within the permitted range
      and an Insured Amount within the permitted range
      and a valid Payment Period
    When the Calculation Input is validated
    And the applicable Rate is found in the Rate Cache
    Then the Annual Premium is calculated
    And the Monthly Premium is calculated
    And the Calculation Record is saved
    And the Calculation Result is returned to the Agent
    And the Premium Calculation is completed

  Scenario: Successful premium calculation with rate cache miss
    Given the Agent submits a Premium Calculation Request
      with an Age within the permitted range
      and an Insured Amount within the permitted range
      and a valid Payment Period
    When the Calculation Input is validated
    And the applicable Rate is NOT found in the Rate Cache
    And the Rate is loaded from the rate source
    And the Rate Cache is written with the loaded Rate
    Then the Annual Premium is calculated
    And the Monthly Premium is calculated
    And the Calculation Record is saved
    And the Calculation Result is returned to the Agent
    And the Premium Calculation is completed

  # ── Rejection: Rate Not Found ────────────────────────────────────────────────

  Scenario: Calculation request rejected when no applicable rate exists
    Given the Agent submits a Premium Calculation Request
      with an Age within the permitted range
      and an Insured Amount within the permitted range
      and a valid Payment Period
    When the Calculation Input is validated
    And no applicable Rate can be found for the given inputs
    Then the Premium Calculation Request is rejected with Rate Not Found
    And no Calculation Record is saved
    And no premium is returned to the Agent

  # ── Rejection: Age Out of Range ──────────────────────────────────────────────

  Scenario: Calculation request rejected when age exceeds maximum of 70
    Given the Agent submits a Premium Calculation Request
      with an Age greater than 70                          # BR-01: max age = 70 (confirmed)
      and an Insured Amount within the permitted range
      and a valid Payment Period
    When the Calculation Input is validated
    Then the Premium Calculation Request is rejected with Age Out of Range
    And the response status is 400 Bad Request
    And the error code is "AGE_OUT_OF_RANGE"
    And the error message is "投保年齡超出承保範圍，最高承保年齡為 70 歲。"
    And no Rate retrieval is attempted
    And no Calculation Record is saved

  Scenario: Calculation request rejected when age is below minimum
    Given the Agent submits a Premium Calculation Request
      with an Age below the permitted minimum             # ⚠️ OQ-02: minimum age TBC
      and an Insured Amount within the permitted range
      and a valid Payment Period
    When the Calculation Input is validated
    Then the Premium Calculation Request is rejected with Age Out of Range
    And the response status is 400 Bad Request
    And the error code is "AGE_OUT_OF_RANGE"
    And no Rate retrieval is attempted
    And no Calculation Record is saved

  # ── Rejection: Insured Amount Out of Range ───────────────────────────────────

  Scenario: Calculation request rejected when insured amount is out of range
    Given the Agent submits a Premium Calculation Request
      with an Age within the permitted range
      and an Insured Amount outside the permitted range  # ⚠️ OQ-03: thresholds TBC
      and a valid Payment Period
    When the Calculation Input is validated
    Then the Premium Calculation Request is rejected with Insured Amount Out of Range
    And no Rate retrieval is attempted
    And no Calculation Record is saved

  # ── Rejection: Invalid Payment Period ───────────────────────────────────────

  Scenario: Calculation request rejected when payment period is invalid
    Given the Agent submits a Premium Calculation Request
      with an Age within the permitted range
      and an Insured Amount within the permitted range
      and an invalid Payment Period  # ⚠️ OQ-04: valid values TBC
    When the Calculation Input is validated
    Then the Premium Calculation Request is rejected with Invalid Payment Period
    And no Rate retrieval is attempted
    And no Calculation Record is saved
```

---

### Feature: Rate Cache Lifecycle

---

```gherkin
Feature: Rate Cache Lifecycle
  So that premium calculations are served efficiently
  The system must maintain a valid Rate Cache

  Scenario: Rate Cache is written after a cache miss
    Given a Premium Calculation Request is in progress
    And the applicable Rate is NOT found in the Rate Cache
    When the Rate is loaded from the rate source
    Then the Rate Cache is written with the loaded Rate
    And the Rate is marked as retrieved
    And the calculation continues

  Scenario: Expired Rate Cache entry is not used for calculation
    Given a Rate Cache entry for a Product exists
    And that Rate Cache entry has expired
    When a Premium Calculation Request is submitted for that Product
    Then the Rate Cache is treated as a cache miss
    And the Rate is reloaded from the rate source
    And the Rate Cache is written with the reloaded Rate
    # ⚠️ OQ-05: expiry duration and trigger TBC
    # ⚠️ OQ-14: confirm whether adding a Rate Table Version triggers cache expiry
```

---

### Feature: Calculation History Query

---

```gherkin
Feature: Calculation History Query
  As an Agent
  I want to query my past Calculation Records
  So that I can review previous trial calculations

  # ⚠️ OQ-06: Authentication scope to be confirmed
  # ⚠️ OQ-11: Query filter criteria to be confirmed

  Scenario: Agent successfully retrieves their own calculation history
    Given the Agent has been authenticated
    And the Agent submits a Calculation History Query
      with valid query conditions  # ⚠️ OQ-11: conditions TBC
    When the query conditions are validated
    And Calculation Records belonging to the Agent are found
    Then the Calculation Records are returned to the Agent

  Scenario: Calculation history retrieval fails
    Given the Agent has been authenticated
    And the Agent submits a Calculation History Query
      with valid query conditions
    When the query conditions are validated
    And the Calculation History retrieval fails
    Then the Agent is informed that the retrieval has failed
    # ⚠️ OQ-12: failure classification TBC

  Scenario: Agent is rejected when attempting to access another party's records
    Given the Agent has been authenticated
    And the Agent submits a Calculation History Query
      for records that do not belong to the Agent  # ⚠️ OQ-07: ownership definition TBC
    Then the request is rejected with Unauthorised Access
    And no Calculation Records are returned
```

---

### Feature: Agent Authentication

---

```gherkin
Feature: Agent Authentication
  # ⚠️ OQ-06: This feature is marked 待定 (to be confirmed) on the board

  Scenario: Agent is successfully authenticated
    Given an Agent presents their credentials
    When the identity verification is performed
    Then the Agent identity is confirmed as authenticated
    And the Agent may proceed with permitted actions

  Scenario: Agent authentication fails
    Given an Agent presents their credentials
    When the identity verification is performed
    And the credentials cannot be verified
    Then the Agent Authentication fails
    And the Agent is not permitted to proceed
```

---

### Feature: Rate Table & Product Management

---

```gherkin
Feature: Rate Table and Product Management
  # ⚠️ OQ-01: The actor for all scenarios in this feature is not identified on the board
  # ⚠️ OQ-08: It is unclear whether Product and Rate Table registration
  #           occur within this system or via an external system

  Scenario: A Product is registered
    Given an authorised actor  # ⚠️ OQ-01: actor TBC
    When the actor registers a Product
    Then the Product is recorded
    And a Rate Table may subsequently be registered for that Product

  Scenario: A Rate Table is registered for a Product
    Given a Product has been registered
    And an authorised actor  # ⚠️ OQ-01: actor TBC
    When the actor registers a Rate Table for the Product
    Then the Rate Table is recorded for that Product

  Scenario: A new Rate Table Version is added
    Given a Rate Table has been registered for a Product
    And an authorised actor  # ⚠️ OQ-01: actor TBC
    When the actor adds a new Rate Table Version
    Then the new Rate Table Version is recorded
    And Rate Entries may be registered against the new version
    # ⚠️ OQ-14: confirm whether this triggers Rate Cache expiry

  Scenario: Rate Entries are registered for a Rate Table Version
    Given a Rate Table Version exists for a Product
    And an authorised actor  # ⚠️ OQ-01: actor TBC
    When the actor registers Rate Entries for that Rate Table Version
    Then the Rate Entries are recorded
    And the Rate is available for retrieval during premium calculations
```

---

*End of Document*

---

> **Next Steps for Stakeholders**
> 1. Review and resolve all 14 Open Questions before sprint planning.
> 2. Confirm or reclassify all events marked 待定 (to be confirmed) and 推論 (inferred).
> 3. Clarify the event sequence anomaly raised in OQ-10 (RateNotFound appearing before CalculationInputValidated in the timeline).
> 4. Identify the missing actor for Rate Table and Product management (OQ-01).
> 5. ✅ BR-01 maximum age confirmed as 70 years old (v0.2). Minimum age (OQ-02) still pending.

## 事件風暴分析（Event Storming）
> 來源：photo · 便利貼 27 張 · 辨識出 **2 個 bounded context**、22 個 domain event（顏色語彙依 eventstorming.com：橘=Event、藍=Command、黃=Actor/Aggregate、粉=External、紫=Policy、綠=Read Model、紅=Hotspot）

### Bounded Context：費率快取已寫入
RateCacheWritten
| 元素 | 內容 |
|------|------|
| Domain Events（橘） | 試算請求已拒出
PremiumCalculationRequested、對應費率未查得
RateNotFound
(404)、試算輸入已驗證通過
CalculationInputValidated、年繳保費已計算
AnnualPremiumCalculated、年齡超出範圍已拒絕
AgeOutOfRangeRejected
(400)、月繳保費已計算
MonthlyPremiumCalculated、明細超出範圍已拒絕
InsuredAmountOutOfRangeRejected
(400)、試算紀錄已保存
CalculationRecordSaved、被保投保不合法已拒絕
InvalidPaymentPeriodRejected
(400)、試算結果已回傳
CalculationResultReturned、費率已查得
RateRetrieved、保費試算已完成（推論）
PremiumCalculationCompleted |
| Aggregates（長黃） | 費率快取已寫入
RateCacheWritten、費率快取已逾期失效（推論）
RateCacheExpired |

### Bounded Context：費率快取已命中
RateCacheHit
| 元素 | 內容 |
|------|------|
| Domain Events（橘） | 業務員身分已驗證（待定）
AgentAuthenticated、商品已建檔（待定）
ProductRegistered、試算紀錄查詢條件已驗證（推論）
CalculationHistoryQueryRequested、費率表已建檔（待定）
RateTableRegistered、試算紀錄已查得
CalculationRecordsRetrieved、費率明細已登錄（推論）
RateEntriesRegistered、試算紀錄查詢失敗（推論）
CalculationHistoryRetrievalService
FAILED、費率表版本已新增
RateTableVersionAdded、業務員身分驗證已失敗（推論）
AgentAuthenticationFailed、非本人未授權已拒絕（推論）
UnauthorizedAccessRejected |
| Aggregates（長黃） | 費率快取已命中
RateCacheHit、費率快取未命中
RateCacheMissed、費率已從資料庫載入
RateEntryLoadedFromDatabase |

### 事件流（時間軸）
`試算請求已拒出
PremiumCalculationRequested` → `對應費率未查得
RateNotFound
(404)` → `試算輸入已驗證通過
CalculationInputValidated` → `年繳保費已計算
AnnualPremiumCalculated` → `年齡超出範圍已拒絕
AgeOutOfRangeRejected
(400)` → `月繳保費已計算
MonthlyPremiumCalculated` → `明細超出範圍已拒絕
InsuredAmountOutOfRangeRejected
(400)` → `試算紀錄已保存
CalculationRecordSaved` → `被保投保不合法已拒絕
InvalidPaymentPeriodRejected
(400)` → `試算結果已回傳
CalculationResultReturned` → `費率已查得
RateRetrieved` → `保費試算已完成（推論）
PremiumCalculationCompleted` → `業務員身分已驗證（待定）
AgentAuthenticated` → `商品已建檔（待定）
ProductRegistered` → `試算紀錄查詢條件已驗證（推論）
CalculationHistoryQueryRequested` → `費率表已建檔（待定）
RateTableRegistered` → `試算紀錄已查得
CalculationRecordsRetrieved` → `費率明細已登錄（推論）
RateEntriesRegistered` → `試算紀錄查詢失敗（推論）
CalculationHistoryRetrievalService
FAILED` → `費率表版本已新增
RateTableVersionAdded` → `業務員身分驗證已失敗（推論）
AgentAuthenticationFailed` → `非本人未授權已拒絕（推論）
UnauthorizedAccessRejected`
