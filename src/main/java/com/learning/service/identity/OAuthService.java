package com.learning.service.identity;

import com.learning.enums.OAuth2ProviderEnum;
import com.learning.responseDTO.JwtResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface OAuthService {

    /**
     * Merge social account with existing local account
     */
    @Transactional
    JwtResponse mergeAccount(Long localUserId, OAuth2ProviderEnum provider, String providerId, String email);

}
