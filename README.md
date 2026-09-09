# OAuth2 Resource Server

A stateless Spring Boot resource server that protects REST API endpoints by validating JWTs issued by an external identity provider (Auth0).

It does not handle login itself, authentication is delegated to Auth0, which issues signed tokens. This service validates those tokens (signature, issuer, expiry) and enforces scope-based access on protected endpoints.

## How it works

On startup, the service uses the configured issuer URL to discover Auth0's OIDC metadata and fetch its public keys (JWKS). On each request, it verifies the incoming token's signature against those cached keys and checks the issuer and expiry. Valid tokens then pass through method-level authorization, which requires the correct scope. Unauthenticated requests are rejected with 401; authenticated requests lacking the required scope are rejected with 403.

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
| `GET` | `/actuator/health`, `/actuator/metrics` | Requires valid token |

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

Integration tests (JUnit + MockMvc) cover authenticated, unauthenticated, and insufficient-scope requests, reaching 95% instruction / 100% branch coverage.

## Performance

Instrumented with Spring Boot Actuator and Micrometer. Because JWKS public keys are cached at startup and validation happens locally (no per-request call back to Auth0), measured average endpoint latency was ~2ms locally after JVM warmup.