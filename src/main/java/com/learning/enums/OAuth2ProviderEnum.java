package com.learning.enums;

import lombok.Getter;

@Getter
public enum OAuth2ProviderEnum {
    LOCAL("LOCAL"),   // Default
    GOOGLE("GOOGLE"),
    GITHUB("GITHUB"),
    FACEBOOK("FACEBOOK")
    ;

    private final String oauth2Provide;

    OAuth2ProviderEnum(String oauth2Provide) {
        this.oauth2Provide = oauth2Provide;
    }


    public static OAuth2ProviderEnum getEnumFromEnumString(String enumString){
        try{
            return OAuth2ProviderEnum.valueOf(enumString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OAuth2ProviderEnum.LOCAL;
        }

    }

    public static OAuth2ProviderEnum getEnumFromProvider(String oauth2Provide){
        for(OAuth2ProviderEnum oAuth2ProviderEnum: OAuth2ProviderEnum.values()){
            if(oAuth2ProviderEnum.oauth2Provide.equals(oauth2Provide.toUpperCase())){
                return oAuth2ProviderEnum;
            }
        }
        return OAuth2ProviderEnum.LOCAL;
    }
}
