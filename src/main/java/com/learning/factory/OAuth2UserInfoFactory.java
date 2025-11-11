package com.learning.factory;

import com.learning.enums.OAuth2ProviderEnum;
import com.learning.factory.userInfo.FacebookOAuth2UserInfo;
import com.learning.factory.userInfo.GithubOAuth2UserInfo;
import com.learning.factory.userInfo.GoogleOAuth2UserInfo;
import com.learning.factory.userInfo.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
                                                   Map<String, Object> attributes,
                                                   String accessToken){
        switch (OAuth2ProviderEnum.getEnumFromEnumString(registrationId)){
            case GOOGLE -> {
                return new GoogleOAuth2UserInfo(attributes);
            }
            case GITHUB -> {
                return new GithubOAuth2UserInfo(attributes, accessToken);
            }
            case FACEBOOK -> {
                return new FacebookOAuth2UserInfo(attributes);
            }
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        }
    }
}
