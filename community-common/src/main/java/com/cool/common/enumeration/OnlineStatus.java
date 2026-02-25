package com.cool.common.enumeration;

public enum OnlineStatus {
    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    BUSY(2, "忙碌"),
    AWAY(3, "离开");
    
    private final int code;
    private final String desc;
    
    OnlineStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public static OnlineStatus getByCode(int code) {
        for (OnlineStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return OFFLINE;
    }
}
