package com.learning.service.social;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.User;
import com.learning.enums.OAuth2ProviderEnum;
import com.learning.repository.identity.UserRepository;
import com.learning.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2Service {

    private final UserRepository userRepository;

    public OAuth2Service(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void linkSocialAccount(UserPrincipal userPrincipal, String provider, String providerId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(()-> new UserNotFoundException(userPrincipal.getId()));
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(OAuth2ProviderEnum.getEnumFromEnumString(provider), providerId);
        if(existingUser.isPresent() && existingUser.get().getId().equals(user.getId())){
            throw new IdentityAppExcpetion("Social account already linked to another user", HttpStatus.BAD_REQUEST);
        }
        // Link social account
        user.setProviderId(providerId);
        user.setProvider(OAuth2ProviderEnum.getEnumFromEnumString(provider));
        userRepository.save(user);
    }

    public void unlinkSocialAccount(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(()-> new UserNotFoundException(userPrincipal.getId()));

        if(user.getPassword() == null){
            throw new RuntimeException("Cannot unlink social account. Please set a password first.");
        }

        user.setProvider(OAuth2ProviderEnum.LOCAL);
        user.setProviderId(null);
        userRepository.save(user);
    }

    public Map<String, Object> getSocialConnectionForUser(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(()-> new UserNotFoundException(userPrincipal.getId()));
        Map<String, Object> response = new HashMap<>();
        response.put("currentProvider", user.getProvider());
        response.put("providerId", user.getProviderId());
        response.put("hasPassword", user.getPassword() != null);
        return response;
    }

    public Map<String, Object> checkUserExistsUsingEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", user.isPresent());
        if(user.isPresent()){
            response.put("provider", user.get().getProvider());
            response.put("hasPassword", user.get().getPassword() != null);
        }
        return response;
    }
}
