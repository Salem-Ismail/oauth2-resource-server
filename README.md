# Fine-Grained API Authorization Service

A stateless Spring Boot resource server that protects REST API endpoints with layered authorization: JWT validation, scope-based access control, and fine-grained ownership checks enforced by Amazon Verified Permissions (Cedar).

It does not handle login itself. Authentication is delegated to Auth0, which issues signed tokens. This service validates those tokens, enforces that the caller has the required scope, and then checks whether the caller actually owns the resource they are requesting, blocking cross-user access (IDOR).

## How it works

The service applies three layers of security, in order:

1. **Authentication.** On startup it uses the configured issuer URL to discover Auth0's OIDC metadata and fetch its public keys (JWKS), which it caches. On each request it verifies the token's signature against those cached keys and checks the issuer and expiry, so validation happens locally without a per-request call to Auth0.
2. **Scope authorization.** Method-level security requires the token to carry the correct scope (`read:status`).
3. **Ownership authorization.** The service asks Amazon Verified Permissions whether the caller may access the specific resource. AVP evaluates a Cedar policy (a user may read a status only if they own it) and returns allow or deny. The service enforces that verdict and logs every decision for auditing.

Request outcomes:

- Unauthenticated request: **401**
- Valid token, missing the required scope: **403**
- Valid token, correct scope, but accessing another user's resource: **403**
- Valid token, correct scope, own resource: **200**

This split follows the policy decision point / policy enforcement point (PDP/PEP) pattern: AVP decides, the service enforces.

## Tech stack

- Java 25, Spring Boot 4, Spring Security 7
- OAuth2 Resource Server (JWT validation via JWKS)
- Amazon Verified Permissions + Cedar (fine-grained authorization)
- AWS SDK for Java v2
- Spring Boot Actuator + Micrometer (metrics)
- JUnit 5 + MockMvc + Mockito (testing)
- Gradle

## Endpoints

| Method | Path | Access |
|--------|------|--------|
| `GET` | `/public/health` | Public |
| `GET` | `/users/{userId}/status/{code}` | Valid token + `read:status` scope + ownership of the requested user |
| `GET` | `/actuator/health`, `/actuator/metrics` | Requires authentication |

## Authorization model

The caller's identity travels in the token as an `acting_user` claim. On a request to `/users/{userId}/status/{code}`, the service sends AVP a decision query:

- **principal:** the acting user
- **action:** `readStatus`
- **resource:** the requested user's status, with its `owner` attribute

The Cedar policy permits the action only when `resource.owner == principal`. Every decision is logged as a structured audit event (principal, resource, verdict); running on AWS these logs ship to CloudWatch via the standard logging pipeline.

## Running locally

Set your Auth0 issuer and AVP policy store ID in `src/main/resources/application.properties`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.auth0.com/
avp.policy-store-id=YOUR_POLICY_STORE_ID
```

AWS credentials are resolved from the standard credential chain (for example `~/.aws/credentials`), never from the codebase. Then run:

```bash
./gradlew bootRun
```

## Testing

```bash
./gradlew test
```

Integration tests use JUnit 5 and MockMvc to drive requests through the full Spring security pipeline. They cover:

- **Status-code logic:** each branch of the status mapping (`A` to ACTIVE, `L` to LOCKED, `S` to SUSPENDED, other to UNKNOWN)
- **Access control:** correct scope (200), wrong scope (403), and no token (401)
- **Ownership:** a caller accessing their own resource (200) and another user's resource (403)
- **Public access:** `/public/health` without authentication

The AVP integration is mocked at its service boundary with Mockito, so tests run with no network or AWS credentials (verified by running the suite with credentials removed). This also keeps coverage focused on the application's own logic rather than the third-party SDK wrapper.

Current coverage: **96% instruction / 100% branch** (excluding the mocked AVP wrapper).

## Performance

Instrumented with Spring Boot Actuator and Micrometer. JWT validation runs locally against cached JWKS keys, so the authentication path adds about 2ms. The ownership check adds a network call to Amazon Verified Permissions, bringing average protected-endpoint latency to about 23ms (measured locally, against AVP in ca-central-1). This reflects the deliberate tradeoff of externalized authorization: a network round-trip in exchange for centralized, auditable policy. These are local reference points, not production benchmarks.
