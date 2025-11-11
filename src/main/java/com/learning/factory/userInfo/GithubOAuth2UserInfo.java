package com.learning.factory.userInfo;

import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo{

    private final String accessToken;

    public GithubOAuth2UserInfo(Map<String, Object> attributes, String accessToken) {
        super(attributes);
        this.accessToken = accessToken;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        // GitHub might not return email in basic scope
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }

    public String getAccessToken() {
        return accessToken;
    }
}
