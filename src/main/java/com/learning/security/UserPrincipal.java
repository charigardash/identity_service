package com.learning.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.learning.dbentity.identity.User;
import com.learning.enums.OAuth2ProviderEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails, OAuth2User {
    private Long id;
    private String username;
    private String email;
    private OAuth2ProviderEnum provider;
    private String providerId;
    private String imageUrl;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;

    // Constructor for OAuth2 login
    public UserPrincipal(Long id, String username, String email, String password,
                         OAuth2ProviderEnum provider, String providerId, String imageUrl,
                         Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.imageUrl = imageUrl;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // Constructor for traditional login
    public UserPrincipal(Long id, String username, String email, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // Build method for traditional login
    public static UserPrincipal build(User user){
        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        return new UserPrincipal(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                grantedAuthorities);
    }

    // Create method for OAuth2 login
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
         return new UserPrincipal(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                user.getProvider(),
                user.getProviderId(),
                user.getImageUrl(),
                grantedAuthorities,
                 attributes);
    }

    public OAuth2ProviderEnum getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public String getImageUrl() { return imageUrl; }

    public Long getId(){
        return this.id;
    }

    public String getEmail(){
        return this.email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getName() {
        return this.username;
    }
}
