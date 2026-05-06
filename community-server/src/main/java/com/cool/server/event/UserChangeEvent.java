package com.cool.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserChangeEvent extends ApplicationEvent {
    
    private final Long userId;
    private final ChangeType changeType;
    
    public UserChangeEvent(Object source, Long userId, ChangeType changeType) {
        super(source);
        this.userId = userId;
        this.changeType = changeType;
    }
    
    public enum ChangeType {
        CREATE,
        UPDATE,
        DELETE
    }
}
