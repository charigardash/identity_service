package com.learning.service.social;

import com.learning.security.JwtUtils;
import com.learning.security.UserPrincipal;
import com.learning.service.identity.OAuthService;
import com.learning.service.identity.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private OAuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String jwt = jwtUtils.generateTokenFromUsername(oAuth2User.getName());
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        authService.mergeAccount(principal.getId(), principal.getProvider(), principal.getProviderId(), principal.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(principal.getId()).getToken();
        // Determine target URL (frontend URL)
        String targetUrl = determineTargetUrl(request, response);
        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("accessToken", jwt)
                .queryParam("refreshToken", refreshToken)
                .queryParam("provider", getProviderFromAuthentication(authentication))
                .build().toString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getProviderFromAuthentication(Authentication authentication) {
        if(authentication.getPrincipal() instanceof UserPrincipal){
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getProvider().getOauth2Provide();
        }
        return "unknown";
    }

    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response){
        return "http://localhost:3000/oauth2/redirect";
    }

}
