package com.cool.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostChangeEvent extends ApplicationEvent {
    
    private final Long postId;
    private final ChangeType changeType;
    
    public PostChangeEvent(Object source, Long postId, ChangeType changeType) {
        super(source);
        this.postId = postId;
        this.changeType = changeType;
    }
    
    public enum ChangeType {
        CREATE,
        UPDATE,
        DELETE
    }
}
