package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.SignupExceptions;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.RefreshToken;
import com.learning.dbentity.identity.Role;
import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import com.learning.repository.identity.RoleRepository;
import com.learning.repository.identity.User2FARepository;
import com.learning.repository.identity.UserRepository;
import com.learning.requestDTO.LoginRequest;
import com.learning.requestDTO.SignupRequest;
import com.learning.requestDTO.TokenRefreshRequest;
import com.learning.requestDTO.TwoFactorRequest;
import com.learning.responseDTO.JwtResponse;
import com.learning.responseDTO.TwoFactorResponse;
import com.learning.security.JwtUtils;
import com.learning.security.UserPrincipal;
import com.learning.service.identity.AuthService;
import com.learning.service.identity.RefreshTokenService;
import com.learning.service.identity.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.learning.constants.IdentityConstants.adminStringConstant;
import static com.learning.constants.IdentityConstants.moderatorStringConstant;
import static com.learning.enums.RolesEnum.*;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private User2FARepository user2FARepository;

    @Override
    public Object authenticateUser(LoginRequest loginRequest, String deviceId) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new UserNotFoundException(userPrincipal.getId()));

        //check if 2FA enabled for this user
        boolean is2FAEnabled = user2FARepository.findByUser(user).map(User2FA::getEnabled).orElse(false);
        boolean isDeviceTrusted = twoFactorAuthService.isDeviceTrusted(user.getId(), deviceId);
        if(is2FAEnabled && !isDeviceTrusted){
            // Generate and send OTP
            String otp = twoFactorAuthService.generateLoginOtp(user.getId(), deviceId);
            // Generate temporary token for 2FA verification
            String tempToken = jwtUtils.generateJwtToken(authentication);
            return new TwoFactorResponse(true, "Two-factor authentication required. OTP sent to your email.",
                    tempToken, user.getId());
        }else {
            // No 2FA required, proceed with normal login
            return generateJwtResponse(authentication, userPrincipal);
        }
    }

    /**
     * Verify 2FA and complete login
     * @param usedId
     * @param twoFactorRequest
     * @return
     */
    @Override
    public JwtResponse verifyTwoFactor(Long usedId, TwoFactorRequest twoFactorRequest){
        User user = userRepository.findById(usedId).orElseThrow(() -> new UserNotFoundException(usedId));
        boolean isValid = false;
        if(twoFactorAuthService.verifyLoginAttempt(usedId, twoFactorRequest.getCode(), twoFactorRequest.getDeviceId())){
            isValid = true;
        }
        else if(twoFactorAuthService.isDeviceTrusted(usedId, twoFactorRequest.getDeviceId())){
            isValid = true;
        }
        if(!isValid){
            throw new IdentityAppExcpetion("Invalid verification code", HttpStatus.FORBIDDEN);
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUserName(), null, Collections.emptyList());
        return generateJwtResponse(authentication, UserPrincipal.build(user));
    }

    private JwtResponse generateJwtResponse(Authentication authentication, UserPrincipal userPrincipal){
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        String jwtToken = jwtUtils.generateJwtToken(authentication);
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

    @Override
    public JwtResponse refreshJwtToken(TokenRefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        RefreshToken token = refreshTokenService.findToken(refreshToken);
        token = refreshTokenService.verifyExpiration(token);
        User userPrincipal = token.getUser();
        String jwtToken = jwtUtils.generateJwtTokenFromUser(userPrincipal);
        Set<String> roles = userPrincipal.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        return JwtResponse.builder()
                .token(jwtToken)
                .refreshToken(token.getToken())
                .id(userPrincipal.getId())
                .userName(userPrincipal.getUserName())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .type("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logoutUser(TokenRefreshRequest refreshRequest) {
        refreshTokenService.deleteByToken(refreshRequest.getRefreshToken());
    }

    @Override
    @Transactional
    public void logoutAllUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId.toString()));
        refreshTokenService.revokeAllUserTokens(user);
    }


    @Override
    @Transactional
    public String registerUser(SignupRequest signupRequest) {
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            throw new SignupExceptions( "Error: Username is already taken!", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            throw new SignupExceptions("Error: Email is already in use!", HttpStatus.BAD_REQUEST);
        }
        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(), passwordEncoder.encode(signupRequest.getPassword()));
        Set<Role> roles = getRoles(signupRequest.getRoles());
        user.setRoles(roles);
        userRepository.save(user);
        return "User registered successfully!";
    }

    @Override
    public List<User> getAllRegisterUser() {
        return userRepository.findAll();
    }

    private Set<Role> getRoles(Set<String> strRoles){
        Set<Role> roles = new HashSet<>();
        if(strRoles == null){
            Role role = roleRepository.findByName(ROLE_USER).
                    orElseThrow(() -> new SignupExceptions("Error : Role not found", HttpStatus.INTERNAL_SERVER_ERROR));
            roles.add(role);
        }else{
            strRoles.forEach(role -> {
                switch(role){
                    case adminStringConstant :
                        Role adminRole = roleRepository.findByName(ROLE_ADMIN).
                                orElseThrow(() -> new SignupExceptions("Error : Role not found", HttpStatus.INTERNAL_SERVER_ERROR));
                        roles.add(adminRole);
                        break;
                    case moderatorStringConstant :
                        Role moderatorRole = roleRepository.findByName(ROLE_MODERATOR).
                                orElseThrow(() -> new SignupExceptions("Error : Role not found", HttpStatus.INTERNAL_SERVER_ERROR));
                        roles.add(moderatorRole);
                        break;
                    default :
                        Role user = roleRepository.findByName(ROLE_USER).
                                orElseThrow(() -> new SignupExceptions("Error : Role not found", HttpStatus.INTERNAL_SERVER_ERROR));
                        roles.add(user);
                }
            });
        }
        return roles;
    }
}
