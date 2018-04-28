package com.example.user.finalhcproject;

public enum BridgeStateUpdatedEvent {
    UNKNOWN(-1),
    INITIALIZED(0),
    FULL_CONFIG(1),
    BRIDGE_CONFIG(2),
    LIGHTS_AND_GROUPS(3),
    SCENES(4),
    SENSORS_AND_SWITCHES(5),
    RULES(6),
    SCHEDULES_AND_TIMERS(7),
    RESOURCE_LINKS(8),
    DEVICE_SEARCH_STATUS(9);

    private int code;
    BridgeStateUpdatedEvent(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}