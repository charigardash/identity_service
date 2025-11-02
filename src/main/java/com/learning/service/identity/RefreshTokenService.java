package com.learning.service.identity;

import com.learning.dbentity.identity.RefreshToken;
import com.learning.dbentity.identity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface RefreshTokenService {
    RefreshToken createRefreshToken(Long userId);
    RefreshToken findToken(String token);
    RefreshToken verifyExpiration(RefreshToken token);

    @Transactional
    void revokeAllUserTokens(User user);

    @Transactional
    void deleteByToken(String refreshToken);

    @Transactional
    void deleteByAllExpiredToken();
}
