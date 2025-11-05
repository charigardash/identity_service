package com.learning.enums;

import lombok.Getter;

@Getter
public enum TwoFAMethodEnum {
    OTP("OTP"), TOTP("TOTP"), BACKUP("BACKUP");

    private final String method;
    TwoFAMethodEnum(String method) {
        this.method = method;
    }
}
