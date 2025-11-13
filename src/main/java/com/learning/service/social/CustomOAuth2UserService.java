package com.learning.service.social;

import com.learning.dbentity.identity.Role;
import com.learning.dbentity.identity.User;
import com.learning.enums.OAuth2ProviderEnum;
import com.learning.enums.RolesEnum;
import com.learning.factory.OAuth2UserInfoFactory;
import com.learning.factory.userInfo.OAuth2UserInfo;
import com.learning.repository.identity.RoleRepository;
import com.learning.repository.identity.UserRepository;
import com.learning.security.UserPrincipal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try{
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex){
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    @Transactional
    public OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Get provider information
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        String accessToken = oAuth2UserRequest.getAccessToken().getTokenValue();

        //Extract user attributes based on provider
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes(), accessToken);

        if(!StringUtils.hasText(oAuth2UserInfo.getEmail())){
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user already exists
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());

        User user;

        if(userOptional.isPresent()){
            user = userOptional.get();
            if(!user.getProvider().getOauth2Provide().equals(registrationId.toUpperCase())){
                // User exists with different provider
                throw new OAuth2AuthenticationException(
                        "You're signed up with " + user.getProvider() + " account. " +
                                "Please use your " + user.getProvider() + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        }else{
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserPrincipal.create(user, oAuth2UserInfo.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setProvider(OAuth2ProviderEnum.getEnumFromProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setUserName(generateUsername(oAuth2UserInfo));
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setEmailVerified(true);
        user.setEnabled(true);
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName(RolesEnum.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    private @NotBlank @Size(max = 50) String generateUsername(OAuth2UserInfo oAuth2UserInfo) {
        // Generate a unique username from email or name
        String baseUsername = oAuth2UserInfo.getName().replaceAll("\\s+", "").toLowerCase();
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUserName(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        user.setUserName(generateUsername(oAuth2UserInfo));
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(user);
    }
}
