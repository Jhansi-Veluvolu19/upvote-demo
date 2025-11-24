package com.example.upvote;
// <- change to your package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

@Configuration
public class OAuthUserConfig {

    // inject your AppUserService to create local users (adjust constructor or @Autowired if needed)
    private final AppUserService appUserService;

    public OAuthUserConfig(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * OIDC (Google) user service — creates AppUser record if missing
     */
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        // return a lambda that implements OAuth2UserService<OidcUserRequest, OidcUser>
        return (OidcUserRequest userRequest) -> {

            // delegate to default OIDC service to load the user info from provider
            OidcUser oidcUser = delegate.loadUser(userRequest);

            // prefer email as username
            String email = oidcUser.getEmail();
            if (email == null || email.isBlank()) {
                email = oidcUser.getSubject(); // fallback if email not available
            }

            // create or update local AppUser record (adjust AppUserService API to your code)
            appUserService.createOrUpdateFromOidc(email, oidcUser);

            // return the OidcUser to Spring Security for auth
            return oidcUser;
        };
    }
}
