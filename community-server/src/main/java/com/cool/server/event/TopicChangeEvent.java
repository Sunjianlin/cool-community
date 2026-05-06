package com.cool.server.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TopicChangeEvent extends ApplicationEvent {
    
    private final Long topicId;
    private final ChangeType changeType;
    
    public TopicChangeEvent(Object source, Long topicId, ChangeType changeType) {
        super(source);
        this.topicId = topicId;
        this.changeType = changeType;
    }
    
    public enum ChangeType {
        CREATE,
        UPDATE,
        DELETE
    }
}
