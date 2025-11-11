package com.learning.service.identity.serviceImp;

import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.RefreshToken;
import com.learning.dbentity.identity.User;
import com.learning.enums.OAuth2ProviderEnum;
import com.learning.repository.identity.UserRepository;
import com.learning.responseDTO.JwtResponse;
import com.learning.security.JwtUtils;
import com.learning.security.UserPrincipal;
import com.learning.service.identity.OAuthService;
import com.learning.service.identity.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class oauth2ServiceImpl implements OAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;



    /**
     * Merge social account with existing local account
     */
    @Override
    public JwtResponse mergeAccount(Long localUserId, OAuth2ProviderEnum provider, String providerId, String email) {
        User user = userRepository.findById(localUserId).orElseThrow(() -> new UserNotFoundException(localUserId));
        if(!user.getEmail().equals(email)){
            throw new RuntimeException("Email does not match local account");
        }
        // Update user with social provider information
        user.setProviderId(providerId);
        user.setProvider(provider);
        userRepository.save(user);
        return generateJwtResponse(UserPrincipal.build(user));
    }

    private JwtResponse generateJwtResponse( UserPrincipal userPrincipal){
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        String jwtToken = jwtUtils.generateJwtToken(userPrincipal);
        return JwtResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .id(userPrincipal.getId())
                .userName(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .type("Bearer")
                .build();
    }


}
