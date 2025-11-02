package com.learning.enums;

public enum DeviceNameEnum {

    WEB_BROWSER("Web Browser");

    private final String deviceName;

    DeviceNameEnum(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

}
