# Google Authentication Setup Guide

## Overview

This application now supports Google OAuth2 authentication alongside the existing username/password authentication. Users can sign in with their Google account, and the system will automatically create a user profile if one doesn't exist.

## Features Implemented

### 1. **Dual Authentication Support**

- Traditional username/password authentication
- Google OAuth2 authentication
- Automatic user detection and merging

### 2. **Complete Integration**

- OAuth2 users receive the same JWT tokens as regular users
- Refresh tokens are generated for session management
- Role-based access control (RBAC) applies to OAuth2 users
- OAuth2 users get default USER role assigned

### 3. **User Management**

- New users are automatically created on first Google login
- Existing users can link their Google account
- OAuth2 provider tracking (google, future: facebook, github, etc.)
- Users cannot use password login if registered via OAuth2

## Configuration

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google+ API
4. Go to **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
5. Configure OAuth consent screen:
   - Add application name
   - Add authorized domains
   - Add scopes: `email`, `profile`
6. Create OAuth 2.0 Client ID:
   - Application type: **Web application**
   - Authorized redirect URIs:
     - `http://localhost:8089/api/v1/login/oauth2/code/google` (Development)
     - `https://yourdomain.com/api/v1/login/oauth2/code/google` (Production)
7. Copy **Client ID** and **Client Secret**

### 2. Application Configuration

Update `application.properties` with your Google OAuth2 credentials:

```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# OAuth2 Frontend Redirect URL (Update this to your frontend URL)
app.oauth2.authorizedRedirectUri=http://localhost:5173/oauth2/redirect
```

Or (recommended) set environment variables instead of committing secrets:

```bash
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
OAUTH2_REDIRECT_URI=http://localhost:5173/oauth2/redirect
```

### 3. Database Changes

The `users` table now includes two new columns:

- `oauth2_provider` (VARCHAR(20)) - stores the provider name (e.g., 'google')
- `oauth2_provider_id` (VARCHAR(100)) - stores the provider's user ID

These columns will be automatically created by Hibernate when you start the application (ddl-auto=update).

## API Endpoints

### 1. Initiate Google Login

```
GET /api/v1/oauth2/authorization/google
```

Redirects user to Google's OAuth2 login page.

**Frontend Usage:**

```javascript
// Redirect user to Google login
window.location.href =
  "http://localhost:8089/api/v1/oauth2/authorization/google";
```

### 2. OAuth2 Callback (Automatic)

```
GET /api/v1/login/oauth2/code/google
```

This endpoint is called automatically by Google after successful authentication.
Spring Security handles this internally.

### 3. Success Redirect

After successful authentication, the user is redirected to:

```
http://localhost:5173/oauth2/redirect?authData=<encoded_jwt_response>&success=true
```

The `authData` parameter contains a URL-encoded JSON with:

```json
{
  "token": "JWT_ACCESS_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "type": "Bearer",
  "id": 123,
  "username": "user@gmail.com",
  "email": "user@gmail.com",
  "privileges": ["READ_PRIVILEGE", "WRITE_PRIVILEGE"],
  "role": 1
}
```

### 4. Failure Redirect

On authentication failure:

```
http://localhost:5173/oauth2/redirect?error=<error_message>
```

## Frontend Integration

### React/Vue/Angular Example

```javascript
// 1. Redirect to Google Login
function loginWithGoogle() {
  window.location.href =
    "http://localhost:8089/api/v1/oauth2/authorization/google";
}

// 2. Handle OAuth2 Redirect (on your redirect page)
function handleOAuth2Redirect() {
  const urlParams = new URLSearchParams(window.location.search);
  const authDataEncoded = urlParams.get("authData");
  const success = urlParams.get("success");
  const error = urlParams.get("error");

  if (success === "true" && authDataEncoded) {
    // Decode the auth data
    const authData = JSON.parse(decodeURIComponent(authDataEncoded));

    // Store tokens
    localStorage.setItem("accessToken", authData.token);
    localStorage.setItem("refreshToken", authData.refreshToken);
    localStorage.setItem(
      "user",
      JSON.stringify({
        id: authData.id,
        username: authData.username,
        email: authData.email,
        privileges: authData.privileges,
        role: authData.role,
      })
    );

    // Redirect to dashboard
    window.location.href = "/dashboard";
  } else if (error) {
    console.error("OAuth2 authentication failed:", error);
    alert("Login failed: " + error);
    window.location.href = "/login";
  }
}

// 3. Use the tokens for API calls
function makeAuthenticatedRequest(url) {
  const token = localStorage.getItem("accessToken");

  return fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });
}
```

## Authentication Flow

### For New Users (First-time Google Login)

1. User clicks "Sign in with Google"
2. User is redirected to Google login page
3. User authenticates with Google
4. Google redirects back to application with authorization code
5. Application exchanges code for user profile
6. System checks if user exists by email
7. **New user is created** with:
   - Email from Google
   - Username = Email
   - First name from Google profile
   - Status = ACTIVE
   - Type = USER
   - Default USER role assigned
   - OAuth2 provider = "google"
   - Empty password (OAuth2 users don't use passwords)
8. JWT access token + refresh token generated
9. User redirected to frontend with auth data

### For Existing Users

1. User clicks "Sign in with Google"
2. User authenticates with Google
3. System finds existing user by email
4. User's OAuth2 provider info is updated (if not already set)
5. JWT access token + refresh token generated
6. User redirected to frontend with auth data

### For Traditional Login Users Switching to OAuth2

If a user created an account with username/password and later tries Google login:

- System recognizes the email
- Links the Google account to existing user profile
- User can now login using either method

### For OAuth2 Users Trying Traditional Login

If a user registered via Google OAuth2 and tries to login with password:

- System detects OAuth2 registration
- Throws error: "User registered with google. Please use google login."
- User must use Google authentication

## Security Considerations

1. **Email Verification**: Users authenticated via Google have verified emails (Google's responsibility)
2. **Password Security**: OAuth2 users don't have passwords, reducing attack surface
3. **Token Security**: JWT tokens have expiration (configured in application.properties)
4. **Refresh Tokens**: Long-lived tokens for session management
5. **Role-Based Access**: OAuth2 users get default USER role, can be upgraded by admin

## Testing

### 1. Test Google Login

1. Ensure application.properties has valid Google credentials
2. Start the application
3. Navigate to: `http://localhost:8089/api/v1/oauth2/authorization/google`
4. Login with Google account
5. Verify redirect to frontend with auth data

### 2. Test Existing User Flow

1. Create a user manually via regular signup
2. Login with Google using the same email
3. Verify the user is recognized and not duplicated

### 3. Test API Access

1. Complete Google login and get JWT token
2. Make request to protected endpoint:
   ```bash
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
        http://localhost:8089/api/v1/test/user
   ```

## Troubleshooting

### Issue: "redirect_uri_mismatch" error

**Solution**: Ensure the redirect URI in Google Console matches exactly:

- `http://localhost:8089/api/v1/login/oauth2/code/google` (dev)
- Include the correct port and context path

### Issue: User created without role

**Solution**: Ensure a default role exists in database:

```sql
-- Check if default role exists
SELECT * FROM roles WHERE name = 'User' AND type = 'USER';

-- If not, create one
INSERT INTO roles (id, name, type, created_at, updated_at)
VALUES (1, 'User', 'USER', NOW(), NOW());
```

### Issue: Frontend not receiving auth data

**Solution**: Check the `app.oauth2.authorizedRedirectUri` in application.properties matches your frontend redirect page URL.

### Issue: CORS errors

**Solution**: Ensure CORS is properly configured in SecurityConfig for OAuth2 endpoints.

## Production Deployment

### 1. Update Configuration

```properties
# Use environment variables
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
spring.security.oauth2.client.registration.google.redirect-uri=${BASE_URL}/login/oauth2/code/google
app.oauth2.authorizedRedirectUri=${FRONTEND_URL}/oauth2/redirect
```

### 2. Google Console

- Add production domain to authorized domains
- Add production redirect URI to authorized redirect URIs

### 3. HTTPS

- Ensure production uses HTTPS
- Update all URLs to use https://

## Future Enhancements

1. Add support for more OAuth2 providers (Facebook, GitHub, Microsoft)
2. Allow users to link multiple OAuth2 providers
3. Add OAuth2 account unlinking functionality
4. Implement social profile picture sync
5. Add OAuth2 specific user settings

## File Structure

```
src/main/java/com/ffms/resqeats/
├── config/
│   └── SecurityConfig.java (Updated with OAuth2 configuration)
├── controller/
│   └── auth/
│       └── OAuth2Controller.java (OAuth2 endpoints)
├── models/
│   └── usermgt/
│       └── User.java (Added oauth2Provider fields)
├── security/
│   ├── CustomUserDetailsService.java (Updated for OAuth2)
│   └── oauth2/
│       ├── OAuth2UserInfo.java (Interface)
│       ├── GoogleOAuth2UserInfo.java (Google implementation)
│       ├── OAuth2UserInfoFactory.java (Factory pattern)
│       ├── CustomOAuth2UserService.java (OAuth2 user processing)
│       ├── OAuth2AuthenticationSuccessHandler.java (Success handler)
│       └── OAuth2AuthenticationFailureHandler.java (Failure handler)
```

## Support

For issues or questions:

1. Check the application logs
2. Verify Google Console configuration
3. Test with Postman/curl
4. Review this documentation

---

**Last Updated**: January 3, 2026
