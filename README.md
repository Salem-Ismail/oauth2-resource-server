# OAuth2 Resource Server

A stateless Spring Boot resource server that protects REST API endpoints by validating JWTs issued by an external identity provider (Auth0).

It does not handle login itself. Authentication is delegated to Auth0, which issues signed tokens. This service validates those tokens (signature, issuer, expiry) and enforces scope-based access on protected endpoints.

## How it works

On startup, the service uses the configured issuer URL to discover Auth0's OIDC metadata and fetch its public keys (JWKS), which it caches. On each request it verifies the token's signature against those cached keys and checks the issuer and expiry, so validation happens locally without a network call to Auth0 per request. Valid tokens then pass through method-level authorization, which requires the correct scope.

- Unauthenticated request: **401**
- Authenticated but missing the required scope: **403**
- Valid token with the required scope: **200**

## Tech stack

- Java 25, Spring Boot 4, Spring Security 7
- OAuth2 Resource Server (JWT validation via JWKS)
- Spring Boot Actuator + Micrometer (metrics)
- JUnit 5 + MockMvc (testing)
- Gradle

## Endpoints

| Method | Path | Access |
|--------|------|--------|
| `GET` | `/public/health` | Public |
| `GET` | `/users/{userId}/status/{code}` | Requires valid token + `read:status` scope |
| `GET` | `/actuator/health`, `/actuator/metrics` | Requires authentication |

## Running locally

Set your Auth0 issuer in `src/main/resources/application.properties`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.auth0.com/
```

Then run:

```bash
./gradlew bootRun
```

## Testing

```bash
./gradlew test
```

Integration tests use JUnit 5 and MockMvc to drive requests through the full Spring security pipeline. They cover:

- **Status-code logic:** each branch of the status mapping (`A` to ACTIVE, `L` to LOCKED, `S` to SUSPENDED, other to UNKNOWN)
- **Access control:** a valid token with the correct scope (200), a valid token with the wrong scope (403), and a request with no token (401)
- **Public access:** `/public/health` is reachable without authentication

Protected-endpoint tests use MockMvc's JWT support to simulate an authenticated request without depending on a live Auth0 tenant, for example `.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status")))`.

Current coverage: **95% instruction / 100% branch.**

## Performance

Instrumented with Spring Boot Actuator and Micrometer. Because JWKS public keys are cached at startup and validation runs locally (no per-request call to Auth0), measured average endpoint latency was about 2ms locally after JVM warmup. This is a local reference point, not a production benchmark.
