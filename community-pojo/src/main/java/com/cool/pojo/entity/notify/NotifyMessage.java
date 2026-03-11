package com.cool.pojo.entity.notify;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class NotifyMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;
    private Long receiverId;
    private Long senderId;
    private String notifyType;
    private String content;
    private Date createTime;

    private Long businessId;
    private String businessType;
    private String extra;

    public NotifyMessage() {
        this.createTime = new Date();
        this.messageId = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    public NotifyMessage(Long receiverId, Long senderId, String notifyType, String content, Long businessId) {
        this();
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.notifyType = notifyType;
        this.content = content;
        this.businessId = businessId;
    }

    public static NotifyMessageBuilder builder() {
        return new NotifyMessageBuilder();
    }

    public static class NotifyMessageBuilder {
        private NotifyMessage message = new NotifyMessage();

        public NotifyMessageBuilder receiverId(Long receiverId) {
            message.setReceiverId(receiverId);
            return this;
        }

        public NotifyMessageBuilder senderId(Long senderId) {
            message.setSenderId(senderId);
            return this;
        }

        public NotifyMessageBuilder notifyType(String notifyType) {
            message.setNotifyType(notifyType);
            return this;
        }

        public NotifyMessageBuilder content(String content) {
            message.setContent(content);
            return this;
        }

        public NotifyMessageBuilder businessId(Long businessId) {
            message.setBusinessId(businessId);
            return this;
        }

        public NotifyMessageBuilder businessType(String businessType) {
            message.setBusinessType(businessType);
            return this;
        }

        public NotifyMessageBuilder extra(String extra) {
            message.setExtra(extra);
            return this;
        }

        public NotifyMessage build() {
            return message;
        }
    }
}
