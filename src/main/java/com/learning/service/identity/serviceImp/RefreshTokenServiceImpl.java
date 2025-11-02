package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.RefreshToken;
import com.learning.dbentity.identity.User;
import com.learning.repository.identity.RefreshTokenRepository;
import com.learning.repository.identity.UserRepository;
import com.learning.service.identity.RefreshTokenService;
import com.learning.utility.identity.AuthenticationUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();
        Optional<User> user= userRepository.findById(userId);
        if(user.isEmpty()) throw new UserNotFoundException(userId.toString());
        refreshToken.setUser(user.get());
        String token = AuthenticationUtility.generateRefreshToken();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken findToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if(refreshToken.isEmpty())throw new IdentityAppExcpetion("Refresh token not found", NOT_FOUND);
        return refreshToken.get();
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new IdentityAppExcpetion("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
        }
        if(token.getRevoked()){
            throw new IdentityAppExcpetion("Refresh token has been revoked", HttpStatus.FORBIDDEN);
        }
        return token;
    }

    @Transactional
    @Override
    public void revokeAllUserTokens(User user){
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    @Override
    @Transactional
    public void deleteByToken(String token){
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    @Override
    public void deleteByAllExpiredToken(){
        refreshTokenRepository.deleteAllExpiredToken();
    }
}
