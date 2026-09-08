# OAuth2 Resource Server
 
A stateless Spring Boot resource server that protects REST API 

endpoints by validating JWTs issued by an external identity 

provider (Auth0).
 
## How it works

On startup, the app uses the configured issuer URL to discover 

Auth0's endpoints and fetch its public keys (JWKS). On each request, 

it verifies the incoming token's signature against those keys and 

checks the issuer and expiry - valid tokens reach the controller, 

invalid ones are rejected with 401.
 
## Status

- [x] REST endpoints (public + protected-to-be)

- [x] Auth0 setup as identity provider

- [x] JWT/JWKS validation (in progress)

- [x] Role-based access control
 
## Endpoints

- `GET /public/health` - health check (public)

- `GET /users/{userId}/status/{code}` - user status

- `POST /login` - login stub
 
