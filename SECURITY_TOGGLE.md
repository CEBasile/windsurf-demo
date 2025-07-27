# Security Toggle Configuration

This document explains how to enable/disable security in the ticket application using the `application.yml` configuration.

## Configuration

### Enable Security (Default)
```yaml
spring:
  security:
    enabled: true  # or omit this line entirely (defaults to true)
```

### Disable Security (Development Mode)
```yaml
spring:
security:
    enabled: false
```

## Environment Variable Override

You can also control security using the `SECURITY_ENABLED` environment variable:

```bash
# Enable security
export SECURITY_ENABLED=true

# Disable security
export SECURITY_ENABLED=false
```

## Behavior When Security is Disabled

When `spring.security.enabled` is set to `false`:

1. **Authentication**: All API endpoints become accessible without authentication
2. **User Context**: `UserService` provides default values:
   - `getCurrentUserSid()` returns `"default-user"`
   - `getCurrentUserRoles()` returns `["ADMIN"]` for full access
   - `isAuthenticated()` always returns `true`
3. **Authorization**: All role-based access controls are bypassed

## Use Cases

### Development Environment
```yaml
spring:
  security:
    enabled: false
```
Perfect for local development when you don't want to deal with JWT tokens.

### Testing Environment
```yaml
spring:
  security:
    enabled: false
```
Useful for integration tests that focus on business logic rather than security.

### Production Environment
```yaml
spring:
  security:
    enabled: true  # Always keep security enabled in production
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://your-oidc-provider.com/.well-known/jwks.json
          issuer-uri: https://your-oidc-provider.com
```

## Testing the Configuration

Run the security toggle tests to verify the configuration works:

```bash
mvn test -Dtest=SecurityToggleTest*
```

## Security Considerations

⚠️ **WARNING**: Never disable security in production environments. This feature is intended for development and testing purposes only.

When security is disabled:
- All API endpoints are publicly accessible
- No user authentication or authorization is performed
- All operations are performed as a default admin user
